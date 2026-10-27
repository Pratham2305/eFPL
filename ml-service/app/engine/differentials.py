"""
Differential Detector Engine.
Filters non-squad players whose model prediction meaningfully exceeds market expectations.
Uses stepwise ownership threshold relaxation (<5%, <10%, <15%) to ensure at least 5 gems are found.
"""

import logging
from typing import Any
from sqlalchemy.orm import Session
from sqlalchemy.sql import func

from app.models.db_models import Player, Prediction, Gameweek, Team, MyTeam

logger = logging.getLogger(__name__)


def detect_differentials(db: Session, max_ownership: float | None = None) -> dict[str, Any]:
    """
    Finds non-owned players under ownership threshold with top expected points.
    Relaxes threshold stepwise if needed to ensure at least 5 gems are returned.
    """
    next_gw_obj = db.query(Gameweek).filter(Gameweek.is_next == True).first()
    if not next_gw_obj:
        next_gw_obj = db.query(Gameweek).filter(Gameweek.is_current == True).first()
    target_gw = next_gw_obj.id if next_gw_obj else 1

    latest_squad_gw = db.query(func.max(MyTeam.gameweek_id)).scalar()
    squad_player_ids = set()
    if latest_squad_gw is not None:
        squad_player_ids = {
            r[0] for r in db.query(MyTeam.player_id).filter(MyTeam.gameweek_id == latest_squad_gw).all()
        }

    # Stepwise threshold levels
    threshold_ladder = [5.0, 10.0, 15.0]
    if max_ownership is not None and max_ownership > 0 and max_ownership not in threshold_ladder:
        threshold_ladder = sorted(list(set(threshold_ladder + [float(max_ownership)])))

    selected_threshold = threshold_ladder[-1]
    best_results = []

    for threshold in threshold_ladder:
        query = (
            db.query(Player, Prediction, Team)
            .join(Team, Player.team_id == Team.id)
            .join(Prediction, (Player.id == Prediction.player_id) & (Prediction.gameweek_id == target_gw))
            .filter(
                Player.selected_by_percent <= threshold,
                Player.status == "a",
                Prediction.expected_points >= 3.5
            )
        )
        if squad_player_ids:
            query = query.filter(Player.id.not_in(squad_player_ids))

        rows = query.order_by(Prediction.expected_points.desc()).limit(12).all()
        if len(rows) >= 5 or threshold == threshold_ladder[-1]:
            selected_threshold = threshold
            best_results = rows
            if len(rows) >= 5:
                break

    results = []
    for player, pred, team in best_results:
        results.append({
            "player_id": player.id,
            "web_name": player.web_name,
            "team_short": team.short_name,
            "position_name": ["", "GK", "DEF", "MID", "FWD"][player.element_type],
            "now_cost": round(player.now_cost / 10.0, 1),
            "ownership_percent": float(player.selected_by_percent or 0),
            "expected_points": round(float(pred.expected_points), 2),
            "ceiling": round(float(pred.ceiling or 0), 2),
            "start_probability": round(float(pred.start_probability or 1.0), 2)
        })

    return {
        "status": "success",
        "target_gameweek": target_gw,
        "threshold_used": selected_threshold,
        "differentials": results
    }
