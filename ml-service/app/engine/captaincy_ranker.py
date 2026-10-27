"""
Captaincy Ranker Engine.
Ranks ALL starting XI players by risk-adjusted expected points for the upcoming GW:
  - Safe: floor-weighted (high floor + start security)
  - Balanced: expected points * start probability
  - Aggressive: ceiling-weighted (high ceiling + explosive potential)
Suggests a reliable Vice-Captain from a different team and surfaces top outside-squad alternatives.
"""

import logging
from typing import Any
from sqlalchemy.orm import Session
from sqlalchemy.sql import func

from app.models.db_models import MyTeam, Player, Prediction, Gameweek, Team, Fixture, Recommendation

logger = logging.getLogger(__name__)


def _get_player_fixture(db: Session, team_id: int, gameweek_id: int) -> dict[str, Any]:
    """Retrieves fixture details for a team in a given gameweek."""
    fixture = (
        db.query(Fixture)
        .filter(
            Fixture.gameweek_id == gameweek_id,
            (Fixture.team_h_id == team_id) | (Fixture.team_a_id == team_id)
        )
        .first()
    )
    if not fixture:
        return {"opponent": "TBD", "is_home": True, "fdr": 3}

    is_home = fixture.team_h_id == team_id
    fdr = (fixture.team_h_difficulty if is_home else fixture.team_a_difficulty) or 3
    opp_id = fixture.team_a_id if is_home else fixture.team_h_id
    opp_team = db.query(Team).filter(Team.id == opp_id).first()
    opp_name = opp_team.short_name if opp_team else "OPP"

    return {
        "opponent": opp_name,
        "is_home": is_home,
        "fdr": fdr
    }


def _generate_captain_reason(web_name: str, xp: float, fdr: int, opp: str, is_home: bool,
                             ceiling: float, floor: float, start_prob: float, form: float) -> str:
    """Generates a concise deterministic reasoning for captaincy recommendation."""
    loc = "H" if is_home else "A"
    if xp >= 6.5:
        return f"Elite ceiling ({ceiling:.1f} pts) in favorable matchup vs {opp} ({loc}, FDR {fdr})."
    if fdr <= 2 and is_home:
        return f"Target fixture: plays at home against FDR {fdr} {opp} with high expected goal involvement."
    if floor >= 3.0 and start_prob >= 0.9:
        return f"High floor ({floor:.1f} pts) with guaranteed 90-minute security vs {opp} ({loc})."
    if form >= 5.0:
        return f"Excellent form ({form:.1f} pts/match) with solid underlying goal threat."
    if start_prob < 0.85:
        return f"High upside ({ceiling:.1f} pts) but minor rotation risk ({int(start_prob * 100)}% start prob)."
    return f"Projected {xp:.1f} xPts against {opp} ({loc}, FDR {fdr})."


