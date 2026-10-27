package com.fplassistant.controller;

import com.fplassistant.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * GET /api/recommendations/transfers?horizon=3
     */
    @GetMapping("/recommendations/transfers")
    public ResponseEntity<Map<String, Object>> getTransfers(@RequestParam(defaultValue = "3") int horizon) {
        return ResponseEntity.ok(recommendationService.getTransferRecommendations(horizon));
    }

    /**
     * GET /api/recommendations/captain
     */
    @GetMapping("/recommendations/captain")
    public ResponseEntity<Map<String, Object>> getCaptains() {
        return ResponseEntity.ok(recommendationService.getCaptainRecommendations());
    }

    /**
     * GET /api/analysis/weaknesses
     */
    @GetMapping("/analysis/weaknesses")
    public ResponseEntity<Map<String, Object>> getWeaknesses() {
        return ResponseEntity.ok(recommendationService.getSquadWeaknesses());
    }

    /**
     * GET /api/analysis/differentials
     */
    @GetMapping("/analysis/differentials")
    public ResponseEntity<Map<String, Object>> getDifferentials(@RequestParam(defaultValue = "12.0") double maxOwnership) {
        return ResponseEntity.ok(recommendationService.getDifferentials(maxOwnership));
    }

    /**
     * GET /api/recommendations/chips?horizon=8
     */
    @GetMapping("/recommendations/chips")
    public ResponseEntity<Map<String, Object>> getChips(@RequestParam(defaultValue = "8") int horizon) {
        return ResponseEntity.ok(recommendationService.getChipStrategy(horizon));
    }

    /**
     * POST /api/predictions/generate
     */
    @PostMapping("/predictions/generate")
    public ResponseEntity<Map<String, Object>> generatePredictions(@RequestParam(required = false) Integer targetGw) {
        return ResponseEntity.ok(recommendationService.generatePredictions(targetGw));
    }
}
