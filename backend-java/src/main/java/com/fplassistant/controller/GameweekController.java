package com.fplassistant.controller;

import com.fplassistant.dto.GameweekDto;
import com.fplassistant.service.GameweekPhaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gameweeks")
public class GameweekController {

    private final GameweekPhaseService gameweekPhaseService;

    public GameweekController(GameweekPhaseService gameweekPhaseService) {
        this.gameweekPhaseService = gameweekPhaseService;
    }

    @GetMapping
    public ResponseEntity<List<GameweekDto>> getGameweeks() {
        return ResponseEntity.ok(gameweekPhaseService.getAllGameweeks());
    }
}
