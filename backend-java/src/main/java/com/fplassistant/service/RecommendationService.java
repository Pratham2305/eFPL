package com.fplassistant.service;

import com.fplassistant.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service that proxies internal ML & recommendation requests to Python ml-service.
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;

    public RecommendationService(RestTemplate restTemplate, AppProperties appProperties) {
        this.restTemplate = restTemplate;
        this.appProperties = appProperties;
    }

    private String mlUrl(String path) {
        return appProperties.getMlService().getBaseUrl() + "/api/v1" + path;
    }

    public Map<String, Object> getTransferRecommendations(int horizon) {
        String url = mlUrl("/recommendations/transfers?horizon=" + horizon);
        log.info("Fetching transfer recommendations from ML service: {}", url);
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    public Map<String, Object> getCaptainRecommendations() {
        String url = mlUrl("/recommendations/captains");
        log.info("Fetching captain recommendations from ML service: {}", url);
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    public Map<String, Object> getSquadWeaknesses() {
        String url = mlUrl("/recommendations/weaknesses");
        log.info("Fetching squad weaknesses from ML service: {}", url);
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    public Map<String, Object> getDifferentials(double maxOwnership) {
        String url = mlUrl("/recommendations/differentials?max_ownership=" + maxOwnership);
        log.info("Fetching differential recommendations from ML service: {}", url);
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    public Map<String, Object> getChipStrategy(int horizon) {
        String url = mlUrl("/recommendations/chips?horizon=" + horizon);
        log.info("Fetching chip strategy from ML service: {}", url);
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    public Map<String, Object> generatePredictions(Integer targetGw) {
        String path = targetGw != null ? "/predict/generate?target_gw=" + targetGw : "/predict/generate";
        String url = mlUrl(path);
        log.info("Triggering ML prediction generation: {}", url);
        return restTemplate.postForObject(url, null, Map.class);
    }
}
