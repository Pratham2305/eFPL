package com.fplassistant.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fplassistant.config.AppProperties;
import com.fplassistant.dto.ChatLlmResult;
import com.fplassistant.dto.ChatMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

@Component
public class OllamaLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaLlmClient.class);

    private final AppProperties appProperties;
    private final RestTemplate shortTimeoutRestTemplate;
    private final RestTemplate generationRestTemplate;

    public OllamaLlmClient(AppProperties appProperties, RestTemplateBuilder restTemplateBuilder) {
        this.appProperties = appProperties;
        this.shortTimeoutRestTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();

        int timeoutSec = appProperties.getOllama().getTimeoutSeconds();
        if (timeoutSec <= 0) timeoutSec = 90;
        this.generationRestTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(timeoutSec))
                .build();
    }

    @Override
    public String getBaseUrl() {
        return appProperties.getOllama().getBaseUrl();
    }

    @Override
    public String getDefaultModel() {
        return appProperties.getOllama().getModel();
    }

    @Override
    public boolean isReachable() {
        try {
            String url = getBaseUrl() + "/api/tags";
            ResponseEntity<OllamaTagsResponse> resp = shortTimeoutRestTemplate.getForEntity(url, OllamaTagsResponse.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("Ollama is not reachable at {}: {}", getBaseUrl(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isModelAvailable(String targetModel) {
        if (targetModel == null || targetModel.isBlank()) {
            targetModel = getDefaultModel();
        }
        try {
            String url = getBaseUrl() + "/api/tags";
            ResponseEntity<OllamaTagsResponse> resp = shortTimeoutRestTemplate.getForEntity(url, OllamaTagsResponse.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null || resp.getBody().getModels() == null) {
                return false;
            }

            final String cleanTarget = targetModel.toLowerCase().trim();
            final String baseName = cleanTarget.contains(":") ? cleanTarget.split(":")[0] : cleanTarget;

            return resp.getBody().getModels().stream()
                    .filter(Objects::nonNull)
                    .map(OllamaModelItem::getName)
                    .filter(Objects::nonNull)
                    .map(String::toLowerCase)
                    .anyMatch(name -> name.equals(cleanTarget) || name.startsWith(baseName + ":") || name.equals(baseName));
        } catch (Exception e) {
            log.debug("Error checking Ollama model '{}' at {}: {}", targetModel, getBaseUrl(), e.getMessage());
            return false;
        }
    }

    @Override
    public ChatLlmResult generateChat(List<ChatMessageDto> messages, double temperature, int numCtx) {
        long start = System.currentTimeMillis();
        String model = getDefaultModel();
        String url = getBaseUrl() + "/api/chat";

        List<Map<String, String>> formattedMessages = new ArrayList<>();
        for (ChatMessageDto msg : messages) {
            formattedMessages.add(Map.of("role", msg.role(), "content", msg.content()));
        }

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", formattedMessages,
                "stream", false,
                "options", Map.of(
                        "temperature", temperature,
                        "num_ctx", numCtx
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Invoking Ollama LLM chat at {} [model: {}, messages: {}]", url, model, messages.size());
            ResponseEntity<OllamaChatResponse> response = generationRestTemplate.postForEntity(url, entity, OllamaChatResponse.class);
            long elapsed = System.currentTimeMillis() - start;

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getMessage() != null) {
                String replyContent = response.getBody().getMessage().getContent();
                if (replyContent != null && !replyContent.isBlank()) {
                    return new ChatLlmResult(replyContent.trim(), true, null, elapsed);
                }
            }
            return new ChatLlmResult(null, false, "Empty response from Ollama", elapsed);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("Ollama call failed at {} (model: {}): {}", url, model, e.getMessage());
            return new ChatLlmResult(null, false, e.getMessage(), elapsed);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OllamaTagsResponse {
        private List<OllamaModelItem> models;

        public List<OllamaModelItem> getModels() { return models; }
        public void setModels(List<OllamaModelItem> models) { this.models = models; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OllamaModelItem {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OllamaChatResponse {
        private String model;
        private OllamaMessage message;
        private Boolean done;

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public OllamaMessage getMessage() { return message; }
        public void setMessage(OllamaMessage message) { this.message = message; }

        public Boolean getDone() { return done; }
        public void setDone(Boolean done) { this.done = done; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OllamaMessage {
        private String role;
        private String content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
