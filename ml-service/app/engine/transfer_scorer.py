"""
Transfer Scoring Engine: Squad-aware, budget-aware, fixture-aware transfer optimization.
Always returns a ranked list of viable alternatives for all 15 squad players,
categorized with clear verdict labels (RECOMMENDED, CONSIDER, SIDEWAYS, NOT_WORTH_HIT).
"""

import logging
from datetime import datetime
from typing import Any
from sqlalchemy.orm import Session
from sqlalchemy.sql import func

from app.core.config import settings
from app.models.db_models import MyTeam, MyTeamState, Player, Prediction, Gameweek, Team, Fixture, Recommendation

logger = logging.getLogger(__name__)

STANDARD_HIT_COST = 4
MODEL_VERSION = settings.default_model_version


def _get_player_xp_horizon(db: Session, player_id: int, start_gw: int, horizon: int) -> float:
    """Sums predicted xPoints for a player over horizon GWs."""
    val = (
        db.query(func.sum(Prediction.expected_points))
        .filter(
            Prediction.player_id == player_id,
            Prediction.gameweek_id >= start_gw,
            Prediction.gameweek_id < start_gw + horizon
        )
        .scalar()
    )
    if val is not None:
        return float(val)
    # Fallback to player form
    p = db.query(Player).filter(Player.id == player_id).first()
    form = float(p.form or 3.0) if p else 3.0
    return round(form * horizon, 2)


def _get_avg_fdr(db: Session, team_id: int, start_gw: int, num_gws: int = 3) -> float:
    """Computes average FDR for a team over upcoming gameweeks."""
    fixtures = (
        db.query(Fixture)
        .filter(
            Fixture.gameweek_id >= start_gw,
            Fixture.gameweek_id < start_gw + num_gws,
            (Fixture.team_h_id == team_id) | (Fixture.team_a_id == team_id)
        )
        .all()
    )
    if not fixtures:
        return 3.0
    diffs = [
        f.team_h_difficulty if f.team_h_id == team_id else f.team_a_difficulty
        for f in fixtures
        if (f.team_h_difficulty if f.team_h_id == team_id else f.team_a_difficulty) is not None
    ]
    return round(sum(diffs) / len(diffs), 1) if diffs else 3.0


