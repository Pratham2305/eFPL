"""SQLAlchemy ORM models mirroring the Postgres schema."""

from sqlalchemy import (
    Column, Integer, SmallInteger, String, Text, Boolean, Numeric, DateTime,
    ForeignKey, UniqueConstraint, JSON
)
from sqlalchemy.sql import func

from app.core.database import Base


class Team(Base):
    __tablename__ = "teams"

    id = Column(Integer, primary_key=True)
    name = Column(String(100), nullable=False)
    short_name = Column(String(10), nullable=False)
    code = Column(Integer)
    strength = Column(Integer)
    strength_overall_home = Column(Integer)
    strength_overall_away = Column(Integer)
    strength_attack_home = Column(Integer)
    strength_attack_away = Column(Integer)
    strength_defence_home = Column(Integer)
    strength_defence_away = Column(Integer)
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())


class Player(Base):
    __tablename__ = "players"

    id = Column(Integer, primary_key=True)
    team_id = Column(Integer, ForeignKey("teams.id"), nullable=False)
    first_name = Column(String(100))
    second_name = Column(String(100))
    web_name = Column(String(100), nullable=False)
    element_type = Column(SmallInteger, nullable=False)
    now_cost = Column(Integer, nullable=False)
    status = Column(String(5))
    chance_of_playing_next = Column(Integer)
    news = Column(Text)
    photo = Column(String(100))
    form = Column(Numeric(5, 2))
    total_points = Column(Integer, default=0)
    points_per_game = Column(Numeric(5, 2))
    selected_by_percent = Column(Numeric(6, 2))
    minutes = Column(Integer, default=0)
    goals_scored = Column(Integer, default=0)
    assists = Column(Integer, default=0)
    clean_sheets = Column(Integer, default=0)
    goals_conceded = Column(Integer, default=0)
    own_goals = Column(Integer, default=0)
    penalties_saved = Column(Integer, default=0)
    penalties_missed = Column(Integer, default=0)
    yellow_cards = Column(Integer, default=0)
    red_cards = Column(Integer, default=0)
    saves = Column(Integer, default=0)
    bonus = Column(Integer, default=0)
    bps = Column(Integer, default=0)
    influence = Column(Numeric(7, 2))
    creativity = Column(Numeric(7, 2))
    threat = Column(Numeric(7, 2))
    ict_index = Column(Numeric(7, 2))
    expected_goals = Column(Numeric(7, 3))
    expected_assists = Column(Numeric(7, 3))
    expected_goal_involvements = Column(Numeric(7, 3))
    expected_goals_conceded = Column(Numeric(7, 3))
    transfers_in_event = Column(Integer, default=0)
    transfers_out_event = Column(Integer, default=0)
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())


class Gameweek(Base):
    __tablename__ = "gameweeks"

    id = Column(Integer, primary_key=True)
    name = Column(String(50))
    deadline_time = Column(DateTime)
    average_entry_score = Column(Integer)
    highest_score = Column(Integer)
    is_current = Column(Boolean, default=False)
    is_next = Column(Boolean, default=False)
    is_previous = Column(Boolean, default=False)
    finished = Column(Boolean, default=False)
    data_checked = Column(Boolean, default=False)
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())


class Fixture(Base):
    __tablename__ = "fixtures"

    id = Column(Integer, primary_key=True)
    gameweek_id = Column(Integer, ForeignKey("gameweeks.id"))
    team_h_id = Column(Integer, ForeignKey("teams.id"), nullable=False)
    team_a_id = Column(Integer, ForeignKey("teams.id"), nullable=False)
    team_h_difficulty = Column(SmallInteger)
    team_a_difficulty = Column(SmallInteger)
    kickoff_time = Column(DateTime)
    finished = Column(Boolean, default=False)
    team_h_score = Column(Integer)
    team_a_score = Column(Integer)
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())


