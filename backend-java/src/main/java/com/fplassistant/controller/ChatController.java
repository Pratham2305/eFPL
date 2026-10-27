package com.fplassistant.controller;

import com.fplassistant.dto.ChatHealthResponse;
import com.fplassistant.dto.ChatRequestDto;
import com.fplassistant.dto.ChatResponseDto;
import com.fplassistant.model.ChatMessageEntity;
import com.fplassistant.repository.ChatMessageRepository;
import com.fplassistant.service.chat.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatMessageRepository messageRepository;

    public ChatController(ChatService chatService, ChatMessageRepository messageRepository) {
        this.chatService = chatService;
        this.messageRepository = messageRepository;
    }

    /**
     * POST /api/chat — Grounded FPL Natural Language Q&A assistant.
     */
    @PostMapping
    public ResponseEntity<ChatResponseDto> handleChatQuery(@RequestBody ChatRequestDto request) {
        ChatResponseDto response = chatService.processChat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/chat/health — Check Ollama connectivity, model status, and predictions availability.
     */
    @GetMapping("/health")
    public ResponseEntity<ChatHealthResponse> getChatHealth() {
        ChatHealthResponse health = chatService.getHealth();
        return ResponseEntity.ok(health);
    }

    /**
     * GET /api/chat/history/{conversationId} — Retrieve conversation history.
     */
    @GetMapping("/history/{conversationId}")
    public ResponseEntity<List<ChatMessageEntity>> getHistory(@PathVariable UUID conversationId) {
        List<ChatMessageEntity> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return ResponseEntity.ok(messages);
    }
}
