package com.fplassistant.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "short_name", nullable = false, length = 10)
    private String shortName;

    private Integer code;
    private Integer strength;

    @Column(name = "strength_overall_home")
    private Integer strengthOverallHome;

    @Column(name = "strength_overall_away")
    private Integer strengthOverallAway;

    @Column(name = "strength_attack_home")
    private Integer strengthAttackHome;

    @Column(name = "strength_attack_away")
    private Integer strengthAttackAway;

    @Column(name = "strength_defence_home")
    private Integer strengthDefenceHome;

    @Column(name = "strength_defence_away")
    private Integer strengthDefenceAway;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Team() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public Integer getStrength() { return strength; }
    public void setStrength(Integer strength) { this.strength = strength; }

    public Integer getStrengthOverallHome() { return strengthOverallHome; }
    public void setStrengthOverallHome(Integer strengthOverallHome) { this.strengthOverallHome = strengthOverallHome; }

    public Integer getStrengthOverallAway() { return strengthOverallAway; }
    public void setStrengthOverallAway(Integer strengthOverallAway) { this.strengthOverallAway = strengthOverallAway; }

    public Integer getStrengthAttackHome() { return strengthAttackHome; }
    public void setStrengthAttackHome(Integer strengthAttackHome) { this.strengthAttackHome = strengthAttackHome; }

    public Integer getStrengthAttackAway() { return strengthAttackAway; }
    public void setStrengthAttackAway(Integer strengthAttackAway) { this.strengthAttackAway = strengthAttackAway; }

    public Integer getStrengthDefenceHome() { return strengthDefenceHome; }
    public void setStrengthDefenceHome(Integer strengthDefenceHome) { this.strengthDefenceHome = strengthDefenceHome; }

    public Integer getStrengthDefenceAway() { return strengthDefenceAway; }
    public void setStrengthDefenceAway(Integer strengthDefenceAway) { this.strengthDefenceAway = strengthDefenceAway; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist @PreUpdate
    void touchTimestamp() { this.updatedAt = LocalDateTime.now(); }
}
