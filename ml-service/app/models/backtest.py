"""
Model Evaluation & Backtest Script.
Evaluates model predictions against actual historical gameweek results
to compute MAE (Mean Absolute Error) and RMSE (Root Mean Squared Error).
"""

import logging
import numpy as np
import pandas as pd
from sqlalchemy.orm import Session

from app.models.db_models import PlayerGameweekStats, Prediction

logger = logging.getLogger(__name__)


def run_backtest_evaluation(db: Session, num_gameweeks: int = 5) -> dict[str, float]:
    """
    Evaluates predictions stored in the database against actual player_gameweek_stats.
    """
    results = (
        db.query(
            Prediction.expected_points,
            PlayerGameweekStats.total_points
        )
        .join(
            PlayerGameweekStats,
            (Prediction.player_id == PlayerGameweekStats.player_id) &
            (Prediction.gameweek_id == PlayerGameweekStats.gameweek_id)
        )
        .all()
    )

    if not results:
        logger.warning("No overlapping predictions and actual stats for backtest evaluation.")
        return {"mae": 0.0, "rmse": 0.0, "sample_size": 0}

    df = pd.DataFrame(results, columns=['pred', 'actual'])
    df['pred'] = df['pred'].astype(float)
    df['actual'] = df['actual'].astype(float)

    mae = float(np.mean(np.abs(df['pred'] - df['actual'])))
    rmse = float(np.sqrt(np.mean((df['pred'] - df['actual']) ** 2)))

    logger.info("Backtest evaluation complete (N=%d): MAE=%.2f, RMSE=%.2f", len(df), mae, rmse)
    return {
        "mae": round(mae, 3),
        "rmse": round(rmse, 3),
        "sample_size": len(df)
    }
