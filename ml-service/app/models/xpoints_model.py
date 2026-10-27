"""
Expected Points (xPoints) Model & Persistence Layer.
Predicts expected points, floor (10th percentile), and ceiling (90th percentile)
conditioned on P(start), position, form, underlying xGI, and fixture difficulty.
"""

import logging
import pandas as pd
import numpy as np
from sqlalchemy.orm import Session
from sqlalchemy.dialects.postgresql import insert as pg_insert

from app.models.db_models import Prediction
from app.features.builder import build_feature_dataframe
from app.models.minutes_model import predict_start_probabilities

logger = logging.getLogger(__name__)

MODEL_VERSION = "xP-v1.0"

# Position-specific scoring weights
POSITION_BASE_POINTS = {
    1: 3.5,  # GK (clean sheet potential + saves)
    2: 3.8,  # DEF (clean sheet potential + attacking returns)
    3: 4.2,  # MID (goals 5pts, assists 3pts, clean sheet 1pt)
    4: 4.5,  # FWD (goals 4pts, assists 3pts)
}


def generate_and_save_predictions(db: Session, target_gw: int | None = None) -> int:
    """
    Generates xPoints predictions for target_gw and saves them into the PostgreSQL
    predictions table. Returns number of saved predictions.
    """
    features_df = build_feature_dataframe(db, target_gw)
    if features_df.empty:
        return 0

    target_gw_val = int(features_df['target_gw'].iloc[0])

    # 1. Start Probabilities
    start_probs = predict_start_probabilities(features_df)
    features_df['start_prob'] = start_probs

    # 2. Position base expected points
    pos_base = features_df['element_type'].map(POSITION_BASE_POINTS).fillna(4.0)

    # 3. Form & Underlying xGI multiplier
    form_weight = features_df['rolling_pts_5'] * 0.4
    xgi_weight = features_df['rolling_xgi_5'] * 3.5

    # 4. Fixture difficulty multiplier (FDR 1=easy x1.2, 3=neutral x1.0, 5=hard x0.75)
    fdr = features_df['fdr']
    fdr_mult = np.where(fdr == 1, 1.25,
               np.where(fdr == 2, 1.12,
               np.where(fdr == 3, 1.0,
               np.where(fdr == 4, 0.85, 0.72))))

    # Home advantage multiplier
    home_mult = np.where(features_df['is_home'] == 1, 1.08, 0.94)

    # Unconditioned raw expected points if playing 90 mins
    raw_xpoints = (pos_base + form_weight + xgi_weight) * fdr_mult * home_mult

    # Expected points conditioned on P(start)
    expected_pts = raw_xpoints * start_probs

    # Floor & Ceiling calculations
    floor = np.maximum(1.0 * start_probs, expected_pts * 0.5)
    ceiling = expected_pts * 1.8 + np.where(features_df['element_type'].isin([3, 4]), 3.0, 2.0)

    features_df['expected_points'] = np.round(expected_pts, 2)
    features_df['floor'] = np.round(floor, 2)
    features_df['ceiling'] = np.round(ceiling, 2)

    # 5. Persist into PostgreSQL predictions table
    saved_count = 0
    for _, row in features_df.iterrows():
        stmt = pg_insert(Prediction).values(
            player_id=int(row['player_id']),
            gameweek_id=target_gw_val,
            model_version=MODEL_VERSION,
            expected_points=float(row['expected_points']),
            floor=float(row['floor']),
            ceiling=float(row['ceiling']),
            start_probability=float(row['start_prob']),
        ).on_conflict_do_update(
            index_elements=["player_id", "gameweek_id", "model_version"],
            set_={
                "expected_points": float(row['expected_points']),
                "floor": float(row['floor']),
                "ceiling": float(row['ceiling']),
                "start_probability": float(row['start_prob']),
            }
        )
        db.execute(stmt)
        saved_count += 1

    db.commit()
    logger.info("Saved %d predictions for GW %d into database", saved_count, target_gw_val)
    return saved_count
