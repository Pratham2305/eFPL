package com.fplassistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fplassistant.client.OllamaClient;
import com.fplassistant.dto.TeamCurrentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service that uses Ollama LLM to generate natural-language grounded explanations
 * and answer user Q&A queries.
 */
@Service
public class LlmExplanationService {

    private static final Logger log = LoggerFactory.getLogger(LlmExplanationService.class);

    private final OllamaClient ollamaClient;
    private final TeamService teamService;
    private final RecommendationService recommendationService;
    private final ObjectMapper objectMapper;

    public LlmExplanationService(OllamaClient ollamaClient, TeamService teamService,
                                 RecommendationService recommendationService, ObjectMapper objectMapper) {
        this.ollamaClient = ollamaClient;
        this.teamService = teamService;
        this.recommendationService = recommendationService;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT = """
            You are a Fantasy Premier League (FPL) Decision-Support Assistant.
            Your job is to explain data-driven player predictions and transfer options to the manager in clear, plain-English advice.
            
            CRITICAL RULES:
            1. Numbers, expected points (xPoints), prices, and rankings MUST come strictly from the provided context JSON.
            2. Never invent or hallucinate expected-points numbers or statistics not present in the context.
            3. Structure recommendation explanations into:
               - RECOMMENDATION
               - WHY
               - RISKS
               - ALTERNATIVES
            """;

    public Map<String, Object> answerChatQuery(String userQuery) {
        log.info("Processing AI Chat query: '{}'", userQuery);

        // Build rich context from DB
        TeamCurrentResponse squad = null;
        try { squad = teamService.getCurrentTeam(); } catch (Exception ignored) {}

        Map<String, Object> transfers = null;
        try { transfers = recommendationService.getTransferRecommendations(3); } catch (Exception ignored) {}

        Map<String, Object> captains = null;
        try { captains = recommendationService.getCaptainRecommendations(); } catch (Exception ignored) {}

        String contextJson = "";
        try {
            Map<String, Object> fullContext = Map.of(
                    "squad_summary", squad != null ? Map.of("rank", squad.getOverallRank(), "bank", squad.getBank(), "free_transfers", squad.getFreeTransfers()) : "No squad",
                    "top_transfers", transfers != null ? transfers.get("recommendations") : "None",
                    "captain_rankings", captains != null ? captains.get("captaincy_rankings") : "None"
            );
            contextJson = objectMapper.writeValueAsString(fullContext);
        } catch (Exception e) {
            contextJson = "{}";
        }

        String userPrompt = "Context Data:\n" + contextJson + "\n\nUser Question:\n" + userQuery;

        String llmReply = ollamaClient.generateResponse(SYSTEM_PROMPT, userPrompt);

        // Fallback narrative if Ollama is not running locally
        if (llmReply == null || llmReply.isBlank()) {
            llmReply = generateFallbackResponse(userQuery, squad, transfers);
        }

        return Map.of(
                "query", userQuery,
                "reply", llmReply,
                "grounded", true
        );
    }

    private String generateFallbackResponse(String query, TeamCurrentResponse squad, Map<String, Object> transfers) {
        StringBuilder sb = new StringBuilder();
        sb.append("🤖 **FPL Assistant Analysis**:\n\n");

        if (squad != null) {
            sb.append("Your current overall rank is **#").append(squad.getOverallRank() != null ? squad.getOverallRank() : "N/A")
              .append("** with **£").append(squad.getBank() != null ? (squad.getBank() / 10.0) : "0.0").append("m** in the bank and **")
              .append(squad.getFreeTransfers()).append(" free transfer(s)**.\n\n");
        }

        sb.append("Based on our expected points model (xP-v1.0):\n");
        sb.append("- **Transfers**: Check the AI Recommendations tab for top transfer-in targets offering net expected point gains.\n");
        sb.append("- **Captaincy**: Prioritize high-floor starters with easy fixture difficulty ratings.\n\n");
        sb.append("*(Note: For real-time natural language dialogue, ensure local Ollama container is running at http://localhost:11434)*");

        return sb.toString();
    }
}
