package com.fplassistant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fplassistant.client.LlmClient;
import com.fplassistant.dto.ChatLlmResult;
import com.fplassistant.dto.ChatRequestDto;
import com.fplassistant.dto.ChatResponseDto;
import com.fplassistant.model.*;
import com.fplassistant.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LlmClient llmClient;

    @Autowired
    private GameweekRepository gameweekRepository;

    @Autowired
    private MyTeamStateRepository myTeamStateRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PredictionRepository predictionRepository;

    @BeforeEach
    void setUp() {
        // Setup mock default behavior
        when(llmClient.getBaseUrl()).thenReturn("http://localhost:11434");
        when(llmClient.getDefaultModel()).thenReturn("llama3.1:8b");
        when(llmClient.isReachable()).thenReturn(false); // Default to unreachable (testing fallback)
        when(llmClient.isModelAvailable(anyString())).thenReturn(false);

        // Ensure a current gameweek and team state exist
        if (gameweekRepository.count() == 0) {
            Gameweek gw = new Gameweek();
            gw.setId(9);
            gw.setName("Gameweek 9");
            gw.setIsNext(true);
            gw.setIsCurrent(false);
            gameweekRepository.save(gw);

            MyTeamState state = new MyTeamState();
            state.setGameweek(gw);
            state.setBank(5); // £0.5m
            state.setFreeTransfers(1);
            state.setOverallRank(154200);
            state.setWildcardAvailable(true);
            state.setFreeHitAvailable(true);
            state.setTripleCaptainAvailable(true);
            state.setBenchBoostAvailable(false); // BB used
            myTeamStateRepository.save(state);
        }
    }

    @Test
    void testGetChatHealth() throws Exception {
        when(llmClient.isReachable()).thenReturn(true);
        when(llmClient.isModelAvailable("llama3.1:8b")).thenReturn(true);

        mockMvc.perform(get("/api/chat/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ollamaReachable").value(true))
                .andExpect(jsonPath("$.modelAvailable").value(true))
                .andExpect(jsonPath("$.model").value("llama3.1:8b"));
    }

    @Test
    void testRemainingChipsDeterministicAnswerWhenOllamaOffline() throws Exception {
        when(llmClient.isReachable()).thenReturn(false);

        ChatRequestDto req = new ChatRequestDto("what all are my remaining chips?", null);

        String responseBody = mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("CHIPS_STATUS"))
                .andExpect(jsonPath("$.source").value("DETERMINISTIC_FALLBACK"))
                .andReturn().getResponse().getContentAsString();

        ChatResponseDto resp = objectMapper.readValue(responseBody, ChatResponseDto.class);

        assertThat(resp.getReply()).contains("Wildcard, Free Hit, Triple Captain");
        assertThat(resp.getReply()).contains("Bench Boost");
        assertThat(resp.getReply()).doesNotContain("Check the AI Recommendations tab");
        assertThat(resp.getWarning()).contains("Local Ollama service is not reachable");
    }

    @Test
    void testTransferAdviceDeterministicAnswerWhenOllamaOffline() throws Exception {
        when(llmClient.isReachable()).thenReturn(false);

        ChatRequestDto req = new ChatRequestDto("hey, what transfer should i make this week?", null);

        String responseBody = mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("TRANSFER_ADVICE"))
                .andExpect(jsonPath("$.source").value("DETERMINISTIC_FALLBACK"))
                .andReturn().getResponse().getContentAsString();

        ChatResponseDto resp = objectMapper.readValue(responseBody, ChatResponseDto.class);

        assertThat(resp.getReply()).contains("RECOMMENDATION");
        assertThat(resp.getReply()).doesNotContain("Check the AI Recommendations tab");
    }

    @Test
    void testHappyPathWithLlmGroundedResponse() throws Exception {
        when(llmClient.isReachable()).thenReturn(true);
        when(llmClient.isModelAvailable("llama3.1:8b")).thenReturn(true);

        String mockLlmReply = "### RECOMMENDATION\nPrioritize saving your free transfer for GW9 to have 2 FTs for the upcoming fixture swings.\n\n### CONFIDENCE\nHigh.\n\n### WHY\nYour starting XI projected points are stable.";
        when(llmClient.generateChat(anyList(), anyDouble(), anyInt()))
                .thenReturn(new ChatLlmResult(mockLlmReply, true, null, 120));

        ChatRequestDto req = new ChatRequestDto("hey, what transfer should i make this week?", null);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("TRANSFER_ADVICE"))
                .andExpect(jsonPath("$.source").value("LLM"))
                .andExpect(jsonPath("$.reply").value(mockLlmReply));
    }
}
