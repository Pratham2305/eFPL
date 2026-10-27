package com.fplassistant.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for GET /api/fixtures
 */
public class FixtureDto {

    private Integer id;
    private Integer gameweekId;
    private String homeTeamName;
    private String homeTeamShortName;
    private Integer homeTeamCode;
    private String homeBadgeUrl;
    private String awayTeamName;
    private String awayTeamShortName;
    private Integer awayTeamCode;
    private String awayBadgeUrl;
    private Short homeDifficulty;
    private Short awayDifficulty;
    private LocalDateTime kickoffTime;
    private Boolean finished;
    private Integer homeScore;
    private Integer awayScore;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getGameweekId() { return gameweekId; }
    public void setGameweekId(Integer gameweekId) { this.gameweekId = gameweekId; }

    public String getHomeTeamName() { return homeTeamName; }
    public void setHomeTeamName(String homeTeamName) { this.homeTeamName = homeTeamName; }

    public String getHomeTeamShortName() { return homeTeamShortName; }
    public void setHomeTeamShortName(String homeTeamShortName) { this.homeTeamShortName = homeTeamShortName; }

    public Integer getHomeTeamCode() { return homeTeamCode; }
    public void setHomeTeamCode(Integer homeTeamCode) { this.homeTeamCode = homeTeamCode; }

    public String getHomeBadgeUrl() { return homeBadgeUrl; }
    public void setHomeBadgeUrl(String homeBadgeUrl) { this.homeBadgeUrl = homeBadgeUrl; }

    public String getAwayTeamName() { return awayTeamName; }
    public void setAwayTeamName(String awayTeamName) { this.awayTeamName = awayTeamName; }

    public String getAwayTeamShortName() { return awayTeamShortName; }
    public void setAwayTeamShortName(String awayTeamShortName) { this.awayTeamShortName = awayTeamShortName; }

    public Integer getAwayTeamCode() { return awayTeamCode; }
    public void setAwayTeamCode(Integer awayTeamCode) { this.awayTeamCode = awayTeamCode; }

    public String getAwayBadgeUrl() { return awayBadgeUrl; }
    public void setAwayBadgeUrl(String awayBadgeUrl) { this.awayBadgeUrl = awayBadgeUrl; }

    public Short getHomeDifficulty() { return homeDifficulty; }
    public void setHomeDifficulty(Short homeDifficulty) { this.homeDifficulty = homeDifficulty; }

    public Short getAwayDifficulty() { return awayDifficulty; }
    public void setAwayDifficulty(Short awayDifficulty) { this.awayDifficulty = awayDifficulty; }

    public LocalDateTime getKickoffTime() { return kickoffTime; }
    public void setKickoffTime(LocalDateTime kickoffTime) { this.kickoffTime = kickoffTime; }

    public Boolean getFinished() { return finished; }
    public void setFinished(Boolean finished) { this.finished = finished; }

    public Integer getHomeScore() { return homeScore; }
    public void setHomeScore(Integer homeScore) { this.homeScore = homeScore; }

    public Integer getAwayScore() { return awayScore; }
    public void setAwayScore(Integer awayScore) { this.awayScore = awayScore; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final FixtureDto instance = new FixtureDto();
        public Builder id(Integer v) { instance.setId(v); return this; }
        public Builder gameweekId(Integer v) { instance.setGameweekId(v); return this; }
        public Builder homeTeamName(String v) { instance.setHomeTeamName(v); return this; }
        public Builder homeTeamShortName(String v) { instance.setHomeTeamShortName(v); return this; }
        public Builder homeTeamCode(Integer v) { instance.setHomeTeamCode(v); return this; }
        public Builder homeBadgeUrl(String v) { instance.setHomeBadgeUrl(v); return this; }
        public Builder awayTeamName(String v) { instance.setAwayTeamName(v); return this; }
        public Builder awayTeamShortName(String v) { instance.setAwayTeamShortName(v); return this; }
        public Builder awayTeamCode(Integer v) { instance.setAwayTeamCode(v); return this; }
        public Builder awayBadgeUrl(String v) { instance.setAwayBadgeUrl(v); return this; }
        public Builder homeDifficulty(Short v) { instance.setHomeDifficulty(v); return this; }
        public Builder awayDifficulty(Short v) { instance.setAwayDifficulty(v); return this; }
        public Builder kickoffTime(LocalDateTime v) { instance.setKickoffTime(v); return this; }
        public Builder finished(Boolean v) { instance.setFinished(v); return this; }
        public Builder homeScore(Integer v) { instance.setHomeScore(v); return this; }
        public Builder awayScore(Integer v) { instance.setAwayScore(v); return this; }
        public FixtureDto build() { return instance; }
    }
}
