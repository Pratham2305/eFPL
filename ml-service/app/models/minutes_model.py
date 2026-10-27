"""
Start Probability & Expected Minutes Classifier.
Predicts P(minutes >= 60) for a given player based on recent start trends,
injury status, and rolling minutes.
"""

import numpy as np
import pandas as pd


def predict_start_probabilities(features_df: pd.DataFrame) -> pd.Series:
    """
    Computes start probability P(start) in [0.0, 1.0] for each player.
    Conditioned on chance of playing next round, rolling minutes, and start ratio.
    """
    if features_df.empty:
        return pd.Series(dtype=float)

    # Base availability factor from official injury/status flag (0-100)
    availability = features_df['chance_of_playing'] / 100.0

    # Minutes factor based on rolling 5-GW minutes and start ratio
    mins_factor = np.clip(features_df['rolling_mins_5'] / 90.0, 0.0, 1.0)
    start_ratio = features_df['start_ratio_5']

    # Combined logistic score
    logit = 2.0 * start_ratio + 1.5 * mins_factor - 1.0
    prob = 1.0 / (1.0 + np.exp(-logit))

    # Apply availability mask
    final_prob = prob * availability
    return pd.Series(np.clip(final_prob, 0.0, 1.0), index=features_df.index)