def recommend_transfers(db: Session, horizon: int = 3, limit: int = 10) -> dict[str, Any]:
    """
    Evaluates transfer options across all 15 squad players.
    Always returns ranked alternatives, structured reasons, and verdict badges.
    """
    now_iso = datetime.utcnow().isoformat()

    # 1. Upcoming target gameweek
    next_gw_obj = db.query(Gameweek).filter(Gameweek.is_next == True).first()
    if not next_gw_obj:
        next_gw_obj = db.query(Gameweek).filter(Gameweek.is_current == True).first()
    target_gw = next_gw_obj.id if next_gw_obj else 1

    # 2. Latest squad state & picks
    latest_squad_gw = db.query(func.max(MyTeam.gameweek_id)).scalar()
    if latest_squad_gw is None:
        return {
            "status": "empty",
            "modelVersion": MODEL_VERSION,
            "generatedAt": now_iso,
            "freeTransfers": 1,
            "bank": 0.0,
            "holdAdvice": "Sync team data to see transfer suggestions.",
            "bestUseOfFreeTransfer": None,
            "bestHitCandidate": None,
            "options": [],
            "recommendations": []
        }

    team_state = db.query(MyTeamState).filter(MyTeamState.gameweek_id == latest_squad_gw).first()
    if not team_state:
        team_state = db.query(MyTeamState).order_by(MyTeamState.gameweek_id.desc()).first()

    bank = team_state.bank if team_state and team_state.bank is not None else 10  # tenths of £m
    free_transfers = team_state.free_transfers if team_state and team_state.free_transfers is not None else 1
    hit_cost = 0 if free_transfers >= 1 else STANDARD_HIT_COST

    picks = (
        db.query(MyTeam, Player, Team)
        .join(Player, MyTeam.player_id == Player.id)
        .join(Team, Player.team_id == Team.id)
        .filter(MyTeam.gameweek_id == latest_squad_gw)
        .all()
    )

    if not picks:
        return {
            "status": "empty",
            "modelVersion": MODEL_VERSION,
            "generatedAt": now_iso,
            "freeTransfers": free_transfers,
            "bank": round(bank / 10.0, 1),
            "holdAdvice": None,
            "bestUseOfFreeTransfer": None,
            "bestHitCandidate": None,
            "options": [],
            "recommendations": []
        }

    squad_player_ids = [p.Player.id for p in picks]
    squad_team_counts: dict[int, int] = {}
    for p in picks:
        tid = p.Player.team_id
        squad_team_counts[tid] = squad_team_counts.get(tid, 0) + 1

    # Preload squad xP for 1, 3, 6 GWs
    squad_xp_1: dict[int, float] = {}
    squad_xp_3: dict[int, float] = {}
    squad_xp_6: dict[int, float] = {}
    for p in picks:
        pid = p.Player.id
        squad_xp_1[pid] = _get_player_xp_horizon(db, pid, target_gw, 1)
        squad_xp_3[pid] = _get_player_xp_horizon(db, pid, target_gw, 3)
        squad_xp_6[pid] = _get_player_xp_horizon(db, pid, target_gw, 6)

    # 3. Candidate pool (top 80 active non-squad players by 3-GW predicted xP)
    candidate_rows = (
        db.query(Player, Team, func.sum(Prediction.expected_points).label("sum_xp"))
        .join(Team, Player.team_id == Team.id)
        .outerjoin(
            Prediction,
            (Player.id == Prediction.player_id) &
            (Prediction.gameweek_id >= target_gw) &
            (Prediction.gameweek_id < target_gw + 3)
        )
        .filter(
            Player.id.not_in(squad_player_ids),
            Player.status != "u"
        )
        .group_by(Player.id, Team.id)
        .order_by(func.sum(Prediction.expected_points).desc().nullslast())
        .limit(80)
        .all()
    )

    candidates = []
    for player, team, sum_xp in candidate_rows:
        xp3 = float(sum_xp) if sum_xp is not None else float(player.form or 3.0) * 3
        candidates.append((player, team, xp3))

    # 4. Evaluate all 15 squad players against same-position candidates within budget
    evaluated_pairs = []

    for out_pick, out_player, out_team in picks:
        # Selling price: for baseline local app, fallback to now_cost
        selling_price = out_player.now_cost
        max_buy_price = selling_price + bank
        out_fdr = _get_avg_fdr(db, out_team.id, target_gw, 3)

        for in_player, in_team, in_xp_3 in candidates:
            # Same position required
            if in_player.element_type != out_player.element_type:
                continue

            # Budget rule
            if in_player.now_cost > max_buy_price:
                continue

            # Team limit rule (max 3 per club)
            new_club_count = squad_team_counts.get(in_player.team_id, 0)
            if in_player.team_id != out_player.team_id and new_club_count >= 3:
                continue

            # Multi-horizon xP
            in_xp_1 = _get_player_xp_horizon(db, in_player.id, target_gw, 1)
            in_xp_6 = _get_player_xp_horizon(db, in_player.id, target_gw, 6)

            gain_1 = round(in_xp_1 - squad_xp_1[out_player.id], 2)
            gain_3 = round(in_xp_3 - squad_xp_3[out_player.id], 2)
            gain_6 = round(in_xp_6 - squad_xp_6[out_player.id], 2)

            net_value_no_hit = gain_3
            net_value = round(net_value_no_hit - hit_cost, 2)

            # Verdict categorization
            if net_value >= 1.5:
                verdict = "RECOMMENDED"
            elif net_value >= 0.0:
                verdict = "CONSIDER"
            elif net_value_no_hit > 0.0 and hit_cost > 0:
                verdict = "NOT_WORTH_HIT"
            else:
                verdict = "SIDEWAYS"

            # Deterministic structured reasons
            reasons = []
            in_fdr = _get_avg_fdr(db, in_team.id, target_gw, 3)
            if in_fdr < out_fdr:
                reasons.append({
                    "type": "FIXTURE_RUN",
                    "detail": f"Next 3 GWs avg FDR {in_fdr} vs {out_fdr} for {out_player.web_name}"
                })

            out_chance = out_player.chance_of_playing_next if out_player.chance_of_playing_next is not None else 100
            if out_chance < 100 or out_player.status in ["i", "d"]:
                reasons.append({
                    "type": "MINUTES_RISK",
                    "detail": f"{out_player.web_name} flagged with {out_chance}% playing chance"
                })

            in_form = float(in_player.form or 0)
            out_form = float(out_player.form or 0)
            if in_form >= out_form + 1.0:
                reasons.append({
                    "type": "FORM_TREND",
                    "detail": f"Recent form momentum: {in_player.web_name} ({in_form:.1f}) vs {out_player.web_name} ({out_form:.1f})"
                })

            in_transfers_in = in_player.transfers_in_event or 0
            if in_transfers_in >= 40000:
                reasons.append({
                    "type": "PRICE_RISE_RISK",
                    "detail": f"{in_player.web_name} heavily bought (+{in_transfers_in:,} transfers this GW)"
                })

            in_ownership = float(in_player.selected_by_percent or 0)
            out_ownership = float(out_player.selected_by_percent or 0)
            if in_ownership >= out_ownership + 12.0:
                reasons.append({
                    "type": "OWNERSHIP_TREND",
                    "detail": f"High effective ownership ({in_ownership:.1f}%) offers rank protection"
                })

            if not reasons:
                reasons.append({
                    "type": "XP_GAIN",
                    "detail": f"Projected +{gain_3:.1f} pts gain over 3 gameweeks"
                })

            confidence_num = round(min(0.95, max(0.45, 0.60 + (gain_3 / 15.0))), 2)
            confidence_label = "HIGH" if confidence_num >= 0.75 else ("MEDIUM" if confidence_num >= 0.55 else "LOW")
            bank_after = round((bank + selling_price - in_player.now_cost) / 10.0, 1)

            opt = {
                # Strict API contract fields
                "out": {
                    "id": out_player.id,
                    "name": out_player.web_name,
                    "team": out_team.short_name,
                    "price": round(out_player.now_cost / 10.0, 1),
                    "sellingPrice": round(selling_price / 10.0, 1),
                    "xP3": squad_xp_3[out_player.id]
                },
                "in": {
                    "id": in_player.id,
                    "name": in_player.web_name,
                    "team": in_team.short_name,
                    "price": round(in_player.now_cost / 10.0, 1),
                    "xP3": in_xp_3,
                    "ownership": float(in_player.selected_by_percent or 0)
                },
                "priceDelta": round((in_player.now_cost - selling_price) / 10.0, 1),
                "bankAfter": bank_after,
                "xpGain": {
                    "gw1": gain_1,
                    "gw3": gain_3,
                    "gw6": gain_6
                },
                "hitCost": hit_cost,
                "netValue3gw": net_value,
                "verdict": verdict,
                "confidence": confidence_label,
                "reasons": reasons,

                # Backward-compatible fields
                "transfer_out": {
                    "id": out_player.id,
                    "web_name": out_player.web_name,
                    "team_short": out_team.short_name,
                    "position": ["", "GK", "DEF", "MID", "FWD"][out_player.element_type],
                    "selling_price": round(selling_price / 10.0, 1),
                    "now_cost": round(out_player.now_cost / 10.0, 1),
                    "xp_1": squad_xp_1[out_player.id],
                    "xp_3": squad_xp_3[out_player.id],
                    "xp_6": squad_xp_6[out_player.id],
                },
                "transfer_in": {
                    "id": in_player.id,
                    "web_name": in_player.web_name,
                    "team_short": in_team.short_name,
                    "position": ["", "GK", "DEF", "MID", "FWD"][in_player.element_type],
                    "price": round(in_player.now_cost / 10.0, 1),
                    "xp_1": in_xp_1,
                    "xp_3": in_xp_3,
                    "xp_6": in_xp_6,
                },
                "price_delta": round((in_player.now_cost - selling_price) / 10.0, 1),
                "bank_after": bank_after,
                "xp_gain_1": gain_1,
                "xp_gain_3": gain_3,
                "xp_gain_6": gain_6,
                "net_value": net_value,
                "net_value_no_hit": net_value_no_hit,
                "hit_cost": hit_cost,
                "summary": f"Swap {out_player.web_name} for {in_player.web_name}: {gain_3:+.1f} xPts over 3 GWs (bank: £{bank_after:.1f}m)."
            }
            evaluated_pairs.append(opt)

    # Sort primarily by 3-GW net value, secondarily by 1-GW gain
    evaluated_pairs.sort(key=lambda x: (x["netValue3gw"], x["xpGain"]["gw1"]), reverse=True)
    top_recommendations = evaluated_pairs[:limit]

    # Best use of free transfer: highest net_value_no_hit
    best_ft = max(evaluated_pairs, key=lambda x: x["net_value_no_hit"]) if evaluated_pairs else None
    # Best hit candidate: highest raw gain where gain_3 > 4.0
    hit_candidates = [p for p in evaluated_pairs if p["xpGain"]["gw3"] > 4.0]
    best_hit = max(hit_candidates, key=lambda x: x["xpGain"]["gw3"]) if hit_candidates else None

    # Determine holdAdvice
    has_strong_move = any(r["verdict"] == "RECOMMENDED" for r in top_recommendations)
    if not has_strong_move:
        hold_advice = (
            "Holding your free transfer is reasonable this week as your current squad's projected points "
            "are competitive. Closest alternative upgrades are listed below."
        )
    else:
        hold_advice = None

    # Persist recommendation record
    try:
        rec_row = Recommendation(
            gameweek_id=target_gw,
            type="transfers",
            payload={"count": len(top_recommendations), "top": top_recommendations[0]["summary"] if top_recommendations else None},
            reasoning=f"Evaluated {len(evaluated_pairs)} transfer alternatives for GW {target_gw}.",
            confidence=0.85
        )
        db.add(rec_row)
        db.commit()
    except Exception as e:
        db.rollback()
        logger.warning("Could not persist transfer recommendation: %s", e)

    return {
        "status": "success",
        "modelVersion": MODEL_VERSION,
        "generatedAt": now_iso,
        "horizon": horizon,
        "freeTransfers": free_transfers,
        "free_transfers": free_transfers,
        "bank": round(bank / 10.0, 1),
        "holdAdvice": hold_advice,
        "bestUseOfFreeTransfer": best_ft,
        "bestHitCandidate": best_hit,
        "options": top_recommendations,
        "recommendations": top_recommendations
    }
