package com.fplassistant.service;

import com.fplassistant.dto.TeamCurrentResponse;
import org.springframework.stereotype.Service;

@Service
public class TeamService {

    private final GameweekSquadService squadService;

    public TeamService(GameweekSquadService squadService) {
        this.squadService = squadService;
    }

    public TeamCurrentResponse getCurrentTeam() {
        return squadService.getSquadForGameweek(null);
    }

    public TeamCurrentResponse getCurrentTeam(Integer targetGw) {
        return squadService.getSquadForGameweek(targetGw);
    }
}
