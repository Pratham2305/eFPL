package com.fplassistant.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_gameweek_stats")
public class PlayerGameweekStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameweek_id", nullable = false)
    private Gameweek gameweek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id")
    private Fixture fixture;

    @Column(name = "total_points")
    private Integer totalPoints;

    private Integer minutes;

    @Column(name = "goals_scored")
    private Integer goalsScored;

    private Integer assists;

    @Column(name = "clean_sheets")
    private Integer cleanSheets;

    @Column(name = "goals_conceded")
    private Integer goalsConceded;

    @Column(name = "own_goals")
    private Integer ownGoals;

    @Column(name = "penalties_saved")
    private Integer penaltiesSaved;

    @Column(name = "penalties_missed")
    private Integer penaltiesMissed;

    @Column(name = "yellow_cards")
    private Integer yellowCards;

    @Column(name = "red_cards")
    private Integer redCards;

    private Integer saves;
    private Integer bonus;
    private Integer bps;
    private BigDecimal influence;
    private BigDecimal creativity;
    private BigDecimal threat;

    @Column(name = "ict_index")
    private BigDecimal ictIndex;

    @Column(name = "expected_goals")
    private BigDecimal expectedGoals;

    @Column(name = "expected_assists")
    private BigDecimal expectedAssists;

    @Column(name = "expected_goal_involvements")
    private BigDecimal expectedGoalInvolvements;

    @Column(name = "expected_goals_conceded")
    private BigDecimal expectedGoalsConceded;

    @Column(name = "player_value")
    private Integer value;
    private Integer selected;

    @Column(name = "transfers_in")
    private Integer transfersIn;

    @Column(name = "transfers_out")
    private Integer transfersOut;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PlayerGameweekStats() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Gameweek getGameweek() { return gameweek; }
    public void setGameweek(Gameweek gameweek) { this.gameweek = gameweek; }

    public Fixture getFixture() { return fixture; }
    public void setFixture(Fixture fixture) { this.fixture = fixture; }

    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

    public Integer getMinutes() { return minutes; }
    public void setMinutes(Integer minutes) { this.minutes = minutes; }

    public Integer getGoalsScored() { return goalsScored; }
    public void setGoalsScored(Integer goalsScored) { this.goalsScored = goalsScored; }

    public Integer getAssists() { return assists; }
    public void setAssists(Integer assists) { this.assists = assists; }

    public Integer getCleanSheets() { return cleanSheets; }
    public void setCleanSheets(Integer cleanSheets) { this.cleanSheets = cleanSheets; }

    public Integer getGoalsConceded() { return goalsConceded; }
    public void setGoalsConceded(Integer goalsConceded) { this.goalsConceded = goalsConceded; }

    public Integer getOwnGoals() { return ownGoals; }
    public void setOwnGoals(Integer ownGoals) { this.ownGoals = ownGoals; }

    public Integer getPenaltiesSaved() { return penaltiesSaved; }
    public void setPenaltiesSaved(Integer penaltiesSaved) { this.penaltiesSaved = penaltiesSaved; }

    public Integer getPenaltiesMissed() { return penaltiesMissed; }
    public void setPenaltiesMissed(Integer penaltiesMissed) { this.penaltiesMissed = penaltiesMissed; }

    public Integer getYellowCards() { return yellowCards; }
    public void setYellowCards(Integer yellowCards) { this.yellowCards = yellowCards; }

    public Integer getRedCards() { return redCards; }
    public void setRedCards(Integer redCards) { this.redCards = redCards; }

    public Integer getSaves() { return saves; }
    public void setSaves(Integer saves) { this.saves = saves; }

    public Integer getBonus() { return bonus; }
    public void setBonus(Integer bonus) { this.bonus = bonus; }

    public Integer getBps() { return bps; }
    public void setBps(Integer bps) { this.bps = bps; }

    public BigDecimal getInfluence() { return influence; }
    public void setInfluence(BigDecimal influence) { this.influence = influence; }

    public BigDecimal getCreativity() { return creativity; }
    public void setCreativity(BigDecimal creativity) { this.creativity = creativity; }

    public BigDecimal getThreat() { return threat; }
    public void setThreat(BigDecimal threat) { this.threat = threat; }

    public BigDecimal getIctIndex() { return ictIndex; }
    public void setIctIndex(BigDecimal ictIndex) { this.ictIndex = ictIndex; }

    public BigDecimal getExpectedGoals() { return expectedGoals; }
    public void setExpectedGoals(BigDecimal expectedGoals) { this.expectedGoals = expectedGoals; }

    public BigDecimal getExpectedAssists() { return expectedAssists; }
    public void setExpectedAssists(BigDecimal expectedAssists) { this.expectedAssists = expectedAssists; }

    public BigDecimal getExpectedGoalInvolvements() { return expectedGoalInvolvements; }
    public void setExpectedGoalInvolvements(BigDecimal expectedGoalInvolvements) { this.expectedGoalInvolvements = expectedGoalInvolvements; }

    public BigDecimal getExpectedGoalsConceded() { return expectedGoalsConceded; }
    public void setExpectedGoalsConceded(BigDecimal expectedGoalsConceded) { this.expectedGoalsConceded = expectedGoalsConceded; }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }

    public Integer getSelected() { return selected; }
    public void setSelected(Integer selected) { this.selected = selected; }

    public Integer getTransfersIn() { return transfersIn; }
    public void setTransfersIn(Integer transfersIn) { this.transfersIn = transfersIn; }

    public Integer getTransfersOut() { return transfersOut; }
    public void setTransfersOut(Integer transfersOut) { this.transfersOut = transfersOut; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist @PreUpdate
    void touchTimestamp() { this.updatedAt = LocalDateTime.now(); }
}
