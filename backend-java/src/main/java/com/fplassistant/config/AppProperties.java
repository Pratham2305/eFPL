package com.fplassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Fpl fpl = new Fpl();
    private MlService mlService = new MlService();

    public Fpl getFpl() { return fpl; }
    public void setFpl(Fpl fpl) { this.fpl = fpl; }

    public MlService getMlService() { return mlService; }
    public void setMlService(MlService mlService) { this.mlService = mlService; }

    public static class Fpl {
        private int teamId;
        private String apiBaseUrl = "https://fantasy.premierleague.com/api";

        public int getTeamId() { return teamId; }
        public void setTeamId(int teamId) { this.teamId = teamId; }

        public String getApiBaseUrl() { return apiBaseUrl; }
        public void setApiBaseUrl(String apiBaseUrl) { this.apiBaseUrl = apiBaseUrl; }
    }

    public static class MlService {
        private String baseUrl = "http://localhost:8000";

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }
}
