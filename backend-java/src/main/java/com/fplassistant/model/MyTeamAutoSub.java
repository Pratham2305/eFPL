package com.fplassistant.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_team_auto_subs")
public class MyTeamAutoSub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameweek_id", nullable = false)
    private Gameweek gameweek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "element_in", nullable = false)
    private Player elementIn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "element_out", nullable = false)
    private Player elementOut;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public MyTeamAutoSub() {}

    public MyTeamAutoSub(Gameweek gameweek, Player elementIn, Player elementOut) {
        this.gameweek = gameweek;
        this.elementIn = elementIn;
        this.elementOut = elementOut;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Gameweek getGameweek() { return gameweek; }
    public void setGameweek(Gameweek gameweek) { this.gameweek = gameweek; }

    public Player getElementIn() { return elementIn; }
    public void setElementIn(Player elementIn) { this.elementIn = elementIn; }

    public Player getElementOut() { return elementOut; }
    public void setElementOut(Player elementOut) { this.elementOut = elementOut; }

    // Aliases
    public Player getPlayerIn() { return elementIn; }
    public void setPlayerIn(Player playerIn) { this.elementIn = playerIn; }

    public Player getPlayerOut() { return elementOut; }
    public void setPlayerOut(Player playerOut) { this.elementOut = playerOut; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final MyTeamAutoSub instance = new MyTeamAutoSub();
        public Builder gameweek(Gameweek v) { instance.setGameweek(v); return this; }
        public Builder playerIn(Player v) { instance.setElementIn(v); return this; }
        public Builder playerOut(Player v) { instance.setElementOut(v); return this; }
        public MyTeamAutoSub build() { return instance; }
    }
}
