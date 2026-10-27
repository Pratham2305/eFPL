package com.fplassistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fplassistant.config.AppProperties;
import com.fplassistant.model.Gameweek;
import com.fplassistant.model.Recommendation;
import com.fplassistant.repository.GameweekRepository;
import com.fplassistant.repository.RecommendationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service that proxies internal ML & recommendation requests to Python ml-service
 * and persists results in the local recommendation store.
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;
    private final RecommendationRepository recommendationRepository;
    private final GameweekRepository gameweekRepository;
    private final ObjectMapper objectMapper;

    public RecommendationService(RestTemplate restTemplate,
                                 AppProperties appProperties,
                                 RecommendationRepository recommendationRepository,
                                 GameweekRepository gameweekRepository,
                                 ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.appProperties = appProperties;
        this.recommendationRepository = recommendationRepository;
        this.gameweekRepository = gameweekRepository;
        this.objectMapper = objectMapper;
    }

    private String mlUrl(String path) {
        return appProperties.getMlService().getBaseUrl() + "/api/v1" + path;
    }

    /**
     * Startup check: if predictions table is empty for upcoming GW, trigger baseline prediction generation.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        try {
            Map<String, Object> status = getPredictionStatus();
            if (status == null || !status.containsKey("gwCoverage") || ((List<?>) status.get("gwCoverage")).isEmpty()) {
                log.info("Startup check: No predictions found for upcoming GWs. Generating baseline predictions...");
                generatePredictions(null, 6);
            } else {
                log.info("Startup check: Active predictions detected: model={}, coverage={}",
                        status.get("modelVersion"), status.get("gwCoverage"));
            }
        } catch (Exception e) {
            log.warn("Startup check: ML service not reachable or failed to check prediction status: {}", e.getMessage());
        }
    }

    public Map<String, Object> getPredictionStatus() {
        String url = mlUrl("/predict/status");
        log.info("Checking ML prediction status: {}", url);
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    public Map<String, Object> getTransferRecommendations(int horizon, int limit) {
        String url = mlUrl("/recommendations/transfers?horizon=" + horizon + "&limit=" + limit);
        log.info("Fetching transfer recommendations from ML service: {}", url);
        Map<String, Object> body = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
        persistRecommendation("transfers", body);
        return body;
    }

    public Map<String, Object> getTransferRecommendations(int horizon) {
        return getTransferRecommendations(horizon, 10);
    }

    public Map<String, Object> getCaptainRecommendations(String riskProfile) {
        String profile = (riskProfile != null && !riskProfile.isBlank()) ? riskProfile : "balanced";
        String url = mlUrl("/recommendations/captains?risk_profile=" + profile);
        log.info("Fetching captain recommendations from ML service: {}", url);
        Map<String, Object> body = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
        persistRecommendation("captaincy", body);
        return body;
    }

    public Map<String, Object> getCaptainRecommendations() {
        return getCaptainRecommendations("balanced");
    }

    public Map<String, Object> getSquadWeaknesses() {
        String url = mlUrl("/recommendations/weaknesses");
        log.info("Fetching squad weaknesses from ML service: {}", url);
        Map<String, Object> body = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
        persistRecommendation("weaknesses", body);
        return body;
    }

    public Map<String, Object> getDifferentials(Double maxOwnership) {
        String url = maxOwnership != null && maxOwnership > 0
                ? mlUrl("/recommendations/differentials?max_ownership=" + maxOwnership)
                : mlUrl("/recommendations/differentials");
        log.info("Fetching differential recommendations from ML service: {}", url);
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    public Map<String, Object> getDifferentials(double maxOwnership) {
        return getDifferentials(Double.valueOf(maxOwnership));
    }

    public Map<String, Object> getChipStrategy(int horizon) {
        String url = mlUrl("/recommendations/chips?horizon=" + horizon);
        log.info("Fetching chip strategy from ML service: {}", url);
        Map<String, Object> body = restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
        persistRecommendation("chips", body);
        return body;
    }

    public Map<String, Object> generatePredictions(Integer targetGw, int horizon) {
        String path = targetGw != null
                ? "/predict/generate?target_gw=" + targetGw + "&horizon=" + horizon
                : "/predict/generate?horizon=" + horizon;
        String url = mlUrl(path);
        log.info("Triggering ML prediction generation: {}", url);
        return restTemplate.postForObject(url, null, Map.class);
    }

    public Map<String, Object> generatePredictions(Integer targetGw) {
        return generatePredictions(targetGw, 6);
    }

    private void persistRecommendation(String type, Map<String, Object> body) {
        if (body == null || recommendationRepository == null || gameweekRepository == null) {
            return;
        }
        try {
            Gameweek gw = gameweekRepository.findByIsNextTrue()
                    .or(() -> gameweekRepository.findByIsCurrentTrue())
                    .orElse(null);
            if (gw == null) {
                return;
            }

            Recommendation rec = new Recommendation();
            rec.setGameweek(gw);
            rec.setType(type);
            rec.setPayload(objectMapper.writeValueAsString(body));
            rec.setConfidence(BigDecimal.valueOf(0.85));
            recommendationRepository.save(rec);
            log.debug("Persisted {} recommendation for GW {}", type, gw.getId());
        } catch (Exception e) {
            log.warn("Failed to persist recommendation: {}", e.getMessage());
        }
    }
}
