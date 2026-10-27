package com.fplassistant.controller;

import com.fplassistant.service.LlmExplanationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final LlmExplanationService explanationService;

    public ChatController(LlmExplanationService explanationService) {
        this.explanationService = explanationService;
    }

    /**
     * POST /api/chat — Natural language Q&A chat endpoint grounded in FPL model context.
     * Request body: { "query": "Why should I captain Salah over Haaland?" }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> handleChatQuery(@RequestBody Map<String, String> request) {
        String query = request.getOrDefault("query", "Summarize my team status");
        Map<String, Object> response = explanationService.answerChatQuery(query);
        return ResponseEntity.ok(response);
    }
}
