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

    model_config = SettingsConfigDict(
        env_file=".env",
        case_sensitive=False,
        extra="ignore"
    )


settings = Settings()
