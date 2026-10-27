package com.fplassistant.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gameweeks")
public class Gameweek {

    @Id
    private Integer id;

    @Column(length = 50)
    private String name;

    @Column(name = "deadline_time")
    private LocalDateTime deadlineTime;

    @Column(name = "average_entry_score")
    private Integer averageEntryScore;

    @Column(name = "highest_score")
    private Integer highestScore;

    @Column(name = "is_current")
    private Boolean isCurrent;

    @Column(name = "is_next")
    private Boolean isNext;

    @Column(name = "is_previous")
    private Boolean isPrevious;

    private Boolean finished;

    @Column(name = "data_checked")
    private Boolean dataChecked;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Gameweek() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getDeadlineTime() { return deadlineTime; }
    public void setDeadlineTime(LocalDateTime deadlineTime) { this.deadlineTime = deadlineTime; }

    public Integer getAverageEntryScore() { return averageEntryScore; }
    public void setAverageEntryScore(Integer averageEntryScore) { this.averageEntryScore = averageEntryScore; }

    public Integer getHighestScore() { return highestScore; }
    public void setHighestScore(Integer highestScore) { this.highestScore = highestScore; }

    public Boolean getIsCurrent() { return isCurrent; }
    public void setIsCurrent(Boolean isCurrent) { this.isCurrent = isCurrent; }

    public Boolean getIsNext() { return isNext; }
    public void setIsNext(Boolean isNext) { this.isNext = isNext; }

    public Boolean getIsPrevious() { return isPrevious; }
    public void setIsPrevious(Boolean isPrevious) { this.isPrevious = isPrevious; }

    public Boolean getFinished() { return finished; }
    public void setFinished(Boolean finished) { this.finished = finished; }

    public Boolean getDataChecked() { return dataChecked; }
    public void setDataChecked(Boolean dataChecked) { this.dataChecked = dataChecked; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist @PreUpdate
    void touchTimestamp() { this.updatedAt = LocalDateTime.now(); }
}
