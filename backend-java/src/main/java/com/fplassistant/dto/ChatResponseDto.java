package com.fplassistant.dto;

import java.time.Instant;
import java.util.UUID;

public class ChatResponseDto {
    private String query;
    private String reply;
    private String intent;
    private String source; // "LLM" or "DETERMINISTIC_FALLBACK"
    private UUID conversationId;
    private Instant dataAsOf;
    private String warning;
    private boolean grounded;

    public ChatResponseDto() {}

    public ChatResponseDto(String query, String reply, String intent, String source,
                           UUID conversationId, Instant dataAsOf, String warning, boolean grounded) {
        this.query = query;
        this.reply = reply;
        this.intent = intent;
        this.source = source;
        this.conversationId = conversationId;
        this.dataAsOf = dataAsOf;
        this.warning = warning;
        this.grounded = grounded;
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public Instant getDataAsOf() { return dataAsOf; }
    public void setDataAsOf(Instant dataAsOf) { this.dataAsOf = dataAsOf; }

    public String getWarning() { return warning; }
    public void setWarning(String warning) { this.warning = warning; }

    public boolean isGrounded() { return grounded; }
    public void setGrounded(boolean grounded) { this.grounded = grounded; }
}
