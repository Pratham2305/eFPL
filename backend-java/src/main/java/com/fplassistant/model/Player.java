package com.fplassistant.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "players")
public class Player {

    @Id
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "second_name", length = 100)
    private String secondName;

    @Column(name = "web_name", nullable = false, length = 100)
    private String webName;

    @Column(name = "element_type", nullable = false)
    private Short elementType;

    @Column(name = "now_cost", nullable = false)
    private Integer nowCost;

    @Column(length = 5)
    private String status;

    @Column(name = "chance_of_playing_next")
    private Integer chanceOfPlayingNext;

    private String news;

    @Column(length = 100)
    private String photo;

    private BigDecimal form;

    @Column(name = "total_points")
    private Integer totalPoints;

    @Column(name = "points_per_game")
    private BigDecimal pointsPerGame;

    @Column(name = "selected_by_percent")
    private BigDecimal selectedByPercent;

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

    @Column(name = "transfers_in_event")
    private Integer transfersInEvent;

    @Column(name = "transfers_out_event")
    private Integer transfersOutEvent;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Player() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getSecondName() { return secondName; }
    public void setSecondName(String secondName) { this.secondName = secondName; }

    public String getWebName() { return webName; }
    public void setWebName(String webName) { this.webName = webName; }

    public Short getElementType() { return elementType; }
    public void setElementType(Short elementType) { this.elementType = elementType; }

    public Integer getNowCost() { return nowCost; }
    public void setNowCost(Integer nowCost) { this.nowCost = nowCost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getChanceOfPlayingNext() { return chanceOfPlayingNext; }
    public void setChanceOfPlayingNext(Integer chanceOfPlayingNext) { this.chanceOfPlayingNext = chanceOfPlayingNext; }

    public String getNews() { return news; }
    public void setNews(String news) { this.news = news; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public BigDecimal getForm() { return form; }
    public void setForm(BigDecimal form) { this.form = form; }

    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

    public BigDecimal getPointsPerGame() { return pointsPerGame; }
    public void setPointsPerGame(BigDecimal pointsPerGame) { this.pointsPerGame = pointsPerGame; }

    public BigDecimal getSelectedByPercent() { return selectedByPercent; }
    public void setSelectedByPercent(BigDecimal selectedByPercent) { this.selectedByPercent = selectedByPercent; }

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

    public Integer getTransfersInEvent() { return transfersInEvent; }
    public void setTransfersInEvent(Integer transfersInEvent) { this.transfersInEvent = transfersInEvent; }

    public Integer getTransfersOutEvent() { return transfersOutEvent; }
    public void setTransfersOutEvent(Integer transfersOutEvent) { this.transfersOutEvent = transfersOutEvent; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist @PreUpdate
    void touchTimestamp() { this.updatedAt = LocalDateTime.now(); }
}
