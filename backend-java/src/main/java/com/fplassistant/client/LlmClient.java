package com.fplassistant.client;

import com.fplassistant.dto.ChatLlmResult;
import com.fplassistant.dto.ChatMessageDto;

import java.util.List;

public interface LlmClient {

    ChatLlmResult generateChat(List<ChatMessageDto> messages, double temperature, int numCtx);

    boolean isReachable();

    boolean isModelAvailable(String model);

    String getBaseUrl();

    String getDefaultModel();
}
