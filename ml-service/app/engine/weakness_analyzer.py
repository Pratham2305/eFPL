"""
Squad Weakness Analyzer.
Flags starters with low expected points or high injury risk over upcoming GWs.
"""

import logging
from sqlalchemy.orm import Session
from sqlalchemy.sql import func

from app.models.db_models import MyTeam, Player, Prediction, Gameweek, Team

logger = logging.getLogger(__name__)


def analyze_weaknesses(db: Session, threshold_xp: float = 3.0) -> list[dict]:
    """
    Finds squad starters whose predicted xPoints fall below threshold_xp.
    """
    current_gw_obj = db.query(Gameweek).filter(Gameweek.is_current == True).first()
    if not current_gw_obj:
        current_gw_obj = db.query(Gameweek).filter(Gameweek.is_next == True).first()
    if not current_gw_obj:
        return []

    target_gw = current_gw_obj.id

    starters = (
        db.query(MyTeam, Player, Prediction, Team)
        .join(Player, MyTeam.player_id == Player.id)
        .join(Team, Player.team_id == Team.id)
        .outerjoin(
            Prediction,
            (Player.id == Prediction.player_id) & (Prediction.gameweek_id == target_gw)
        )
        .filter(MyTeam.gameweek_id == target_gw, MyTeam.position <= 11)
        .all()
    )

    weaknesses = []
    for pick, player, pred, team in starters:
        xp = float(pred.expected_points) if pred and pred.expected_points else float(player.form or 0)
        chance = player.chance_of_playing_next if player.chance_of_playing_next is not None else 100

        if xp < threshold_xp or chance < 75:
            issue = "Injured / Doubtful" if chance < 75 else f"Low Expected Points ({xp:.1f} xPts)"
            weaknesses.append({
                "player_id": player.id,
                "web_name": player.web_name,
                "team_short": team.short_name,
                "position_name": ["", "GK", "DEF", "MID", "FWD"][player.element_type],
                "expected_points": round(xp, 2),
                "chance_of_playing": chance,
                "issue": issue,
                "now_cost": player.now_cost
            })

    return sorted(weaknesses, key=lambda w: w["expected_points"])
