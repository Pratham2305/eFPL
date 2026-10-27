package com.fplassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Fpl fpl = new Fpl();
    private MlService mlService = new MlService();
    private Ollama ollama = new Ollama();
    private Chat chat = new Chat();

    public Fpl getFpl() { return fpl; }
    public void setFpl(Fpl fpl) { this.fpl = fpl; }

    public MlService getMlService() { return mlService; }
    public void setMlService(MlService mlService) { this.mlService = mlService; }

    public Ollama getOllama() { return ollama; }
    public void setOllama(Ollama ollama) { this.ollama = ollama; }

    public Chat getChat() { return chat; }
    public void setChat(Chat chat) { this.chat = chat; }

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

    public static class Ollama {
        private String baseUrl = "http://localhost:11434";
        private String model = "llama3.1:8b";
        private int timeoutSeconds = 90;
        private double temperature = 0.2;
        private int numCtx = 4096;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }

        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }

        public int getNumCtx() { return numCtx; }
        public void setNumCtx(int numCtx) { this.numCtx = numCtx; }
    }

    public static class Chat {
        private int maxHistoryTurns = 6;

        public int getMaxHistoryTurns() { return maxHistoryTurns; }
        public void setMaxHistoryTurns(int maxHistoryTurns) { this.maxHistoryTurns = maxHistoryTurns; }
    }
}
