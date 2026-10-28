package com.fplassistant.controller;

import com.fplassistant.dto.PerformanceHistoryResponse;
import com.fplassistant.dto.SyncResponse;
import com.fplassistant.dto.TeamCurrentResponse;
import com.fplassistant.service.FplApiSyncService;
import com.fplassistant.service.PerformanceHistoryService;
import com.fplassistant.service.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team")
public class TeamController {

    private final TeamService teamService;
    private final FplApiSyncService syncService;
    private final PerformanceHistoryService historyService;

    public TeamController(TeamService teamService,
                          FplApiSyncService syncService,
                          PerformanceHistoryService historyService) {
        this.teamService = teamService;
        this.syncService = syncService;
        this.historyService = historyService;
    }

    /**
     * GET /api/team/current?gameweekId=8
     */
    @GetMapping("/current")
    public ResponseEntity<TeamCurrentResponse> getCurrentTeam(@RequestParam(required = false) Integer gameweekId) {
        return ResponseEntity.ok(teamService.getCurrentTeam(gameweekId));
    }

    /**
     * GET /api/team/gameweek/{gw}
     */
    @GetMapping("/gameweek/{gw}")
    public ResponseEntity<TeamCurrentResponse> getGameweekTeam(@PathVariable Integer gw) {
        return ResponseEntity.ok(teamService.getCurrentTeam(gw));
    }

    /**
     * GET /api/team/history — Season performance history, KPIs, insights, and charts
     */
    @GetMapping("/history")
    public ResponseEntity<PerformanceHistoryResponse> getTeamHistory() {
        return ResponseEntity.ok(historyService.getPerformanceHistory());
    }

    /**
     * POST /api/team/sync?teamId=12345&forceHistory=true — pull latest data from official FPL API
     */
    @PostMapping("/sync")
    public ResponseEntity<SyncResponse> syncTeam(
            @RequestParam(required = false) Integer teamId,
            @RequestParam(required = false, defaultValue = "false") boolean forceHistory) {
        SyncResponse result = syncService.fullSync(teamId, forceHistory);
        return ResponseEntity.ok(result);
    }
}
