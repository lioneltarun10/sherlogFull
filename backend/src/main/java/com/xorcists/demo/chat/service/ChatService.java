package com.xorcists.demo.chat.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xorcists.demo.chat.dto.ChatRequest;
import com.xorcists.demo.chat.dto.ChatResponse;
import com.xorcists.demo.grafana.service.GrafanaService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class ChatService {

    private static final Logger LOG = Logger.getLogger(ChatService.class);

    // --- Prompt for Step 1: API routing decision ---
    private static final String API_SELECTOR_PROMPT = """
        You are a routing assistant for a Grafana observability system.
        Given a user query, decide which Grafana API to call to gather the data needed to answer it.
        
        Available APIs:
        1. LOGS      - Fetches application/service logs from Loki. Use when the user asks about errors, log messages, events, or "what happened". Requires a LogQL query string.
        2. METRICS   - Fetches metrics from Prometheus. Use when the user asks about CPU, memory, request rates, latency, or numerical measurements. Requires a PromQL query string.
        3. FOLDERS   - Lists Grafana dashboard folders. Use only when the user asks about dashboards or folder structure. No query string needed.
        4. DATASOURCES - Lists configured Grafana datasources. Use only when the user asks about data sources. No query string needed.
        
        Respond ONLY with a valid JSON object in this exact format, no extra text:
        {
          "api": "<LOGS|METRICS|FOLDERS|DATASOURCES>",
          "query": "<the LogQL or PromQL query string, or null if not applicable>"
        }
        
        Examples:
        - "Why are users getting 500 errors on the checkout service?" -> {"api": "LOGS", "query": "{service=\\"checkout\\"} |= \\"500\\""}
        - "What is the CPU usage of the auth service?" -> {"api": "METRICS", "query": "rate(process_cpu_seconds_total{service=\\"auth\\"}[5m])"}
        - "Show me available dashboards" -> {"api": "FOLDERS", "query": null}
        """;

    // --- Prompt for Step 3: Final analysis ---
    private static final String SYSTEM_PROMPT = """
        You are a log analysis assistant for service-oriented systems.
        Purpose: Given (1) a user query about services (issues, errors, unexpected behavior, "why did this happen?" questions, etc.) and (2) corresponding service logs (including service names, service logs, error logs, traces, metrics, etc.), analyze the logs in the context of the query and return a clear, in-depth explanation of what is happening and what to do next.
        Primary behavior:
        Always treat the user query and provided logs as the primary context for analysis.
        Diagnose issues, explain causes, and identify where in the system or which services the problem occurs.
        Provide step-by-step reasoning focused on the user's question and the given log data.
        Do not ignore logs or user query; always tie conclusions back to both.
        Do not invent services, log lines, or events not supported by the data.
        Do not request additional tools or external context; work only with what is provided unless explicitly instructed otherwise.
        Core tasks:
        Interpret log entries, error messages, stack traces, timestamps, correlation IDs, and service names.
        Relate events across multiple services to reconstruct the flow and identify failure points.
        Explain why an error or behavior occurred, where it originated, and how it propagated.
        Distinguish between root cause, symptoms, and side effects.
        Suggest concrete next steps: what to check, what to fix, configuration changes, retries, escalations, or monitoring improvements.
        Analysis rules:
        Use the user query to focus the analysis: prioritize logs relevant to the described symptom, time window, and services.
        Call out specific log lines, error codes, or patterns that support your conclusions.
        If multiple plausible causes exist, list them, indicate which is most likely, and what to investigate to confirm.
        If logs are incomplete or inconclusive, say so explicitly and describe what additional data would be needed.
        Avoid vague statements; prefer precise, evidence-based explanations grounded in log content.
        Preserve technical meaning exactly: do not alter log text, error codes, service names, or identifiers when referencing them.
        Response content:
        Brief summary of the main finding (what is happening / what the user likely needs).
        Detailed analysis: where the issue appears, what specific errors or events indicate the problem, how these events relate to the user's described symptom, any dependencies or upstream/downstream services involved.
        Recommended actions: immediate mitigation, longer-term fixes, additional checks.
        Strict output rules:
        Answer the user's query directly; do not restate the prompt or meta-instructions.
        No generic disclaimers unless uncertainty is specifically present.
        Optimized for actionable insight: the user should understand what happened and what to do next.
        """;

    @ConfigProperty(name = "azure.openai.endpoint")
    String endpoint;

    @ConfigProperty(name = "azure.openai.api-key")
    String apiKey;

    @ConfigProperty(name = "azure.openai.deployment-name")
    String deploymentName;

    @Inject
    GrafanaService grafanaService;

    private OpenAIClient openAIClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    void init() {
        LOG.info("Initializing Azure OpenAI client...");
        openAIClient = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(endpoint)
                .buildClient();
        LOG.info("Azure OpenAI client initialized successfully");
    }

    public ChatResponse processMessage(ChatRequest request) {
        String userMessage = request.getMessage();
        LOG.infof("Processing message: %s", userMessage);

        try {
            // ── Step 1: Ask the AI which Grafana API to call ──────────────────
            String apiDecisionJson = callAIForApiSelection(userMessage);
            LOG.infof("AI API selection response: %s", apiDecisionJson);

            JsonNode decision = objectMapper.readTree(apiDecisionJson);
            String api = decision.get("api").asText();
            String query = decision.has("query") && !decision.get("query").isNull()
                    ? decision.get("query").asText()
                    : null;

            LOG.infof("Selected Grafana API: %s, Query: %s", api, query);

            // ── Step 2: Call the appropriate Grafana API ──────────────────────
            String grafanaData = fetchGrafanaData(api, query);
            LOG.infof("Grafana data fetched for API [%s]", api);

            // ── Step 3: Send user message + Grafana data to AI for final answer ─
            String finalResponse = callAIForAnalysis(userMessage, api, grafanaData);
            LOG.infof("Final AI response generated");

            return new ChatResponse(finalResponse);

        } catch (Exception e) {
            LOG.errorf(e, "Error in processMessage: %s", e.getMessage());
            return new ChatResponse("Sorry, I encountered an error while processing your request. Please try again later.");
        }
    }

    /**
     * Step 1 — Ask the AI to pick which Grafana API to call.
     */
    private String callAIForApiSelection(String userMessage) {
        List<ChatRequestMessage> messages = Arrays.asList(
                new ChatRequestSystemMessage(API_SELECTOR_PROMPT),
                new ChatRequestUserMessage(userMessage)
        );

        ChatCompletionsOptions options = new ChatCompletionsOptions(messages);
        options.setMaxCompletionTokens(256);

        ChatCompletions completions = openAIClient.getChatCompletions(deploymentName, options);
        return completions.getChoices().get(0).getMessage().getContent().trim();
    }

    /**
     * Step 2 — Call the appropriate Grafana API based on AI's decision.
     */
    private String fetchGrafanaData(String api, String query) throws Exception {
        return switch (api) {
            case "LOGS" -> {
                if (query == null || query.isBlank()) {
                    throw new IllegalArgumentException("LOGS API requires a LogQL query");
                }
                Object response = grafanaService.executeLokiQuery(query);
                yield objectMapper.writeValueAsString(response);
            }
            case "METRICS" -> {
                if (query == null || query.isBlank()) {
                    throw new IllegalArgumentException("METRICS API requires a PromQL query");
                }
                Object response = grafanaService.executePrometheusQuery(query);
                yield objectMapper.writeValueAsString(response);
            }
            case "FOLDERS" -> {
                Object response = grafanaService.getFolders();
                yield objectMapper.writeValueAsString(response);
            }
            case "DATASOURCES" -> {
                Object response = grafanaService.getDatasources();
                yield objectMapper.writeValueAsString(response);
            }
            default -> throw new IllegalArgumentException("Unknown Grafana API selected by AI: " + api);
        };
    }

    /**
     * Step 3 — Send user query + fetched Grafana data to AI for final diagnostic analysis.
     */
    private String callAIForAnalysis(String userMessage, String apiUsed, String grafanaData) {
        String contextPrompt = String.format(
                "User Query: %s\n\nData fetched from Grafana API [%s]:\n%s",
                userMessage, apiUsed, grafanaData
        );

        List<ChatRequestMessage> messages = Arrays.asList(
                new ChatRequestSystemMessage(SYSTEM_PROMPT),
                new ChatRequestUserMessage(contextPrompt)
        );

        ChatCompletionsOptions options = new ChatCompletionsOptions(messages);
        options.setMaxCompletionTokens(16384);

        ChatCompletions completions = openAIClient.getChatCompletions(deploymentName, options);
        return completions.getChoices().get(0).getMessage().getContent();
    }

    /**
     * Analyze logs with a custom user prompt.
     * Uses the provided logs as context and the userPrompt as the user's question.
     *
     * @param logs the logs to analyze (as context)
     * @param userPrompt the user's question about the logs
     * @return the AI analysis response
     * @throws RuntimeException if OpenAI call fails
     */
    public String analyzeLogs(String logs, String userPrompt) {
        LOG.infof("Analyzing logs with user prompt: %s", userPrompt);
        LOG.infof("Logs context length: %d characters", logs != null ? logs.length() : 0);

        try {
            // Build the user message combining logs and user prompt
            String userMessage = buildUserMessageWithLogs(logs, userPrompt);

            List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage(SYSTEM_PROMPT),
                new ChatRequestUserMessage(userMessage)
            );

            ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);
            options.setMaxCompletionTokens(16384);

            ChatCompletions chatCompletions = openAIClient.getChatCompletions(deploymentName, options);

            String response = chatCompletions.getChoices().get(0).getMessage().getContent();
            LOG.infof("Received analysis response from Azure OpenAI (length: %d)", response.length());

            return response;
        } catch (Exception e) {
            LOG.errorf(e, "Error calling Azure OpenAI for log analysis: %s", e.getMessage());
            throw new RuntimeException("Failed to analyze logs with OpenAI: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the user message by combining logs and user prompt.
     */
    private String buildUserMessageWithLogs(String logs, String userPrompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("User Question: ").append(userPrompt).append("\n\n");
        sb.append("=== Service Logs ===\n");
        sb.append(logs != null ? logs : "No logs available");
        sb.append("\n=== End of Logs ===\n");
        return sb.toString();
    }
}
