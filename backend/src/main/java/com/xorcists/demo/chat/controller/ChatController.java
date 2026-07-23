package com.xorcists.demo.chat.controller;

import com.xorcists.demo.chat.dto.ChatRequest;
import com.xorcists.demo.chat.dto.ChatResponse;
import com.xorcists.demo.chat.dto.LogAnalysisRequest;
import com.xorcists.demo.chat.dto.LogAnalysisResponse;
import com.xorcists.demo.chat.service.ChatService;
import com.xorcists.demo.chat.service.LogAnalysisService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatController {

    private static final Logger LOG = Logger.getLogger(ChatController.class);

    @Inject
    ChatService chatService;

    @Inject
    LogAnalysisService logAnalysisService;

    @POST
    @Path("/chat")
    public Response chat(ChatRequest request) {
        LOG.infof("Received chat request: %s", request.getMessage());
        
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ChatResponse("Message cannot be empty"))
                .build();
        }

        ChatResponse response = chatService.processMessage(request);
        LOG.infof("Sending chat response: %s", response.getReply());
        
        return Response.ok(response).build();
    }

    @GET
    @Path("/health")
    @Produces(MediaType.TEXT_PLAIN)
    public Response health() {
        return Response.ok("OK").build();
    }

    /**
     * POST /api/analyze
     * Analyzes logs for a given traceId and serviceName using OpenAI.
     * 
     * Request body: { "traceId": "...", "serviceName": "...", "userPrompt": "..." }
     * 
     * Flow:
     * 1. Validates required fields
     * 2. Fetches logs from Grafana/Loki using traceId and serviceName
     * 3. Sends logs + userPrompt to OpenAI for analysis
     * 4. Returns traceId, serviceName, matched logs, and AI response
     */
    @POST
    @Path("/analyze")
    public Response analyze(LogAnalysisRequest request) {
        LOG.infof("Received log analysis request: traceId=%s, serviceName=%s, from=%s, to=%s", 
            request.getTraceId(), request.getServiceName(), request.getFrom(), request.getTo());

        // Validate required fields
        ValidationResult validation = validateAnalysisRequest(request);
        if (!validation.isValid()) {
            LOG.warnf("Validation failed: %s", validation.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(LogAnalysisResponse.error(
                    request.getTraceId(), 
                    request.getServiceName(), 
                    validation.getMessage()))
                .build();
        }

        // Perform the analysis
        LogAnalysisResponse response = logAnalysisService.analyze(request);
        
        if (!response.isSuccess()) {
            LOG.warnf("Analysis failed: %s", response.getErrorMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(response)
                .build();
        }

        LOG.infof("Analysis completed successfully for traceId=%s", request.getTraceId());
        return Response.ok(response).build();
    }

    /**
     * Validates the log analysis request.
     */
    private ValidationResult validateAnalysisRequest(LogAnalysisRequest request) {
        if (request == null) {
            return ValidationResult.invalid("Request body is required");
        }
        if (request.getTraceId() == null || request.getTraceId().trim().isEmpty()) {
            return ValidationResult.invalid("traceId is required");
        }
        // serviceName is optional - defaults to mass.planning-import-backend.service in LogAnalysisService
        if (request.getUserPrompt() == null || request.getUserPrompt().trim().isEmpty()) {
            return ValidationResult.invalid("userPrompt is required");
        }
        // Validate time range if provided
        String timeRangeError = request.validateTimeRange();
        if (timeRangeError != null) {
            return ValidationResult.invalid(timeRangeError);
        }
        return ValidationResult.valid();
    }

    /**
     * Simple validation result holder
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        boolean isValid() {
            return valid;
        }

        String getMessage() {
            return message;
        }
    }
}
