package com.fplassistant.controller;

import com.fplassistant.dto.FixtureDto;
import com.fplassistant.service.FixtureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fixtures")
public class FixtureController {

    private final FixtureService fixtureService;

    public FixtureController(FixtureService fixtureService) {
        this.fixtureService = fixtureService;
    }

    /**
     * GET /api/fixtures?gameweekId=1&teamId=2&rangeEnd=5
     * All parameters optional. Returns fixtures filtered by criteria.
     */
    @GetMapping
    public ResponseEntity<List<FixtureDto>> getFixtures(
            @RequestParam(required = false) Integer gameweekId,
            @RequestParam(required = false) Integer teamId,
            @RequestParam(required = false) Integer rangeEnd
    ) {
        return ResponseEntity.ok(fixtureService.getFixtures(gameweekId, teamId, rangeEnd));
    }
}
