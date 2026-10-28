package com.fplassistant.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers_log")
public class TransferLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "entry_id")
    private Integer entryId;

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

    public Integer getEntryId() { return entryId; }
    public void setEntryId(Integer entryId) { this.entryId = entryId; }

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

    // Aliases
    public Integer getPlayerInCost() { return priceIn; }
    public void setPlayerInCost(Integer playerInCost) { this.priceIn = playerInCost; }

    public Integer getPlayerOutCost() { return priceOut; }
    public void setPlayerOutCost(Integer playerOutCost) { this.priceOut = playerOutCost; }

    public LocalDateTime getTransferTime() { return transferTime; }
    public void setTransferTime(LocalDateTime transferTime) { this.transferTime = transferTime; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist @PreUpdate
    void touchTimestamp() { this.updatedAt = LocalDateTime.now(); }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final TransferLog instance = new TransferLog();
        public Builder id(Integer v) { instance.setId(v); return this; }
        public Builder entryId(Integer v) { instance.setEntryId(v); return this; }
        public Builder gameweek(Gameweek v) { instance.setGameweek(v); return this; }
        public Builder playerIn(Player v) { instance.setPlayerIn(v); return this; }
        public Builder playerOut(Player v) { instance.setPlayerOut(v); return this; }
        public Builder priceIn(Integer v) { instance.setPriceIn(v); return this; }
        public Builder priceOut(Integer v) { instance.setPriceOut(v); return this; }
        public Builder playerInCost(Integer v) { instance.setPriceIn(v); return this; }
        public Builder playerOutCost(Integer v) { instance.setPriceOut(v); return this; }
        public Builder transferTime(LocalDateTime v) { instance.setTransferTime(v); return this; }
        public TransferLog build() { return instance; }
    }
}