class PlayerGameweekStats(Base):
    __tablename__ = "player_gameweek_stats"

    id = Column(Integer, primary_key=True, autoincrement=True)
    player_id = Column(Integer, ForeignKey("players.id"), nullable=False)
    gameweek_id = Column(Integer, ForeignKey("gameweeks.id"), nullable=False)
    fixture_id = Column(Integer, ForeignKey("fixtures.id"))
    total_points = Column(Integer, default=0)
    minutes = Column(Integer, default=0)
    goals_scored = Column(Integer, default=0)
    assists = Column(Integer, default=0)
    clean_sheets = Column(Integer, default=0)
    goals_conceded = Column(Integer, default=0)
    own_goals = Column(Integer, default=0)
    penalties_saved = Column(Integer, default=0)
    penalties_missed = Column(Integer, default=0)
    yellow_cards = Column(Integer, default=0)
    red_cards = Column(Integer, default=0)
    saves = Column(Integer, default=0)
    bonus = Column(Integer, default=0)
    bps = Column(Integer, default=0)
    influence = Column(Numeric(7, 2))
    creativity = Column(Numeric(7, 2))
    threat = Column(Numeric(7, 2))
    ict_index = Column(Numeric(7, 2))
    expected_goals = Column(Numeric(7, 3))
    expected_assists = Column(Numeric(7, 3))
    expected_goal_involvements = Column(Numeric(7, 3))
    expected_goals_conceded = Column(Numeric(7, 3))
    value = Column(Integer)
    selected = Column(Integer)
    transfers_in = Column(Integer, default=0)
    transfers_out = Column(Integer, default=0)
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    __table_args__ = (
        UniqueConstraint("player_id", "gameweek_id", "fixture_id"),
    )


class MyTeam(Base):
    __tablename__ = "my_team"

    id = Column(Integer, primary_key=True, autoincrement=True)
    gameweek_id = Column(Integer, ForeignKey("gameweeks.id"), nullable=False)
    player_id = Column(Integer, ForeignKey("players.id"), nullable=False)
    position = Column(SmallInteger, nullable=False)
    multiplier = Column(SmallInteger, nullable=False, default=1)
    is_captain = Column(Boolean, default=False)
    is_vice_captain = Column(Boolean, default=False)
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    __table_args__ = (
        UniqueConstraint("gameweek_id", "position"),
    )


class MyTeamState(Base):
    __tablename__ = "my_team_state"

    id = Column(Integer, primary_key=True, autoincrement=True)
    gameweek_id = Column(Integer, ForeignKey("gameweeks.id"), nullable=False, unique=True)
    overall_rank = Column(Integer)
    overall_points = Column(Integer)
    gameweek_points = Column(Integer)
    bank = Column(Integer, default=0)
    team_value = Column(Integer, default=0)
    free_transfers = Column(Integer, default=1)
    wildcard_available = Column(Boolean, default=True)
    free_hit_available = Column(Boolean, default=True)
    bench_boost_available = Column(Boolean, default=True)
    triple_captain_available = Column(Boolean, default=True)
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())


class TransferLog(Base):
    __tablename__ = "transfers_log"

    id = Column(Integer, primary_key=True, autoincrement=True)
    gameweek_id = Column(Integer, ForeignKey("gameweeks.id"), nullable=False)
    player_in_id = Column(Integer, ForeignKey("players.id"), nullable=False)
    player_out_id = Column(Integer, ForeignKey("players.id"), nullable=False)
    price_in = Column(Integer)
    price_out = Column(Integer)
    transfer_time = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())


class Prediction(Base):
    __tablename__ = "predictions"

    id = Column(Integer, primary_key=True, autoincrement=True)
    player_id = Column(Integer, ForeignKey("players.id"), nullable=False)
    gameweek_id = Column(Integer, ForeignKey("gameweeks.id"), nullable=False)
    model_version = Column(String(50), nullable=False)
    expected_points = Column(Numeric(6, 2))
    floor = Column(Numeric(6, 2))
    ceiling = Column(Numeric(6, 2))
    start_probability = Column(Numeric(4, 3))
    created_at = Column(DateTime, server_default=func.now())

    __table_args__ = (
        UniqueConstraint("player_id", "gameweek_id", "model_version"),
    )


class Recommendation(Base):
    __tablename__ = "recommendations"

    id = Column(Integer, primary_key=True, autoincrement=True)
    gameweek_id = Column(Integer, ForeignKey("gameweeks.id"), nullable=False)
    type = Column(String(30), nullable=False)
    payload = Column(JSON)
    reasoning = Column(Text)
    confidence = Column(Numeric(4, 3))
    created_at = Column(DateTime, server_default=func.now())
