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

    @Column(name = "gameweek_rank")
    private Integer gameweekRank;

    @Column(name = "event_transfers")
    private Integer eventTransfers;

    @Column(name = "event_transfers_cost")
    private Integer eventTransfersCost;

    @Column(name = "points_on_bench")
    private Integer pointsOnBench;

    @Column(name = "active_chip", length = 30)
    private String activeChip;

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

    public Integer getGameweekRank() { return gameweekRank; }
    public void setGameweekRank(Integer gameweekRank) { this.gameweekRank = gameweekRank; }

    // Aliases for rank
    public Integer getRank() { return gameweekRank; }
    public void setRank(Integer rank) { this.gameweekRank = rank; }
    public void setRankSort(Integer rankSort) {}

    public Integer getGameweekNetPoints() {
        if (gameweekPoints == null) return null;
        int cost = eventTransfersCost != null ? eventTransfersCost : 0;
        return gameweekPoints - cost;
    }
    public void setGameweekNetPoints(Integer netPoints) {}

    public Integer getEventTransfers() { return eventTransfers; }
    public void setEventTransfers(Integer eventTransfers) { this.eventTransfers = eventTransfers; }

    public Integer getEventTransfersCost() { return eventTransfersCost; }
    public void setEventTransfersCost(Integer eventTransfersCost) { this.eventTransfersCost = eventTransfersCost; }

    public Integer getPointsOnBench() { return pointsOnBench; }
    public void setPointsOnBench(Integer pointsOnBench) { this.pointsOnBench = pointsOnBench; }

    public String getActiveChip() { return activeChip; }
    public void setActiveChip(String activeChip) { this.activeChip = activeChip; }

    public Boolean getTripleCaptainActive() { return "3xc".equalsIgnoreCase(activeChip); }
    public void setTripleCaptainActive(boolean active) { if (active) this.tripleCaptainAvailable = false; }
    public void setTripleCaptainUsed(boolean used) { if (used) this.tripleCaptainAvailable = false; }

    public Boolean getBenchBoostActive() { return "bboost".equalsIgnoreCase(activeChip); }
    public void setBenchBoostActive(boolean active) { if (active) this.benchBoostAvailable = false; }
    public void setBenchBoostUsed(boolean used) { if (used) this.benchBoostAvailable = false; }

    public Boolean getFreeHitActive() { return "freehit".equalsIgnoreCase(activeChip); }
    public void setFreeHitActive(boolean active) { if (active) this.freeHitAvailable = false; }
    public void setFreeHitUsed(boolean used) { if (used) this.freeHitAvailable = false; }

    public Boolean getWildcardActive() { return "wildcard".equalsIgnoreCase(activeChip); }
    public void setWildcardActive(boolean active) { if (active) this.wildcardAvailable = false; }
    public void setWildcardUsed(boolean used) { if (used) this.wildcardAvailable = false; }

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
