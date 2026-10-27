package com.fplassistant.dto;

import java.util.List;

public class PlayerMetaDto {
    private List<TeamOptionDto> teams;
    private List<PositionOptionDto> positions;
    private MinMaxDto priceRange;
    private MinMaxDto ownershipRange;
    private int totalPlayers;
    private Integer currentGameweek;
    private Integer nextGameweek;

    public PlayerMetaDto() {}

    public List<TeamOptionDto> getTeams() { return teams; }
    public void setTeams(List<TeamOptionDto> teams) { this.teams = teams; }

    public List<PositionOptionDto> getPositions() { return positions; }
    public void setPositions(List<PositionOptionDto> positions) { this.positions = positions; }

    public MinMaxDto getPriceRange() { return priceRange; }
    public void setPriceRange(MinMaxDto priceRange) { this.priceRange = priceRange; }

    public MinMaxDto getOwnershipRange() { return ownershipRange; }
    public void setOwnershipRange(MinMaxDto ownershipRange) { this.ownershipRange = ownershipRange; }

    public int getTotalPlayers() { return totalPlayers; }
    public void setTotalPlayers(int totalPlayers) { this.totalPlayers = totalPlayers; }

    public Integer getCurrentGameweek() { return currentGameweek; }
    public void setCurrentGameweek(Integer currentGameweek) { this.currentGameweek = currentGameweek; }

    public Integer getNextGameweek() { return nextGameweek; }
    public void setNextGameweek(Integer nextGameweek) { this.nextGameweek = nextGameweek; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final PlayerMetaDto instance = new PlayerMetaDto();
        public Builder teams(List<TeamOptionDto> v) { instance.setTeams(v); return this; }
        public Builder positions(List<PositionOptionDto> v) { instance.setPositions(v); return this; }
        public Builder priceRange(MinMaxDto v) { instance.setPriceRange(v); return this; }
        public Builder ownershipRange(MinMaxDto v) { instance.setOwnershipRange(v); return this; }
        public Builder totalPlayers(int v) { instance.setTotalPlayers(v); return this; }
        public Builder currentGameweek(Integer v) { instance.setCurrentGameweek(v); return this; }
        public Builder nextGameweek(Integer v) { instance.setNextGameweek(v); return this; }
        public PlayerMetaDto build() { return instance; }
    }

    public static class TeamOptionDto {
        private Integer id;
        private String name;
        private String shortName;

        public TeamOptionDto() {}
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getShortName() { return shortName; }
        public void setShortName(String shortName) { this.shortName = shortName; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final TeamOptionDto o = new TeamOptionDto();
            public Builder id(Integer v) { o.setId(v); return this; }
            public Builder name(String v) { o.setName(v); return this; }
            public Builder shortName(String v) { o.setShortName(v); return this; }
            public TeamOptionDto build() { return o; }
        }
    }

    public static class PositionOptionDto {
        private Short elementType;
        private String shortName;
        private String singularName;

        public PositionOptionDto() {}
        public Short getElementType() { return elementType; }
        public void setElementType(Short elementType) { this.elementType = elementType; }
        public String getShortName() { return shortName; }
        public void setShortName(String shortName) { this.shortName = shortName; }
        public String getSingularName() { return singularName; }
        public void setSingularName(String singularName) { this.singularName = singularName; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final PositionOptionDto o = new PositionOptionDto();
            public Builder elementType(Short v) { o.setElementType(v); return this; }
            public Builder shortName(String v) { o.setShortName(v); return this; }
            public Builder singularName(String v) { o.setSingularName(v); return this; }
            public PositionOptionDto build() { return o; }
        }
    }

    public static class MinMaxDto {
        private Double min;
        private Double max;

        public MinMaxDto() {}
        public Double getMin() { return min; }
        public void setMin(Double min) { this.min = min; }
        public Double getMax() { return max; }
        public void setMax(Double max) { this.max = max; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final MinMaxDto o = new MinMaxDto();
            public Builder min(Double v) { o.setMin(v); return this; }
            public Builder max(Double v) { o.setMax(v); return this; }
            public MinMaxDto build() { return o; }
        }
    }
}
