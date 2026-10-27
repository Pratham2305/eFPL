package com.fplassistant.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers_log")
public class TransferLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameweek_id", nullable = false)
    private Gameweek gameweek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_in_id", nullable = false)
    private Player playerIn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_out_id", nullable = false)
    private Player playerOut;

    @Column(name = "price_in")
    private Integer priceIn;

    @Column(name = "price_out")
    private Integer priceOut;

    @Column(name = "transfer_time")
    private LocalDateTime transferTime;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TransferLog() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Gameweek getGameweek() { return gameweek; }
    public void setGameweek(Gameweek gameweek) { this.gameweek = gameweek; }

    public Player getPlayerIn() { return playerIn; }
    public void setPlayerIn(Player playerIn) { this.playerIn = playerIn; }

    public Player getPlayerOut() { return playerOut; }
    public void setPlayerOut(Player playerOut) { this.playerOut = playerOut; }

    public Integer getPriceIn() { return priceIn; }
    public void setPriceIn(Integer priceIn) { this.priceIn = priceIn; }

    public Integer getPriceOut() { return priceOut; }
    public void setPriceOut(Integer priceOut) { this.priceOut = priceOut; }

    public LocalDateTime getTransferTime() { return transferTime; }
    public void setTransferTime(LocalDateTime transferTime) { this.transferTime = transferTime; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist @PreUpdate
    void touchTimestamp() { this.updatedAt = LocalDateTime.now(); }
}
