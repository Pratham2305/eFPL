package com.fplassistant.client;

import com.fplassistant.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * HTTP client wrapper for the official FPL API.
 */
@Component
public class FplApiClient {

    private static final Logger log = LoggerFactory.getLogger(FplApiClient.class);

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;

    public FplApiClient(RestTemplate restTemplate, AppProperties appProperties) {
        this.restTemplate = restTemplate;
        this.appProperties = appProperties;
    }

    /**
     * GET /api/bootstrap-static/ — all players, teams, gameweeks
     */
    public FplBootstrapResponse fetchBootstrapStatic() {
        String url = appProperties.getFpl().getApiBaseUrl() + "/bootstrap-static/";
        log.info("Fetching FPL bootstrap-static from {}", url);
        return restTemplate.getForObject(url, FplBootstrapResponse.class);
    }

    /**
     * GET /api/fixtures/ — all fixtures
     */
    public List<FplFixtureResponse> fetchFixtures() {
        String url = appProperties.getFpl().getApiBaseUrl() + "/fixtures/";
        log.info("Fetching FPL fixtures from {}", url);
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<FplFixtureResponse>>() {}
        ).getBody();
    }

    /**
     * GET /api/entry/{teamId}/ — your team overview
     */
    public FplEntryResponse fetchEntry(int teamId) {
        String url = appProperties.getFpl().getApiBaseUrl() + "/entry/" + teamId + "/";
        log.info("Fetching FPL entry for team {} from {}", teamId, url);
        return restTemplate.getForObject(url, FplEntryResponse.class);
    }

    /**
     * GET /api/entry/{teamId}/event/{gw}/picks/ — your picks for a given GW
     */
    public FplPicksResponse fetchPicks(int teamId, int gameweek) {
        String url = appProperties.getFpl().getApiBaseUrl()
                + "/entry/" + teamId + "/event/" + gameweek + "/picks/";
        log.info("Fetching FPL picks for team {}, GW {} from {}", teamId, gameweek, url);
        return restTemplate.getForObject(url, FplPicksResponse.class);
    }
}
