package com.xorcists.demo.chat.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Request DTO for log analysis with OpenAI.
 * Contains traceId and serviceName for fetching logs from Grafana/Loki,
 * and userPrompt for the actual question to ask OpenAI.
 * 
 * Optional time range fields (from, to) can be provided as ISO-8601 strings.
 * If not provided, defaults to last 7 days.
 */
public class LogAnalysisRequest {

    // Optional at DTO level - validated in ChatController.validateAnalysisRequest()
    private String traceId;

    // Optional - defaults to "mass.planning-import-backend.service" in LogAnalysisService
    private String serviceName;

    @NotBlank(message = "userPrompt is required")
    private String userPrompt;

    // Optional - ISO-8601 format (e.g., "2026-07-16T00:00:00Z"). Defaults to 7 days ago.
    private String from;

    // Optional - ISO-8601 format (e.g., "2026-07-22T23:59:59Z"). Defaults to now.
    private String to;

    public LogAnalysisRequest() {
    }

    public LogAnalysisRequest(String traceId, String serviceName, String userPrompt) {
        this.traceId = traceId;
        this.serviceName = serviceName;
        this.userPrompt = userPrompt;
    }

    public LogAnalysisRequest(String traceId, String serviceName, String userPrompt, String from, String to) {
        this.traceId = traceId;
        this.serviceName = serviceName;
        this.userPrompt = userPrompt;
        this.from = from;
        this.to = to;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Parses the 'from' field as epoch milliseconds.
     * Returns null if not set or invalid format.
     */
    public Long getFromAsEpochMillis() {
        return parseIsoToEpochMillis(from);
    }

    /**
     * Parses the 'to' field as epoch milliseconds.
     * Returns null if not set or invalid format.
     */
    public Long getToAsEpochMillis() {
        return parseIsoToEpochMillis(to);
    }

    private Long parseIsoToEpochMillis(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.trim().isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(isoDateTime).toEpochMilli();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Validates the time range if provided.
     * Returns null if valid, or an error message if invalid.
     */
    public String validateTimeRange() {
        Long fromMillis = getFromAsEpochMillis();
        Long toMillis = getToAsEpochMillis();

        // If neither is provided, that's valid (will use defaults)
        if (from == null && to == null) {
            return null;
        }

        // If one is provided but invalid format
        if (from != null && fromMillis == null) {
            return "Invalid 'from' format. Use ISO-8601 format (e.g., 2026-07-16T00:00:00Z)";
        }
        if (to != null && toMillis == null) {
            return "Invalid 'to' format. Use ISO-8601 format (e.g., 2026-07-22T23:59:59Z)";
        }

        // If both are provided, 'from' must be before 'to'
        if (fromMillis != null && toMillis != null && fromMillis >= toMillis) {
            return "'from' must be before 'to'";
        }

        // Check if time range is in the future
        long now = System.currentTimeMillis();
        if (fromMillis != null && fromMillis > now) {
            return "'from' cannot be in the future";
        }

        return null;
    }
}
