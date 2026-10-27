package com.fplassistant.service.chat;

import com.fplassistant.client.LlmClient;
import com.fplassistant.config.AppProperties;
import com.fplassistant.dto.*;
import com.fplassistant.model.*;
import com.fplassistant.repository.ChatConversationRepository;
import com.fplassistant.repository.ChatMessageRepository;
import com.fplassistant.repository.PredictionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final LlmClient llmClient;
    private final IntentClassifier intentClassifier;
    private final PlayerResolver playerResolver;
    private final ChatContextBuilder chatContextBuilder;
    private final ResponseValidator responseValidator;
    private final DeterministicAnswerRenderer deterministicRenderer;
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final PredictionRepository predictionRepository;
    private final AppProperties appProperties;
    private final String systemPrompt;

    public ChatService(LlmClient llmClient,
                       IntentClassifier intentClassifier,
                       PlayerResolver playerResolver,
                       ChatContextBuilder chatContextBuilder,
                       ResponseValidator responseValidator,
                       DeterministicAnswerRenderer deterministicRenderer,
                       ChatConversationRepository conversationRepository,
                       ChatMessageRepository messageRepository,
                       PredictionRepository predictionRepository,
                       AppProperties appProperties,
                       ResourceLoader resourceLoader) {
        this.llmClient = llmClient;
        this.intentClassifier = intentClassifier;
        this.playerResolver = playerResolver;
        this.chatContextBuilder = chatContextBuilder;
        this.responseValidator = responseValidator;
        this.deterministicRenderer = deterministicRenderer;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.predictionRepository = predictionRepository;
        this.appProperties = appProperties;
        this.systemPrompt = loadSystemPrompt(resourceLoader);
    }

    private String loadSystemPrompt(ResourceLoader loader) {
        try {
            Resource res = loader.getResource("classpath:prompts/system-prompt.st");
            if (res.exists()) {
                try (InputStream is = res.getInputStream()) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            log.warn("Could not load system-prompt.st, falling back to default inline prompt: {}", e.getMessage());
        }
        return """
                You are the FPL Assistant AI, an expert Fantasy Premier League analyst.
                CRITICAL GROUNDING RULES:
                1. Cite ONLY numbers and players present in <fpl_data>. Never invent stats.
                2. Use probabilistic language.
                3. Structure advice into RECOMMENDATION, CONFIDENCE, WHY, RISKS, ALTERNATIVES.
                """;
    }

    @Transactional
    public ChatResponseDto processChat(ChatRequestDto request) {
        String query = request.getQuery() != null ? request.getQuery().trim() : "Summarize my team status";
        UUID conversationId = request.getConversationId();

        // 1. Maintain or create conversation
        ChatConversation conversation = null;
        if (conversationId != null) {
            conversation = conversationRepository.findById(conversationId).orElse(null);
        }
        if (conversation == null) {
            conversation = new ChatConversation(UUID.randomUUID(), query.length() > 50 ? query.substring(0, 50) + "..." : query);
            conversation = conversationRepository.save(conversation);
        }
        conversationId = conversation.getId();

        // 2. Fetch recent conversation history and recent entities for pronoun resolution
        List<ChatMessageEntity> recentMessages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        List<String> recentEntities = extractRecentEntities(recentMessages);

        // 3. Resolve players and classify intent
        List<Player> resolvedPlayers = playerResolver.resolvePlayers(query, recentEntities);
        Intent intent = intentClassifier.classify(query, resolvedPlayers);
        log.info("Chat query: '{}' -> Intent: {}, Resolved Players: {}", query, intent,
                resolvedPlayers.stream().map(Player::getWebName).toList());

        // 4. Build context
        Map<String, Object> context = chatContextBuilder.buildContext(intent, resolvedPlayers);
        String contextJson = chatContextBuilder.toJson(context);

        String reply;
        String source = "LLM";
        String warning = null;
        boolean grounded = true;

        // 5. Check Ollama availability
        boolean reachable = llmClient.isReachable();
        boolean modelAvailable = reachable && llmClient.isModelAvailable(llmClient.getDefaultModel());

        if (!reachable) {
            reply = deterministicRenderer.render(intent, context, query, resolvedPlayers);
            source = "DETERMINISTIC_FALLBACK";
            warning = "Local Ollama service is not reachable at " + llmClient.getBaseUrl() + ". Answered deterministically from your squad intelligence.";
            log.info("Ollama unreachable. Using deterministic fallback.");
        } else if (!modelAvailable) {
            reply = deterministicRenderer.render(intent, context, query, resolvedPlayers);
            source = "DETERMINISTIC_FALLBACK";
            warning = "Model '" + llmClient.getDefaultModel() + "' not found in Ollama. Run 'ollama pull " + llmClient.getDefaultModel() + "' to enable full LLM dialogue.";
            log.info("Ollama model unavailable. Using deterministic fallback.");
        } else {
            // Build LLM Chat payload
            List<ChatMessageDto> messages = new ArrayList<>();
            messages.add(new ChatMessageDto("system", systemPrompt));

            int maxTurns = appProperties.getChat().getMaxHistoryTurns();
            int startIndex = Math.max(0, recentMessages.size() - maxTurns);
            for (int i = startIndex; i < recentMessages.size(); i++) {
                ChatMessageEntity prev = recentMessages.get(i);
                messages.add(new ChatMessageDto(prev.getRole(), prev.getContent()));
            }

            String userMessageWithContext = "User Question:\n" + query + "\n\n<fpl_data>\n" + contextJson + "\n</fpl_data>";
            messages.add(new ChatMessageDto("user", userMessageWithContext));

            ChatLlmResult llmResult = llmClient.generateChat(
                    messages,
                    appProperties.getOllama().getTemperature(),
                    appProperties.getOllama().getNumCtx()
            );

            if (llmResult.success() && llmResult.content() != null && !llmResult.content().isBlank()) {
                ResponseValidator.ValidationResult valResult = responseValidator.validate(llmResult.content(), contextJson);
                if (valResult.isValid()) {
                    reply = llmResult.content();
                    source = "LLM";
                } else {
                    log.warn("First LLM validation failed: {}. Retrying with corrective prompt...", valResult.ungroundedItems());
                    // Retry once with corrective instruction
                    List<ChatMessageDto> retryMessages = new ArrayList<>(messages);
                    retryMessages.add(new ChatMessageDto("assistant", llmResult.content()));
                    retryMessages.add(new ChatMessageDto("user",
                            "CORRECTION REQUIRED: Your response contained ungrounded numbers or player names ("
                                    + valResult.ungroundedItems()
                                    + "). Regenerate your response using ONLY numbers and players present in <fpl_data>."));

                    ChatLlmResult retryResult = llmClient.generateChat(
                            retryMessages,
                            appProperties.getOllama().getTemperature(),
                            appProperties.getOllama().getNumCtx()
                    );

                    if (retryResult.success() && retryResult.content() != null) {
                        ResponseValidator.ValidationResult secondVal = responseValidator.validate(retryResult.content(), contextJson);
                        if (secondVal.isValid()) {
                            reply = retryResult.content();
                            source = "LLM";
                        } else {
                            log.warn("Second LLM validation failed. Falling back to deterministic answer.");
                            reply = deterministicRenderer.render(intent, context, query, resolvedPlayers);
                            source = "DETERMINISTIC_FALLBACK";
                            warning = "LLM generated ungrounded statistics. Answered deterministically from your squad intelligence.";
                            grounded = false;
                        }
                    } else {
                        reply = deterministicRenderer.render(intent, context, query, resolvedPlayers);
                        source = "DETERMINISTIC_FALLBACK";
                        warning = "LLM retry failed. Answered deterministically from your squad intelligence.";
                    }
                }
            } else {
                reply = deterministicRenderer.render(intent, context, query, resolvedPlayers);
                source = "DETERMINISTIC_FALLBACK";
                warning = "Ollama query failed (" + llmResult.errorReason() + "). Answered deterministically from your squad intelligence.";
            }
        }

        // 6. Persist messages to DB
        ChatMessageEntity userMsg = new ChatMessageEntity(conversationId, "user", query, intent.name(), "USER");
        ChatMessageEntity assistantMsg = new ChatMessageEntity(conversationId, "assistant", reply, intent.name(), source);
        messageRepository.save(userMsg);
        messageRepository.save(assistantMsg);

        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        return new ChatResponseDto(
                query,
                reply,
                intent.name(),
                source,
                conversationId,
                Instant.now(),
                warning,
                grounded
        );
    }

    public ChatHealthResponse getHealth() {
        boolean reachable = llmClient.isReachable();
        String model = llmClient.getDefaultModel();
        boolean modelAvailable = reachable && llmClient.isModelAvailable(model);
        boolean predictionsAvailable = predictionRepository.count() > 0;

        return new ChatHealthResponse(
                reachable,
                modelAvailable,
                model,
                predictionsAvailable,
                Instant.now()
        );
    }

    private List<String> extractRecentEntities(List<ChatMessageEntity> messages) {
        List<String> entities = new ArrayList<>();
        if (messages == null || messages.isEmpty()) return entities;

        int start = Math.max(0, messages.size() - 4);
        for (int i = messages.size() - 1; i >= start; i--) {
            String content = messages.get(i).getContent();
            if (content != null) {
                // Find capitalized words or known players in recent text
                entities.add(content);
            }
        }
        return entities;
    }
}
