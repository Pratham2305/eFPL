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
     * GET /api/predictions/status
     */
    @GetMapping("/predictions/status")
    public ResponseEntity<Map<String, Object>> getPredictionStatus() {
        return ResponseEntity.ok(recommendationService.getPredictionStatus());
    }

    /**
     * GET /api/recommendations/transfers?horizon=3&limit=10
     */
    @GetMapping("/recommendations/transfers")
    public ResponseEntity<Map<String, Object>> getTransfers(
            @RequestParam(defaultValue = "3") int horizon,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getTransferRecommendations(horizon, limit));
    }

    /**
     * GET /api/recommendations/captain?riskProfile=balanced
     */
    @GetMapping("/recommendations/captain")
    public ResponseEntity<Map<String, Object>> getCaptains(
            @RequestParam(defaultValue = "balanced") String riskProfile) {
        return ResponseEntity.ok(recommendationService.getCaptainRecommendations(riskProfile));
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
    public ResponseEntity<Map<String, Object>> getDifferentials(
            @RequestParam(required = false) Double maxOwnership) {
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
    public ResponseEntity<Map<String, Object>> generatePredictions(
            @RequestParam(required = false) Integer targetGw,
            @RequestParam(defaultValue = "6") int horizon) {
        return ResponseEntity.ok(recommendationService.generatePredictions(targetGw, horizon));
    }
}
