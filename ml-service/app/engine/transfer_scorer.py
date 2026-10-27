"""
Transfer Scoring Engine: Squad-aware, budget-aware, fixture-aware transfer optimization.
Ranks potential transfers in/out pairs by predicted net points gain over a 3-GW horizon:
  Net Gain = sum(xPoints_in) - sum(xPoints_out) - hit_cost
Enforces FPL rules: budget legality (price_in <= price_out + bank) & team limits (<= 3 per club).
"""

import logging
from sqlalchemy.orm import Session
from sqlalchemy.sql import func

from app.models.db_models import MyTeam, MyTeamState, Player, Prediction, Gameweek, Team

logger = logging.getLogger(__name__)

HIT_COST = 4  # Standard FPL transfer hit penalty for extra transfers


def recommend_transfers(db: Session, horizon: int = 3) -> list[dict]:
    """
    Evaluates transfer options for the current squad over horizon gameweeks.
    Returns ranked transfer recommendations.
    """
    # 1. Fetch current user squad state
    current_gw_obj = db.query(Gameweek).filter(Gameweek.is_current == True).first()
    if not current_gw_obj:
        current_gw_obj = db.query(Gameweek).filter(Gameweek.is_next == True).first()
    if not current_gw_obj:
        return []

    target_gw = current_gw_obj.id

    team_state = db.query(MyTeamState).filter(MyTeamState.gameweek_id == target_gw).first()
    if not team_state:
        team_state = db.query(MyTeamState).order_by(MyTeamState.gameweek_id.desc()).first()

    bank = team_state.bank if team_state else 10  # Bank in tenths

    # 2. Fetch current squad picks (15 players)
    picks = (
        db.query(MyTeam, Player)
        .join(Player, MyTeam.player_id == Player.id)
        .filter(MyTeam.gameweek_id == target_gw)
        .all()
    )

    if not picks:
        return []

    squad_player_ids = [p.Player.id for p in picks]
    squad_team_counts: dict[int, int] = {}
    for p in picks:
        tid = p.Player.team_id
        squad_team_counts[tid] = squad_team_counts.get(tid, 0) + 1

    # 3. Sum predicted xPoints over horizon for current squad
    squad_xp: dict[int, float] = {}
    for pid in squad_player_ids:
        preds = (
            db.query(func.sum(Prediction.expected_points))
            .filter(
                Prediction.player_id == pid,
                Prediction.gameweek_id >= target_gw,
                Prediction.gameweek_id < target_gw + horizon
            )
            .scalar()
        )
        squad_xp[pid] = float(preds or 0.0)

    # 4. Fetch candidate transfer targets (top 50 non-squad players by total expected points)
    candidates = (
        db.query(Player, func.sum(Prediction.expected_points).label("sum_xp"))
        .join(Prediction, Player.id == Prediction.player_id)
        .filter(
            Player.id.not_in(squad_player_ids),
            Player.status == 'a',
            Prediction.gameweek_id >= target_gw,
            Prediction.gameweek_id < target_gw + horizon
        )
        .group_by(Player.id)
        .order_by(func.sum(Prediction.expected_points).desc())
        .limit(50)
        .all()
    )

    # 5. Evaluate all (out, in) pairs for legal budget & team constraints
    recommendations = []
    for out_pick, out_player in picks:
        out_xp = squad_xp.get(out_player.id, 0.0)
        max_buy_price = out_player.now_cost + bank

        for in_player, in_xp_sum in candidates:
            in_xp = float(in_xp_sum or 0.0)

            # Check price budget constraint
            if in_player.now_cost > max_buy_price:
                continue

            # Check position match
            if in_player.element_type != out_player.element_type:
                continue

            # Check club limit constraint (max 3 per club)
            new_team_count = squad_team_counts.get(in_player.team_id, 0)
            if in_player.team_id != out_player.team_id and new_team_count >= 3:
                continue

            net_gain = round(in_xp - out_xp, 2)
            if net_gain > 0.5:
                recommendations.append({
                    "transfer_out": {
                        "id": out_player.id,
                        "web_name": out_player.web_name,
                        "now_cost": out_player.now_cost,
                        "xpoints_horizon": round(out_xp, 2)
                    },
                    "transfer_in": {
                        "id": in_player.id,
                        "web_name": in_player.web_name,
                        "now_cost": in_player.now_cost,
                        "xpoints_horizon": round(in_xp, 2)
                    },
                    "net_points_gain": net_gain,
                    "cost_delta": round((in_player.now_cost - out_player.now_cost) / 10.0, 1),
                    "horizon_gws": horizon,
                    "reasoning": f"Replacing {out_player.web_name} ({out_xp:.1f} xPts) with {in_player.web_name} ({in_xp:.1f} xPts) yields +{net_gain:.1f} net points over next {horizon} GWs."
                })

    # Sort by highest net points gain
    recommendations.sort(key=lambda r: r["net_points_gain"], reverse=True)
    return recommendations[:10]
