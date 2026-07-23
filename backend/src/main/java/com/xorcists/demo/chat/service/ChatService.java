package com.xorcists.demo.chat.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.xorcists.demo.chat.dto.ChatRequest;
import com.xorcists.demo.chat.dto.ChatResponse;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class ChatService {

    private static final Logger LOG = Logger.getLogger(ChatService.class);

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
        Detailed analysis:
        Where the issue appears (service, component, endpoint, job, etc.).
        What specific errors or events indicate the problem.
        How these events relate to the user's described symptom.
        Any dependencies or upstream/downstream services involved.
        Recommended actions:
        Immediate mitigation (e.g., restart service X, roll back deployment Y, adjust config Z).
        Longer-term fixes (e.g., code changes, validation, retry strategy, timeouts, resource limits).
        Additional checks (e.g., inspect database connectivity, verify credentials, monitor specific metrics).
        If the query is not an incident but a "what/why" question, provide a conceptual explanation based on the logs (e.g., why a service behaved a certain way, why a request was throttled, why a job was skipped).
        Strict output rules:
        Answer the user's query directly; do not restate the prompt or meta-instructions.
        Do not output raw logs verbatim unless needed to support a point; quote only relevant snippets.
        No generic disclaimers unless uncertainty is specifically present; then be explicit and concise.
        No bullet lists unless they help structure causes and actions clearly.
        No code fences unless the user explicitly requests code or configuration examples.
        Do not ask the user follow-up questions unless the logs are clearly insufficient and this blocks any meaningful conclusion.
        Failure prevention:
        If the logs contradict the user's expectation, explain the discrepancy clearly.
        If the logs do not show any error, focus on behavior, performance, or configuration aspects that could explain the query.
        If multiple time ranges or services are present, make clear which ones you are focusing on and why.
        If the query is ambiguous, state assumptions explicitly before giving conclusions, but still provide the best possible analysis from the available data.
        Style target:
        Dense, precise, technically accurate, and diagnostic.
        Neutral and professional tone.
        Optimized for actionable insight: the user should understand what happened and what to do next.
        """;

private static final String USER_PROMPT = """
        2026-07-20 14:58:09.453 info INFO - [SUCCESS] TaskDone event published. operation=PlanningModifySaved baseItemId=865462789462822913 traceId=3e293946-4033-409d-ad50-5758243f9d1f task=-180000 

2026-07-20 14:58:09.429 info INFO - Planning record updated in OIC successfully. itemId=865462789462822913 

2026-07-20 14:58:09.394 info INFO - [SUCCESS] TaskDone event published. operation=PlanningModifySaved baseItemId=865462795045441537 traceId=0b2c72be-3ffc-42a9-940d-47ff5757dddb task=-180000 

2026-07-20 14:58:09.380 info INFO - Planning record updated in OIC successfully. itemId=865462795045441537 

2026-07-20 14:58:09.358 info INFO - [SUCCESS] TaskDone event published. operation=PlanningModifySaved baseItemId=865462807473164289 traceId=801e45f8-71cd-48e3-b0cc-25cd19d5eb9c task=-180000 

2026-07-20 14:58:09.333 info INFO - Planning record updated in OIC successfully. itemId=865462807473164289 

2026-07-20 14:58:09.310 info INFO - [SUCCESS] TaskDone event published. operation=PlanningModifySaved baseItemId=865462801668247553 traceId=1a638f18-bc73-49e0-bdfe-51150f7ad558 task=-180000 

2026-07-20 14:58:09.247 info INFO - Planning record updated in OIC successfully. itemId=865462801668247553 

2026-07-20 14:58:09.206 info INFO - [SUCCESS] TaskDone event published. operation=PlanningModifySaved baseItemId=865462772530417665 traceId=18e31691-1342-4827-a3fb-f42bfafaa039 task=-180000 

2026-07-20 14:58:09.184 info INFO - Planning record updated in OIC successfully. itemId=865462772530417665 """;


    @ConfigProperty(name = "azure.openai.endpoint")
    String endpoint;

    @ConfigProperty(name = "azure.openai.api-key")
    String apiKey;

    @ConfigProperty(name = "azure.openai.deployment-name")
    String deploymentName;

    private OpenAIClient openAIClient;

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
        LOG.infof("Processing message with Azure OpenAI: %s", userMessage);

        try {
            List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage(SYSTEM_PROMPT),
                new ChatRequestUserMessage(userMessage + USER_PROMPT)
            );

            ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);
            options.setMaxCompletionTokens(16384);

            ChatCompletions chatCompletions = openAIClient.getChatCompletions(deploymentName, options);

            String response = chatCompletions.getChoices().get(0).getMessage().getContent();
            LOG.infof("Received response from Azure OpenAI: %s", response);

            return new ChatResponse(response);
        } catch (Exception e) {
            LOG.errorf(e, "Error calling Azure OpenAI: %s", e.getMessage());
            return new ChatResponse("Sorry, I encountered an error while processing your request. Please try again later.");
        }
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
