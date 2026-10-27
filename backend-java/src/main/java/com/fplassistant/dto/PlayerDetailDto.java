package com.fplassistant.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PlayerDetailDto {
    private PlayerSummaryDto summary;
    private BigDecimal xp3;
    private BigDecimal xp6;
    private List<UpcomingFixtureDto> upcomingFixtures;
    private List<HistoricalStatDto> historicalStats;
    private List<GameweekPredictionDto> predictionsByGameweek;

    public PlayerDetailDto() {}

    public PlayerSummaryDto getSummary() { return summary; }
    public void setSummary(PlayerSummaryDto summary) { this.summary = summary; }

    public BigDecimal getXp3() { return xp3; }
    public void setXp3(BigDecimal xp3) { this.xp3 = xp3; }

    public BigDecimal getXp6() { return xp6; }
    public void setXp6(BigDecimal xp6) { this.xp6 = xp6; }

    public List<UpcomingFixtureDto> getUpcomingFixtures() { return upcomingFixtures; }
    public void setUpcomingFixtures(List<UpcomingFixtureDto> upcomingFixtures) { this.upcomingFixtures = upcomingFixtures; }

    public List<HistoricalStatDto> getHistoricalStats() { return historicalStats; }
    public void setHistoricalStats(List<HistoricalStatDto> historicalStats) { this.historicalStats = historicalStats; }

    public List<GameweekPredictionDto> getPredictionsByGameweek() { return predictionsByGameweek; }
    public void setPredictionsByGameweek(List<GameweekPredictionDto> predictionsByGameweek) { this.predictionsByGameweek = predictionsByGameweek; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final PlayerDetailDto instance = new PlayerDetailDto();
        public Builder summary(PlayerSummaryDto v) { instance.setSummary(v); return this; }
        public Builder xp3(BigDecimal v) { instance.setXp3(v); return this; }
        public Builder xp6(BigDecimal v) { instance.setXp6(v); return this; }
        public Builder upcomingFixtures(List<UpcomingFixtureDto> v) { instance.setUpcomingFixtures(v); return this; }
        public Builder historicalStats(List<HistoricalStatDto> v) { instance.setHistoricalStats(v); return this; }
        public Builder predictionsByGameweek(List<GameweekPredictionDto> v) { instance.setPredictionsByGameweek(v); return this; }
        public PlayerDetailDto build() { return instance; }
    }

    public static class UpcomingFixtureDto {
        private Integer fixtureId;
        private Integer gameweekId;
        private String gameweekName;
        private Integer opponentTeamId;
        private String opponentName;
        private String opponentShortName;
        private boolean isHome;
        private Integer difficulty;
        private LocalDateTime kickoffTime;
        private boolean isDoubleGameweek;
        private boolean isBlankGameweek;

        public UpcomingFixtureDto() {}

        public Integer getFixtureId() { return fixtureId; }
        public void setFixtureId(Integer fixtureId) { this.fixtureId = fixtureId; }
        public Integer getGameweekId() { return gameweekId; }
        public void setGameweekId(Integer gameweekId) { this.gameweekId = gameweekId; }
        public String getGameweekName() { return gameweekName; }
        public void setGameweekName(String gameweekName) { this.gameweekName = gameweekName; }
        public Integer getOpponentTeamId() { return opponentTeamId; }
        public void setOpponentTeamId(Integer opponentTeamId) { this.opponentTeamId = opponentTeamId; }
        public String getOpponentName() { return opponentName; }
        public void setOpponentName(String opponentName) { this.opponentName = opponentName; }
        public String getOpponentShortName() { return opponentShortName; }
        public void setOpponentShortName(String opponentShortName) { this.opponentShortName = opponentShortName; }
        public boolean isHome() { return isHome; }
        public void setHome(boolean home) { isHome = home; }
        public Integer getDifficulty() { return difficulty; }
        public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
        public LocalDateTime getKickoffTime() { return kickoffTime; }
        public void setKickoffTime(LocalDateTime kickoffTime) { this.kickoffTime = kickoffTime; }
        public boolean isDoubleGameweek() { return isDoubleGameweek; }
        public void setDoubleGameweek(boolean doubleGameweek) { isDoubleGameweek = doubleGameweek; }
        public boolean isBlankGameweek() { return isBlankGameweek; }
        public void setBlankGameweek(boolean blankGameweek) { isBlankGameweek = blankGameweek; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final UpcomingFixtureDto o = new UpcomingFixtureDto();
            public Builder fixtureId(Integer v) { o.setFixtureId(v); return this; }
            public Builder gameweekId(Integer v) { o.setGameweekId(v); return this; }
            public Builder gameweekName(String v) { o.setGameweekName(v); return this; }
            public Builder opponentTeamId(Integer v) { o.setOpponentTeamId(v); return this; }
            public Builder opponentName(String v) { o.setOpponentName(v); return this; }
            public Builder opponentShortName(String v) { o.setOpponentShortName(v); return this; }
            public Builder isHome(boolean v) { o.setHome(v); return this; }
            public Builder difficulty(Integer v) { o.setDifficulty(v); return this; }
            public Builder kickoffTime(LocalDateTime v) { o.setKickoffTime(v); return this; }
            public Builder isDoubleGameweek(boolean v) { o.setDoubleGameweek(v); return this; }
            public Builder isBlankGameweek(boolean v) { o.setBlankGameweek(v); return this; }
            public UpcomingFixtureDto build() { return o; }
        }
    }

    public static class HistoricalStatDto {
        private Integer gameweekId;
        private String opponentShortName;
        private Boolean wasHome;
        private Integer minutes;
        private Integer totalPoints;
        private Integer goalsScored;
        private Integer assists;
        private Integer cleanSheets;
        private Integer goalsConceded;
        private Integer bonus;
        private Integer bps;
        private BigDecimal expectedGoals;
        private BigDecimal expectedAssists;
        private BigDecimal expectedGoalInvolvements;

        public HistoricalStatDto() {}

        public Integer getGameweekId() { return gameweekId; }
        public void setGameweekId(Integer gameweekId) { this.gameweekId = gameweekId; }
        public String getOpponentShortName() { return opponentShortName; }
        public void setOpponentShortName(String opponentShortName) { this.opponentShortName = opponentShortName; }
        public Boolean getWasHome() { return wasHome; }
        public void setWasHome(Boolean wasHome) { this.wasHome = wasHome; }
        public Integer getMinutes() { return minutes; }
        public void setMinutes(Integer minutes) { this.minutes = minutes; }
        public Integer getTotalPoints() { return totalPoints; }
        public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }
        public Integer getGoalsScored() { return goalsScored; }
        public void setGoalsScored(Integer goalsScored) { this.goalsScored = goalsScored; }
        public Integer getAssists() { return assists; }
        public void setAssists(Integer assists) { this.assists = assists; }
        public Integer getCleanSheets() { return cleanSheets; }
        public void setCleanSheets(Integer cleanSheets) { this.cleanSheets = cleanSheets; }
        public Integer getGoalsConceded() { return goalsConceded; }
        public void setGoalsConceded(Integer goalsConceded) { this.goalsConceded = goalsConceded; }
        public Integer getBonus() { return bonus; }
        public void setBonus(Integer bonus) { this.bonus = bonus; }
        public Integer getBps() { return bps; }
        public void setBps(Integer bps) { this.bps = bps; }
        public BigDecimal getExpectedGoals() { return expectedGoals; }
        public void setExpectedGoals(BigDecimal expectedGoals) { this.expectedGoals = expectedGoals; }
        public BigDecimal getExpectedAssists() { return expectedAssists; }
        public void setExpectedAssists(BigDecimal expectedAssists) { this.expectedAssists = expectedAssists; }
        public BigDecimal getExpectedGoalInvolvements() { return expectedGoalInvolvements; }
        public void setExpectedGoalInvolvements(BigDecimal expectedGoalInvolvements) { this.expectedGoalInvolvements = expectedGoalInvolvements; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final HistoricalStatDto o = new HistoricalStatDto();
            public Builder gameweekId(Integer v) { o.setGameweekId(v); return this; }
            public Builder opponentShortName(String v) { o.setOpponentShortName(v); return this; }
            public Builder wasHome(Boolean v) { o.setWasHome(v); return this; }
            public Builder minutes(Integer v) { o.setMinutes(v); return this; }
            public Builder totalPoints(Integer v) { o.setTotalPoints(v); return this; }
            public Builder goalsScored(Integer v) { o.setGoalsScored(v); return this; }
            public Builder assists(Integer v) { o.setAssists(v); return this; }
            public Builder cleanSheets(Integer v) { o.setCleanSheets(v); return this; }
            public Builder goalsConceded(Integer v) { o.setGoalsConceded(v); return this; }
            public Builder bonus(Integer v) { o.setBonus(v); return this; }
            public Builder bps(Integer v) { o.setBps(v); return this; }
            public Builder expectedGoals(BigDecimal v) { o.setExpectedGoals(v); return this; }
            public Builder expectedAssists(BigDecimal v) { o.setExpectedAssists(v); return this; }
            public Builder expectedGoalInvolvements(BigDecimal v) { o.setExpectedGoalInvolvements(v); return this; }
            public HistoricalStatDto build() { return o; }
        }
    }

    public static class GameweekPredictionDto {
        private Integer gameweekId;
        private BigDecimal expectedPoints;
        private BigDecimal floor;
        private BigDecimal ceiling;
        private BigDecimal startProbability;

        public GameweekPredictionDto() {}

        public Integer getGameweekId() { return gameweekId; }
        public void setGameweekId(Integer gameweekId) { this.gameweekId = gameweekId; }
        public BigDecimal getExpectedPoints() { return expectedPoints; }
        public void setExpectedPoints(BigDecimal expectedPoints) { this.expectedPoints = expectedPoints; }
        public BigDecimal getFloor() { return floor; }
        public void setFloor(BigDecimal floor) { this.floor = floor; }
        public BigDecimal getCeiling() { return ceiling; }
        public void setCeiling(BigDecimal ceiling) { this.ceiling = ceiling; }
        public BigDecimal getStartProbability() { return startProbability; }
        public void setStartProbability(BigDecimal startProbability) { this.startProbability = startProbability; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final GameweekPredictionDto o = new GameweekPredictionDto();
            public Builder gameweekId(Integer v) { o.setGameweekId(v); return this; }
            public Builder expectedPoints(BigDecimal v) { o.setExpectedPoints(v); return this; }
            public Builder floor(BigDecimal v) { o.setFloor(v); return this; }
            public Builder ceiling(BigDecimal v) { o.setCeiling(v); return this; }
            public Builder startProbability(BigDecimal v) { o.setStartProbability(v); return this; }
            public GameweekPredictionDto build() { return o; }
        }
    }
}
