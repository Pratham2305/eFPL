package com.fplassistant.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps a single fixture from https://fantasy.premierleague.com/api/fixtures/
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FplFixtureResponse {

    private Integer id;

    /** Gameweek number (null for unscheduled fixtures) */
    private Integer event;

    @JsonProperty("team_h")
    private Integer teamH;

    @JsonProperty("team_a")
    private Integer teamA;

    @JsonProperty("team_h_difficulty")
    private Integer teamHDifficulty;

    @JsonProperty("team_a_difficulty")
    private Integer teamADifficulty;

    @JsonProperty("kickoff_time")
    private String kickoffTime;

    private Boolean finished;

    @JsonProperty("team_h_score")
    private Integer teamHScore;

    @JsonProperty("team_a_score")
    private Integer teamAScore;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEvent() { return event; }
    public void setEvent(Integer event) { this.event = event; }

    public Integer getTeamH() { return teamH; }
    public void setTeamH(Integer teamH) { this.teamH = teamH; }

    public Integer getTeamA() { return teamA; }
    public void setTeamA(Integer teamA) { this.teamA = teamA; }

    public Integer getTeamHDifficulty() { return teamHDifficulty; }
    public void setTeamHDifficulty(Integer teamHDifficulty) { this.teamHDifficulty = teamHDifficulty; }

    public Integer getTeamADifficulty() { return teamADifficulty; }
    public void setTeamADifficulty(Integer teamADifficulty) { this.teamADifficulty = teamADifficulty; }

    public String getKickoffTime() { return kickoffTime; }
    public void setKickoffTime(String kickoffTime) { this.kickoffTime = kickoffTime; }

    public Boolean getFinished() { return finished; }
    public void setFinished(Boolean finished) { this.finished = finished; }

    public Integer getTeamHScore() { return teamHScore; }
    public void setTeamHScore(Integer teamHScore) { this.teamHScore = teamHScore; }

    public Integer getTeamAScore() { return teamAScore; }
    public void setTeamAScore(Integer teamAScore) { this.teamAScore = teamAScore; }
}
