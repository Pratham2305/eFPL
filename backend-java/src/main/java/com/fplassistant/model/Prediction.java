package com.fplassistant.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "predictions")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameweek_id", nullable = false)
    private Gameweek gameweek;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Column(name = "expected_points")
    private BigDecimal expectedPoints;

    private BigDecimal floor;
    private BigDecimal ceiling;

    @Column(name = "start_probability")
    private BigDecimal startProbability;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Prediction() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Gameweek getGameweek() { return gameweek; }
    public void setGameweek(Gameweek gameweek) { this.gameweek = gameweek; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public BigDecimal getExpectedPoints() { return expectedPoints; }
    public void setExpectedPoints(BigDecimal expectedPoints) { this.expectedPoints = expectedPoints; }

    public BigDecimal getFloor() { return floor; }
    public void setFloor(BigDecimal floor) { this.floor = floor; }

    public BigDecimal getCeiling() { return ceiling; }
    public void setCeiling(BigDecimal ceiling) { this.ceiling = ceiling; }

    public BigDecimal getStartProbability() { return startProbability; }
    public void setStartProbability(BigDecimal startProbability) { this.startProbability = startProbability; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    void touchTimestamp() { this.createdAt = LocalDateTime.now(); }
}
