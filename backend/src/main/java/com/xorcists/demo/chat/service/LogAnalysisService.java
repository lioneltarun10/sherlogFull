package com.xorcists.demo.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.xorcists.demo.chat.dto.LogAnalysisRequest;
import com.xorcists.demo.chat.dto.LogAnalysisResponse;
import com.xorcists.demo.grafana.dto.GrafanaQueryResponse;
import com.xorcists.demo.grafana.service.GrafanaService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service that orchestrates the log analysis workflow:
 * 1. Accepts traceId, serviceName, and userPrompt from the request
 * 2. Builds a LogQL query and fetches logs from Grafana/Loki
 * 3. Parses the log response to extract log lines
 * 4. Sends logs + userPrompt to OpenAI for analysis
 * 5. Returns the combined response
 */
@ApplicationScoped
public class LogAnalysisService {

    private static final Logger LOG = Logger.getLogger(LogAnalysisService.class);

    /**
     * Default service name used when not provided in the request.
     */
    private static final String DEFAULT_SERVICE_NAME = "mass.planning-import-backend.service";

    /**
     * Default time window: 7 days.
     */
    private static final Duration DEFAULT_TIME_WINDOW = Duration.ofDays(7);

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Inject
    GrafanaService grafanaService;

    @Inject
    ChatService chatService;

    /**
     * Main entry point for log analysis.
     * Orchestrates fetching logs from Grafana and analyzing them with OpenAI.
     *
     * @param request the analysis request containing traceId, serviceName, userPrompt
     * @return LogAnalysisResponse with logs and AI analysis
     */
    public LogAnalysisResponse analyze(LogAnalysisRequest request) {
        String traceId = request.getTraceId();
        // Use provided serviceName or fall back to default
        String serviceName = (request.getServiceName() != null && !request.getServiceName().trim().isEmpty())
                ? request.getServiceName()
                : DEFAULT_SERVICE_NAME;
        String userPrompt = request.getUserPrompt();

        // Resolve effective time range
        TimeRange timeRange = resolveTimeRange(request);
        
        LOG.infof("Starting log analysis for traceId=%s, serviceName=%s", traceId, serviceName);
        LOG.infof("Effective time range: from=%s (%d ms), to=%s (%d ms)", 
            formatEpochMillis(timeRange.fromMillis), timeRange.fromMillis,
            formatEpochMillis(timeRange.toMillis), timeRange.toMillis);

        // Step 1: Build LogQL query and fetch logs from Grafana/Loki
        List<String> logs;
        try {
            logs = fetchLogsFromGrafana(traceId, serviceName, timeRange);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to fetch logs from Grafana for traceId=%s, serviceName=%s", traceId, serviceName);
            return LogAnalysisResponse.error(traceId, serviceName, 
                "Failed to fetch logs from Grafana: " + e.getMessage());
        }

        // Step 2: Validate that we got logs
        if (logs == null || logs.isEmpty()) {
            LOG.warnf("No logs found for traceId=%s, serviceName=%s, timeRange=[%s to %s]", 
                traceId, serviceName, 
                formatEpochMillis(timeRange.fromMillis), 
                formatEpochMillis(timeRange.toMillis));
            return LogAnalysisResponse.error(traceId, serviceName, 
                "No logs found for the specified traceId and serviceName in time range " +
                formatEpochMillis(timeRange.fromMillis) + " to " + formatEpochMillis(timeRange.toMillis));
        }

        LOG.infof("Fetched %d log lines for traceId=%s", logs.size(), traceId);

        // Step 3: Send logs to OpenAI for analysis
        String aiResponse;
        try {
            String logsAsText = formatLogsForAnalysis(logs);
            aiResponse = chatService.analyzeLogs(logsAsText, userPrompt);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to analyze logs with OpenAI for traceId=%s", traceId);
            return LogAnalysisResponse.error(traceId, serviceName, 
                "Failed to analyze logs with AI: " + e.getMessage());
        }

        // Step 4: Build and return successful response
        LOG.infof("Successfully completed log analysis for traceId=%s", traceId);
        return new LogAnalysisResponse(traceId, serviceName, logs, aiResponse);
    }

    /**
     * Resolves the effective time range from the request.
     * If from/to are provided, uses those. Otherwise, defaults to last 7 days.
     *
     * @param request the analysis request
     * @return resolved TimeRange with epoch milliseconds
     */
    private TimeRange resolveTimeRange(LogAnalysisRequest request) {
        Long fromMillis = request.getFromAsEpochMillis();
        Long toMillis = request.getToAsEpochMillis();

        long now = System.currentTimeMillis();

        // Default 'to' is now
        if (toMillis == null) {
            toMillis = now;
        }

        // Default 'from' is 7 days before 'to'
        if (fromMillis == null) {
            fromMillis = toMillis - DEFAULT_TIME_WINDOW.toMillis();
        }

        return new TimeRange(fromMillis, toMillis);
    }

