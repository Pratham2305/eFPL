package com.fplassistant.dto;

public record ChatLlmResult(
    String content,
    boolean success,
    String errorReason,
    long durationMs
) {}
