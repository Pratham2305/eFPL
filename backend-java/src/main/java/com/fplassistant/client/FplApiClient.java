package com.fplassistant.client;

import com.fplassistant.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
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
     * Returns null if 404 (e.g. future/pre-deadline gameweek).
     */
    public FplPicksResponse fetchPicks(int teamId, int gameweek) {
        String url = appProperties.getFpl().getApiBaseUrl()
                + "/entry/" + teamId + "/event/" + gameweek + "/picks/";
        log.info("Fetching FPL picks for team {}, GW {} from {}", teamId, gameweek, url);
        try {
            return restTemplate.getForObject(url, FplPicksResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            log.info("FPL picks for team {}, GW {} not found (HTTP 404 - likely future event)", teamId, gameweek);
            return null;
        }
    }

    /**
     * GET /api/entry/{teamId}/history/ — historical gameweek results and chips
     */
    public FplHistoryResponse fetchEntryHistory(int teamId) {
        String url = appProperties.getFpl().getApiBaseUrl() + "/entry/" + teamId + "/history/";
        log.info("Fetching FPL entry history for team {} from {}", teamId, url);
        return restTemplate.getForObject(url, FplHistoryResponse.class);
    }

    /**
     * GET /api/entry/{teamId}/transfers/ — transfers made throughout the season
     */
    public List<FplTransferResponse> fetchTransfers(int teamId) {
        String url = appProperties.getFpl().getApiBaseUrl() + "/entry/" + teamId + "/transfers/";
        log.info("Fetching FPL transfers for team {} from {}", teamId, url);
        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<FplTransferResponse>>() {}
            ).getBody();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("FPL transfers for team {} returned 404", teamId);
            return Collections.emptyList();
        }
    }

    /**
     * GET /api/event/{gameweek}/live/ — player performance stats for a specific gameweek
     */
    public FplLiveResponse fetchEventLive(int gameweek) {
        String url = appProperties.getFpl().getApiBaseUrl() + "/event/" + gameweek + "/live/";
        log.info("Fetching FPL live stats for GW {} from {}", gameweek, url);
        try {
            return restTemplate.getForObject(url, FplLiveResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("FPL live stats for GW {} returned 404", gameweek);
            return null;
        }
    }
}
