package com.fplassistant.dto;

import java.time.LocalDateTime;

public class GameweekDto {
    private Integer id;
    private String name;
    private LocalDateTime deadlineTime;
    private Integer averageEntryScore;
    private Integer highestScore;
    private Boolean isCurrent;
    private Boolean isNext;
    private Boolean isPrevious;
    private Boolean finished;
    private Boolean dataChecked;
    private String phase;
    private Boolean isDefaultHome;

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

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }

    public Boolean getIsDefaultHome() { return isDefaultHome; }
    public void setIsDefaultHome(Boolean isDefaultHome) { this.isDefaultHome = isDefaultHome; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final GameweekDto instance = new GameweekDto();
        public Builder id(Integer v) { instance.setId(v); return this; }
        public Builder name(String v) { instance.setName(v); return this; }
        public Builder deadlineTime(LocalDateTime v) { instance.setDeadlineTime(v); return this; }
        public Builder averageEntryScore(Integer v) { instance.setAverageEntryScore(v); return this; }
        public Builder highestScore(Integer v) { instance.setHighestScore(v); return this; }
        public Builder isCurrent(Boolean v) { instance.setIsCurrent(v); return this; }
        public Builder isNext(Boolean v) { instance.setIsNext(v); return this; }
        public Builder isPrevious(Boolean v) { instance.setIsPrevious(v); return this; }
        public Builder finished(Boolean v) { instance.setFinished(v); return this; }
        public Builder dataChecked(Boolean v) { instance.setDataChecked(v); return this; }
        public Builder phase(String v) { instance.setPhase(v); return this; }
        public Builder isDefaultHome(Boolean v) { instance.setIsDefaultHome(v); return this; }
        public GameweekDto build() { return instance; }
    }
}