def rank_captains(db: Session, risk_profile: str = "balanced") -> dict[str, Any]:
    """
    Ranks captaincy choices from current squad for next GW.
    Guarantees all starting XI players are evaluated and ranked.
    """
    profile = risk_profile.lower().strip()
    if profile not in ["safe", "balanced", "aggressive"]:
        profile = "balanced"

    # 1. Target gameweek
    next_gw_obj = db.query(Gameweek).filter(Gameweek.is_next == True).first()
    if not next_gw_obj:
        next_gw_obj = db.query(Gameweek).filter(Gameweek.is_current == True).first()
    target_gw = next_gw_obj.id if next_gw_obj else 1

    # 2. Latest squad picks
    latest_squad_gw = db.query(func.max(MyTeam.gameweek_id)).scalar()
    if latest_squad_gw is None:
        return {
            "status": "empty",
            "message": "No squad found. Please sync your team first.",
            "target_gameweek": target_gw,
            "risk_profile": profile,
            "rankings": [],
            "vice_captain": None,
            "outside_squad": []
        }

    starters = (
        db.query(MyTeam, Player, Team)
        .join(Player, MyTeam.player_id == Player.id)
        .join(Team, Player.team_id == Team.id)
        .filter(MyTeam.gameweek_id == latest_squad_gw, MyTeam.position <= 11)
        .order_by(MyTeam.position.asc())
        .all()
    )

    if not starters:
        # Fallback to all squad players if position flags are missing
        starters = (
            db.query(MyTeam, Player, Team)
            .join(Player, MyTeam.player_id == Player.id)
            .join(Team, Player.team_id == Team.id)
            .filter(MyTeam.gameweek_id == latest_squad_gw)
            .order_by(MyTeam.position.asc())
            .limit(11)
            .all()
        )

    if not starters:
        return {
            "status": "empty",
            "message": "No starting XI players found in squad.",
            "target_gameweek": target_gw,
            "risk_profile": profile,
            "rankings": [],
            "vice_captain": None,
            "outside_squad": []
        }

    squad_player_ids = [p.Player.id for p in starters]

    # Pre-fetch predictions for target_gw
    preds = {
        pr.player_id: pr
        for pr in db.query(Prediction).filter(
            Prediction.gameweek_id == target_gw,
            Prediction.player_id.in_(squad_player_ids)
        ).all()
    }

    candidates = []
    for pick, player, team in starters:
        pred = preds.get(player.id)
        xp = float(pred.expected_points) if pred and pred.expected_points is not None else float(player.form or 3.0)
        floor = float(pred.floor) if pred and pred.floor is not None else round(xp * 0.45, 2)
        ceiling = float(pred.ceiling) if pred and pred.ceiling is not None else round(xp * 1.8 + 2.0, 2)
        start_prob = float(pred.start_probability) if pred and pred.start_probability is not None else 0.85
        ownership = float(player.selected_by_percent or 0)
        form = float(player.form or 0)

        # Fixture info
        fixt = _get_player_fixture(db, team.id, target_gw)
        chance = player.chance_of_playing_next if player.chance_of_playing_next is not None else 100
        rotation_risk = (start_prob < 0.85) or (chance < 100) or (player.status != "a")

        # Risk-adjusted score
        if profile == "safe":
            # Emphasize high floor and start security
            score = round((xp * 0.40 + floor * 0.60) * start_prob, 2)
        elif profile == "aggressive":
            # Emphasize ceiling potential and differential leverage
            diff_factor = 1.0 + max(0.0, (25.0 - ownership) / 100.0)
            score = round((xp * 0.35 + ceiling * 0.65) * (start_prob ** 0.5) * diff_factor, 2)
        else:  # balanced
            score = round(xp * start_prob, 2)

        reason = _generate_captain_reason(
            player.web_name, xp, fixt["fdr"], fixt["opponent"], fixt["is_home"],
            ceiling, floor, start_prob, form
        )

        candidates.append({
            "player_id": player.id,
            "web_name": player.web_name,
            "team_id": team.id,
            "team_short": team.short_name,
            "position": ["", "GK", "DEF", "MID", "FWD"][player.element_type],
            "expected_points": round(xp, 2),
            "floor": round(floor, 2),
            "ceiling": round(ceiling, 2),
            "start_probability": round(start_prob, 2),
            "opponent": fixt["opponent"],
            "is_home": fixt["is_home"],
            "fdr": fixt["fdr"],
            "form": form,
            "rotation_risk": rotation_risk,
            "ownership_percent": ownership,
            "score": score,
            "is_current_captain": bool(pick.is_captain),
            "is_current_vice_captain": bool(pick.is_vice_captain),
            "reason": reason
        })

    # Sort ALL starters by score descending
    candidates.sort(key=lambda c: (c["score"], c["expected_points"]), reverse=True)

    # Assign rank (1 to 11)
    for rank, c in enumerate(candidates, start=1):
        c["rank"] = rank

    top_captain = candidates[0] if candidates else None

    # Suggest Vice-Captain: highest safe score on a different club where possible
    vc_candidates = [
        c for c in candidates[1:]
        if top_captain and c["team_id"] != top_captain["team_id"] and not c["rotation_risk"]
    ]
    if not vc_candidates:
        vc_candidates = candidates[1:]

    vice_captain = vc_candidates[0] if vc_candidates else None

    # Top 3 outside-squad alternatives
    outside_rows = (
        db.query(Player, Team, Prediction)
        .join(Team, Player.team_id == Team.id)
        .join(Prediction, (Player.id == Prediction.player_id) & (Prediction.gameweek_id == target_gw))
        .filter(
            Player.id.not_in(squad_player_ids),
            Player.status == "a"
        )
        .order_by(Prediction.expected_points.desc())
        .limit(3)
        .all()
    )

    outside_squad = []
    for player, team, pred in outside_rows:
        fixt = _get_player_fixture(db, team.id, target_gw)
        xp = float(pred.expected_points)
        outside_squad.append({
            "player_id": player.id,
            "web_name": player.web_name,
            "team_short": team.short_name,
            "position": ["", "GK", "DEF", "MID", "FWD"][player.element_type],
            "expected_points": round(xp, 2),
            "ceiling": float(pred.ceiling or 0),
            "ownership_percent": float(player.selected_by_percent or 0),
            "opponent": fixt["opponent"],
            "is_home": fixt["is_home"],
            "fdr": fixt["fdr"]
        })

    # Persist recommendation
    try:
        rec_row = Recommendation(
            gameweek_id=target_gw,
            type="captaincy",
            payload={
                "top_captain": top_captain["web_name"] if top_captain else None,
                "vice_captain": vice_captain["web_name"] if vice_captain else None,
                "risk_profile": profile
            },
            reasoning=top_captain["reason"] if top_captain else f"Captaincy ranked for GW {target_gw}",
            confidence=0.90
        )
        db.add(rec_row)
        db.commit()
    except Exception as e:
        db.rollback()
        logger.warning("Could not persist captaincy recommendation: %s", e)

    return {
        "status": "success",
        "target_gameweek": target_gw,
        "risk_profile": profile,
        "top_recommendation": top_captain,
        "vice_captain_suggestion": vice_captain,
        "rankings": candidates,
        "outside_squad": outside_squad
    }
