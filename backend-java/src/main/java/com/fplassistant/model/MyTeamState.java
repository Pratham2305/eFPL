package com.fplassistant.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_team_state")
public class MyTeamState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameweek_id", nullable = false)
    private Gameweek gameweek;

    @Column(name = "overall_rank")
    private Integer overallRank;

    @Column(name = "overall_points")
    private Integer overallPoints;

    @Column(name = "gameweek_points")
    private Integer gameweekPoints;

    private Integer bank = 0;

    @Column(name = "team_value")
    private Integer teamValue = 0;

    @Column(name = "free_transfers")
    private Integer freeTransfers = 1;

    @Column(name = "wildcard_available")
    private Boolean wildcardAvailable = true;

    @Column(name = "free_hit_available")
    private Boolean freeHitAvailable = true;

    @Column(name = "bench_boost_available")
    private Boolean benchBoostAvailable = true;

    @Column(name = "triple_captain_available")
    private Boolean tripleCaptainAvailable = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public MyTeamState() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Gameweek getGameweek() { return gameweek; }
    public void setGameweek(Gameweek gameweek) { this.gameweek = gameweek; }

    public Integer getOverallRank() { return overallRank; }
    public void setOverallRank(Integer overallRank) { this.overallRank = overallRank; }

    public Integer getOverallPoints() { return overallPoints; }
    public void setOverallPoints(Integer overallPoints) { this.overallPoints = overallPoints; }

    public Integer getGameweekPoints() { return gameweekPoints; }
    public void setGameweekPoints(Integer gameweekPoints) { this.gameweekPoints = gameweekPoints; }

    public Integer getBank() { return bank; }
    public void setBank(Integer bank) { this.bank = bank; }

    public Integer getTeamValue() { return teamValue; }
    public void setTeamValue(Integer teamValue) { this.teamValue = teamValue; }

    public Integer getFreeTransfers() { return freeTransfers; }
    public void setFreeTransfers(Integer freeTransfers) { this.freeTransfers = freeTransfers; }

    public Boolean getWildcardAvailable() { return wildcardAvailable; }
    public void setWildcardAvailable(Boolean wildcardAvailable) { this.wildcardAvailable = wildcardAvailable; }

    public Boolean getFreeHitAvailable() { return freeHitAvailable; }
    public void setFreeHitAvailable(Boolean freeHitAvailable) { this.freeHitAvailable = freeHitAvailable; }

    public Boolean getBenchBoostAvailable() { return benchBoostAvailable; }
    public void setBenchBoostAvailable(Boolean benchBoostAvailable) { this.benchBoostAvailable = benchBoostAvailable; }

    public Boolean getTripleCaptainAvailable() { return tripleCaptainAvailable; }
    public void setTripleCaptainAvailable(Boolean tripleCaptainAvailable) { this.tripleCaptainAvailable = tripleCaptainAvailable; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist @PreUpdate
    void touchTimestamp() { this.updatedAt = LocalDateTime.now(); }
}
