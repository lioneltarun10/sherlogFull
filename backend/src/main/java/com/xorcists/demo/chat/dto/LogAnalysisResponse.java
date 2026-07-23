package com.xorcists.demo.chat.dto;

import java.util.List;

/**
 * Response DTO for log analysis.
 * Contains the original request context (traceId, serviceName),
 * the matched logs from Grafana/Loki, and the AI analysis response.
 */
public class LogAnalysisResponse {

    private String traceId;
    private String serviceName;
    private List<String> logs;
    private String aiResponse;
    private boolean success;
    private String errorMessage;

    public LogAnalysisResponse() {
    }

    /**
     * Constructor for successful response
     */
    public LogAnalysisResponse(String traceId, String serviceName, List<String> logs, String aiResponse) {
        this.traceId = traceId;
        this.serviceName = serviceName;
        this.logs = logs;
        this.aiResponse = aiResponse;
        this.success = true;
        this.errorMessage = null;
    }

    /**
     * Factory method for error response
     */
    public static LogAnalysisResponse error(String traceId, String serviceName, String errorMessage) {
        LogAnalysisResponse response = new LogAnalysisResponse();
        response.setTraceId(traceId);
        response.setServiceName(serviceName);
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
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

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

    public String getAiResponse() {
        return aiResponse;
    }

    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
