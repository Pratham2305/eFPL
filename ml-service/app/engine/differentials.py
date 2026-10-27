"""
Differential Detector Engine.
Filters non-squad players with ownership < 15% whose model prediction meaningfully
exceeds market expectations.
"""

import logging
from sqlalchemy.orm import Session

from app.models.db_models import Player, Prediction, Gameweek, Team

logger = logging.getLogger(__name__)


def detect_differentials(db: Session, max_ownership: float = 12.0) -> list[dict]:
    """
    Finds non-owned players under max_ownership threshold with top expected points.
    """
    current_gw_obj = db.query(Gameweek).filter(Gameweek.is_current == True).first()
    if not current_gw_obj:
        current_gw_obj = db.query(Gameweek).filter(Gameweek.is_next == True).first()
    if not current_gw_obj:
        return []

    target_gw = current_gw_obj.id

    differentials = (
        db.query(Player, Prediction, Team)
        .join(Team, Player.team_id == Team.id)
        .join(Prediction, (Player.id == Prediction.player_id) & (Prediction.gameweek_id == target_gw))
        .filter(
            Player.selected_by_percent <= max_ownership,
            Player.status == 'a',
            Prediction.expected_points >= 4.0
        )
        .order_by(Prediction.expected_points.desc())
        .limit(10)
        .all()
    )

    results = []
    for player, pred, team in differentials:
        results.append({
            "player_id": player.id,
            "web_name": player.web_name,
            "team_short": team.short_name,
            "position_name": ["", "GK", "DEF", "MID", "FWD"][player.element_type],
            "now_cost": player.now_cost,
            "ownership_percent": float(player.selected_by_percent or 0),
            "expected_points": float(pred.expected_points),
            "ceiling": float(pred.ceiling or 0),
            "start_probability": float(pred.start_probability or 1.0)
        })

    return results
