package com.fplassistant.service.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseValidatorTest {

    private ResponseValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ResponseValidator();
    }

    @Test
    void testValidGroundedResponse() {
        String contextJson = "{\"squad_summary\":{\"bank_millions\":0.5,\"team_value_millions\":102.3},\"starting_xi\":[{\"name\":\"Salah\",\"cost_m\":12.5,\"xP_next\":8.4}]}";
        String llmReply = "### RECOMMENDATION\nCaptain Salah for GW9. He is projected at 8.4 xP and costs £12.5m, leaving £0.5m in the bank.";

        ResponseValidator.ValidationResult result = validator.validate(llmReply, contextJson);
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void testUngroundedHallucinatedNumbersCaught() {
        String contextJson = "{\"squad_summary\":{\"bank_millions\":0.5},\"starting_xi\":[{\"name\":\"Salah\",\"xP_next\":8.4}]}";
        // Response hallucinates two ungrounded stats: 17.8 and 23.4
        String hallucinatedReply = "Salah is projected at 17.8 xP and will yield 23.4 points!";

        ResponseValidator.ValidationResult result = validator.validate(hallucinatedReply, contextJson);
        assertThat(result.isValid()).isFalse();
        assertThat(result.ungroundedItems()).contains("17.8", "23.4");
    }

    @Test
    void testHarmlessNumbersNotFlaggedAsFalsePositives() {
        String contextJson = "{\"starting_xi\":[{\"name\":\"Salah\",\"xP_next\":8.4}]}";
        // 1., 2., GW 9, 3-4-3 are structural
        String reply = "1. Captain Salah (8.4 xP) in GW 9.\n2. Formation is 3-4-3.";

        ResponseValidator.ValidationResult result = validator.validate(reply, contextJson);
        assertThat(result.isValid()).isTrue();
    }
}
