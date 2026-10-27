package com.fplassistant.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_team")
public class MyTeamPick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameweek_id", nullable = false)
    private Gameweek gameweek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private Short position;

    @Column(nullable = false)
    private Short multiplier = 1;

    @Column(name = "is_captain")
    private Boolean isCaptain = false;

    @Column(name = "is_vice_captain")
    private Boolean isViceCaptain = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public MyTeamPick() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Gameweek getGameweek() { return gameweek; }
    public void setGameweek(Gameweek gameweek) { this.gameweek = gameweek; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Short getPosition() { return position; }
    public void setPosition(Short position) { this.position = position; }

    public Short getMultiplier() { return multiplier; }
    public void setMultiplier(Short multiplier) { this.multiplier = multiplier; }

    public Boolean getIsCaptain() { return isCaptain; }
    public void setIsCaptain(Boolean isCaptain) { this.isCaptain = isCaptain; }

    public Boolean getIsViceCaptain() { return isViceCaptain; }
    public void setIsViceCaptain(Boolean isViceCaptain) { this.isViceCaptain = isViceCaptain; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final MyTeamPick instance = new MyTeamPick();
        public Builder gameweek(Gameweek v) { instance.setGameweek(v); return this; }
        public Builder player(Player v) { instance.setPlayer(v); return this; }
        public Builder position(Short v) { instance.setPosition(v); return this; }
        public Builder multiplier(Short v) { instance.setMultiplier(v); return this; }
        public Builder isCaptain(Boolean v) { instance.setIsCaptain(v); return this; }
        public Builder isViceCaptain(Boolean v) { instance.setIsViceCaptain(v); return this; }
        public MyTeamPick build() { return instance; }
    }

    @PrePersist @PreUpdate
    void touchTimestamp() { this.updatedAt = LocalDateTime.now(); }
}
