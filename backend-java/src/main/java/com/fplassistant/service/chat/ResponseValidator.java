package com.fplassistant.service.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ResponseValidator {

    private static final Logger log = LoggerFactory.getLogger(ResponseValidator.class);

    // Pattern for floating point numbers or prices like £12.5m, 8.4, 6.75
    private static final Pattern DECIMAL_OR_PRICE_PATTERN = Pattern.compile("(?:£|\\$)?(\\d+\\.\\d+)(?:m)?");

    public record ValidationResult(boolean isValid, List<String> ungroundedItems) {
        public static ValidationResult valid() {
            return new ValidationResult(true, Collections.emptyList());
        }

        public static ValidationResult invalid(List<String> items) {
            return new ValidationResult(false, items);
        }
    }

    public ValidationResult validate(String llmResponse, String contextJson) {
        if (llmResponse == null || llmResponse.isBlank()) {
            return ValidationResult.invalid(List.of("Empty response from LLM"));
        }
        if (contextJson == null || contextJson.isBlank()) {
            return ValidationResult.valid();
        }

        List<String> ungrounded = new ArrayList<>();

        // Extract decimal numbers and prices from response
        Matcher matcher = DECIMAL_OR_PRICE_PATTERN.matcher(llmResponse);
        while (matcher.find()) {
            String numStr = matcher.group(1);
            try {
                double val = Double.parseDouble(numStr);
                // Harmless decimals or multipliers
                if (val == 1.0 || val == 2.0 || val == 0.0) {
                    continue;
                }

                // Check if val appears in context JSON either as "8.4", "8.40", or substring
                String target1 = numStr;
                String target2 = String.format(Locale.US, "%.1f", val);
                String target3 = String.format(Locale.US, "%.2f", val);

                if (!contextJson.contains(target1) && !contextJson.contains(target2) && !contextJson.contains(target3)) {
                    log.debug("Potential ungrounded number in LLM reply: {}", numStr);
                    ungrounded.add(numStr);
                }
            } catch (NumberFormatException ignored) {}
        }

        // If more than 2 distinct ungrounded numbers are found, flag validation failure
        Set<String> uniqueUngrounded = new HashSet<>(ungrounded);
        if (uniqueUngrounded.size() >= 2) {
            log.warn("Validation failed for LLM response. Ungrounded numbers: {}", uniqueUngrounded);
            return ValidationResult.invalid(new ArrayList<>(uniqueUngrounded));
        }

        return ValidationResult.valid();
    }
}
