package com.fplassistant.dto;

import java.util.UUID;

public class ChatRequestDto {
    private String query;
    private UUID conversationId;

    public ChatRequestDto() {}

    public ChatRequestDto(String query, UUID conversationId) {
        this.query = query;
        this.conversationId = conversationId;
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }
}
