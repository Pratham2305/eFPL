package com.fplassistant.controller;

import com.fplassistant.dto.SyncResponse;
import com.fplassistant.dto.TeamCurrentResponse;
import com.fplassistant.service.FplApiSyncService;
import com.fplassistant.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team")
public class TeamController {

    private final TeamService teamService;
    private final FplApiSyncService syncService;

    public TeamController(TeamService teamService, FplApiSyncService syncService) {
        this.teamService = teamService;
        this.syncService = syncService;
    }

    /**
     * GET /api/team/current?gameweekId=2
     */
    @GetMapping("/current")
    public ResponseEntity<TeamCurrentResponse> getCurrentTeam(@RequestParam(required = false) Integer gameweekId) {
        return ResponseEntity.ok(teamService.getCurrentTeam(gameweekId));
    }

    /**
     * POST /api/team/sync?teamId=12345 — pull latest data from official FPL API
     */
    @PostMapping("/sync")
    public ResponseEntity<SyncResponse> syncTeam(@RequestParam(required = false) Integer teamId) {
        SyncResponse result = syncService.fullSync(teamId);
        return ResponseEntity.ok(result);
    }
}
