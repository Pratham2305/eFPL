package com.fplassistant.dto;

import java.time.Instant;

public class ChatHealthResponse {
    private boolean ollamaReachable;
    private boolean modelAvailable;
    private String model;
    private boolean predictionsAvailable;
    private Instant dataAsOf;

    public ChatHealthResponse() {}

    public ChatHealthResponse(boolean ollamaReachable, boolean modelAvailable, String model,
                              boolean predictionsAvailable, Instant dataAsOf) {
        this.ollamaReachable = ollamaReachable;
        this.modelAvailable = modelAvailable;
        this.model = model;
        this.predictionsAvailable = predictionsAvailable;
        this.dataAsOf = dataAsOf;
    }

    public boolean isOllamaReachable() { return ollamaReachable; }
    public void setOllamaReachable(boolean ollamaReachable) { this.ollamaReachable = ollamaReachable; }

    public boolean isModelAvailable() { return modelAvailable; }
    public void setModelAvailable(boolean modelAvailable) { this.modelAvailable = modelAvailable; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public boolean isPredictionsAvailable() { return predictionsAvailable; }
    public void setPredictionsAvailable(boolean predictionsAvailable) { this.predictionsAvailable = predictionsAvailable; }

    public Instant getDataAsOf() { return dataAsOf; }
    public void setDataAsOf(Instant dataAsOf) { this.dataAsOf = dataAsOf; }
}
