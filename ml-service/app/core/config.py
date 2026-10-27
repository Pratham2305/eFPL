from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Application settings loaded from environment variables."""

    # Database (SQLite default for zero-config local run, PostgreSQL in Docker)
    database_url: str = "sqlite:///./fpl.db"

    # FPL API
    fpl_api_base_url: str = "https://fantasy.premierleague.com/api"
    fpl_team_id: int = 0

    # Service
    app_name: str = "FPL ML Service"
    debug: bool = False

    # Season scoring rules (FPL scoring system - configurable per season)
    default_model_version: str = "baseline-v1"
    pts_appearance_under_60: float = 1.0
    pts_appearance_60_plus: float = 2.0
    # Goals by element_type (1: GK, 2: DEF, 3: MID, 4: FWD)
    pts_goal: dict[int, float] = {1: 6.0, 2: 6.0, 3: 5.0, 4: 4.0}
    pts_assist: float = 3.0
    # Clean sheet by element_type
    pts_clean_sheet: dict[int, float] = {1: 4.0, 2: 4.0, 3: 1.0, 4: 0.0}
    # Goals conceded penalty (per 2 goals conceded, element_type 1 & 2)
    pts_conceded_per_2: dict[int, float] = {1: -1.0, 2: -1.0, 3: 0.0, 4: 0.0}
    pts_save_per_3: float = 1.0

    # Multipliers
    fdr_multipliers: dict[int, float] = {1: 1.25, 2: 1.15, 3: 1.0, 4: 0.85, 5: 0.70}
    home_multiplier: float = 1.10
    away_multiplier: float = 0.90

    model_config = SettingsConfigDict(
        env_file=".env",
        case_sensitive=False,
        extra="ignore"
    )


settings = Settings()