    /**
     * Formats epoch milliseconds to ISO-8601 string for logging.
     */
    private String formatEpochMillis(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
    }

    /**
     * Fetches logs from Grafana/Loki using the traceId, serviceName, and time range.
     * Builds a LogQL query where the line contains the traceId.
     *
     * @param traceId the trace ID to search for in log lines
     * @param serviceName the service name to filter by
     * @param timeRange the time range to search within
     * @return list of log lines matching the criteria
     */
    private List<String> fetchLogsFromGrafana(String traceId, String serviceName, TimeRange timeRange) {
        // Build LogQL query: {service_name="<serviceName>"} |= "<traceId>"
        // This filters by service name label and searches for lines containing the traceId
        String logQlQuery = buildLogQlQuery(traceId, serviceName);
        LOG.infof("Executing LogQL query: %s", logQlQuery);
        LOG.infof("Query time range: from=%d to=%d", timeRange.fromMillis, timeRange.toMillis);

        GrafanaQueryResponse response = grafanaService.executeLokiQuery(
            logQlQuery, timeRange.fromMillis, timeRange.toMillis);
        return parseLogsFromResponse(response);
    }

    /**
     * Builds a LogQL query to search for logs containing the traceId
     * from the specified service.
     *
     * Query format: {service_name="<serviceName>"} |= "<traceId>"
     * - {service_name="..."} filters by the service label
     * - |= "..." filters lines containing the traceId string
     */
    private String buildLogQlQuery(String traceId, String serviceName) {
        // Escape any special characters in traceId to prevent LogQL injection
        String escapedTraceId = escapeLogQlString(traceId);
        String escapedServiceName = escapeLogQlString(serviceName);
        
        return String.format("{service_name=\"%s\"} |= \"%s\"", escapedServiceName, escapedTraceId);
    }

    /**
     * Escapes special characters in strings for safe use in LogQL queries.
     */
    private String escapeLogQlString(String value) {
        if (value == null) {
            return "";
        }
        // Escape backslashes and double quotes
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Parses the Grafana query response to extract log lines.
     * Handles the nested JSON structure of Loki responses.
     */
    private List<String> parseLogsFromResponse(GrafanaQueryResponse response) {
        List<String> logs = new ArrayList<>();

        if (response == null || response.getResults() == null) {
            LOG.warn("Received null or empty response from Grafana");
            return logs;
        }

        JsonNode results = response.getResults();
        LOG.debugf("Parsing Grafana response: %s", results.toString());

        // Navigate through the Loki response structure
        // Structure: results -> {refId} -> frames[] -> data -> values[][]
        results.fields().forEachRemaining(entry -> {
            JsonNode resultData = entry.getValue();
            
            if (resultData.has("frames")) {
                JsonNode frames = resultData.get("frames");
                for (JsonNode frame : frames) {
                    extractLogsFromFrame(frame, logs);
                }
            }
        });

        return logs;
    }

    /**
     * Extracts log lines from a single Loki frame.
     * 
     * Loki response structure:
     * - data.values[0] = labels array (objects with service_name, traceId, etc.)
     * - data.values[1] = timestamps array (epoch milliseconds)
     * - data.values[2] = log lines array (strings)
     */
    private void extractLogsFromFrame(JsonNode frame, List<String> logs) {
        if (frame == null || !frame.has("data")) {
            LOG.debug("Frame is null or has no 'data' field");
            return;
        }

        JsonNode data = frame.get("data");
        if (!data.has("values")) {
            LOG.debug("Frame data has no 'values' field");
            return;
        }

        JsonNode values = data.get("values");
        
        // Loki returns values as [labels[], timestamps[], logLines[]]
        // We want the log lines (third array, index 2)
        if (values.isArray() && values.size() >= 3) {
            JsonNode labels = values.get(0);
            JsonNode timestamps = values.get(1);
            JsonNode logLines = values.get(2);
            
            LOG.debugf("Frame contains %d labels, %d timestamps, %d log lines", 
                labels.size(), timestamps.size(), logLines.size());
            
            if (logLines.isArray()) {
                for (JsonNode logLine : logLines) {
                    if (logLine.isTextual()) {
                        logs.add(logLine.asText());
                    }
                }
            }
        } else {
            LOG.warnf("Unexpected values array structure: size=%d (expected >= 3)", 
                values.isArray() ? values.size() : -1);
        }
    }

    /**
     * Formats the list of logs into a single text block for OpenAI analysis.
     */
    private String formatLogsForAnalysis(List<String> logs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < logs.size(); i++) {
            sb.append(logs.get(i));
            if (i < logs.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Simple holder for time range in epoch milliseconds.
     */
    private static class TimeRange {
        final long fromMillis;
        final long toMillis;

        TimeRange(long fromMillis, long toMillis) {
            this.fromMillis = fromMillis;
            this.toMillis = toMillis;
        }
    }
}
