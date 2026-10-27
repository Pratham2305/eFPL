package com.fplassistant.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fixtures")
public class Fixture {

    @Id
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameweek_id")
    private Gameweek gameweek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_h_id", nullable = false)
    private Team teamHome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_id", nullable = false)
    private Team teamAway;

    @Column(name = "team_h_difficulty")
    private Short teamHomeDifficulty;

    @Column(name = "team_a_difficulty")
    private Short teamAwayDifficulty;

    @Column(name = "kickoff_time")
    private LocalDateTime kickoffTime;

    private Boolean finished;

    @Column(name = "team_h_score")
    private Integer teamHomeScore;

    @Column(name = "team_a_score")
    private Integer teamAwayScore;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Fixture() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Gameweek getGameweek() { return gameweek; }
    public void setGameweek(Gameweek gameweek) { this.gameweek = gameweek; }

    public Team getTeamHome() { return teamHome; }
    public void setTeamHome(Team teamHome) { this.teamHome = teamHome; }

    public Team getTeamAway() { return teamAway; }
    public void setTeamAway(Team teamAway) { this.teamAway = teamAway; }

    public Short getTeamHomeDifficulty() { return teamHomeDifficulty; }
    public void setTeamHomeDifficulty(Short teamHomeDifficulty) { this.teamHomeDifficulty = teamHomeDifficulty; }

    public Short getTeamAwayDifficulty() { return teamAwayDifficulty; }
    public void setTeamAwayDifficulty(Short teamAwayDifficulty) { this.teamAwayDifficulty = teamAwayDifficulty; }

    public LocalDateTime getKickoffTime() { return kickoffTime; }
    public void setKickoffTime(LocalDateTime kickoffTime) { this.kickoffTime = kickoffTime; }

    public Boolean getFinished() { return finished; }
    public void setFinished(Boolean finished) { this.finished = finished; }

    public Integer getTeamHomeScore() { return teamHomeScore; }
    public void setTeamHomeScore(Integer teamHomeScore) { this.teamHomeScore = teamHomeScore; }

    public Integer getTeamAwayScore() { return teamAwayScore; }
    public void setTeamAwayScore(Integer teamAwayScore) { this.teamAwayScore = teamAwayScore; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist @PreUpdate
    void touchTimestamp() { this.updatedAt = LocalDateTime.now(); }
}
