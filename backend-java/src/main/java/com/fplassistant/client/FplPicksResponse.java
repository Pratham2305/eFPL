package com.fplassistant.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maps https://fantasy.premierleague.com/api/entry/{team_id}/event/{gw}/picks/
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FplPicksResponse {

    @JsonProperty("active_chip")
    private String activeChip;

    private List<FplPick> picks;

    public String getActiveChip() { return activeChip; }
    public void setActiveChip(String activeChip) { this.activeChip = activeChip; }

    public List<FplPick> getPicks() { return picks; }
    public void setPicks(List<FplPick> picks) { this.picks = picks; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FplPick {
        private Integer element;
        private Integer position;
        private Integer multiplier;

        @JsonProperty("is_captain")
        private Boolean isCaptain;

        @JsonProperty("is_vice_captain")
        private Boolean isViceCaptain;

        public Integer getElement() { return element; }
        public void setElement(Integer element) { this.element = element; }

        public Integer getPosition() { return position; }
        public void setPosition(Integer position) { this.position = position; }

        public Integer getMultiplier() { return multiplier; }
        public void setMultiplier(Integer multiplier) { this.multiplier = multiplier; }

        public Boolean getIsCaptain() { return isCaptain; }
        public void setIsCaptain(Boolean isCaptain) { this.isCaptain = isCaptain; }

        public Boolean getIsViceCaptain() { return isViceCaptain; }
        public void setIsViceCaptain(Boolean isViceCaptain) { this.isViceCaptain = isViceCaptain; }
    }
}
