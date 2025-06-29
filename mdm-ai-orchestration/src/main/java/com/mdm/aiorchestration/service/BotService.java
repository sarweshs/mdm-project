package com.mdm.aiorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdm.botcore.domain.model.AuditLog; // Reusing model from mdm-bot-core
import com.mdm.botcore.domain.model.MDMEntity; // Reusing model from mdm-bot-core
import com.mdm.botcore.domain.model.MergeCandidatePair; // Reusing model from mdm-bot-core
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map; // Import Map

/**
 * Service to interact with LLM APIs (OpenAI or Gemini) and orchestrate bot capabilities
 * by calling other MDM backend services.
 */
@Service
public class BotService {

    private final WebClient webClient; // For calling LLM APIs and mdm-bot-core
    private final ObjectMapper objectMapper;

    @Value("${llm.provider:openai}") // Default to OpenAI
    private String llmProvider;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String openaiModel;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${mdm.bot.core.base-url}")
    private String botCoreBaseUrl; // Base URL for the mdm-bot-core service

    public BotService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        // Build WebClient. Base URLs for specific services will be configured dynamically.
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Processes a user's chat message using the configured LLM provider to understand intent
     * and then orchestrates calls to relevant backend services.
     * @param userMessage The message from the user.
     * @return The bot's response.
     */
    public String processUserMessage(String userMessage) {
        String botResponse = "I'm sorry, I couldn't understand that. Please try rephrasing or ask me about merge candidates, previewing merges, or merging all.";

        // Check if API key is configured for the selected provider
        if (!isApiKeyConfigured()) {
            return getApiKeyNotConfiguredMessage();
        }

        try {
            // Process with the configured LLM provider
            switch (llmProvider.toLowerCase()) {
                case "gemini":
                    return processWithGemini(userMessage);
                case "openai":
                default:
                    return processWithOpenAI(userMessage);
            }
        } catch (Exception e) {
            System.err.println("Error communicating with " + llmProvider + " API: " + e.getMessage());
            e.printStackTrace();
            botResponse = "I'm having trouble connecting to my brain. Please try again later. Error: " + e.getMessage();
        }
        return botResponse;
    }

    /**
     * Check if the API key is configured for the selected provider.
     * @return true if API key is configured, false otherwise.
     */
    private boolean isApiKeyConfigured() {
        switch (llmProvider.toLowerCase()) {
            case "gemini":
                return geminiApiKey != null && !geminiApiKey.trim().isEmpty() && 
                       !geminiApiKey.equals("YOUR_GEMINI_API_KEY_HERE");
            case "openai":
            default:
                return openaiApiKey != null && !openaiApiKey.trim().isEmpty() && 
                       !openaiApiKey.equals("YOUR_OPENAI_API_KEY_HERE");
        }
    }

    /**
     * Get appropriate message when API key is not configured.
     * @return Configuration message.
     */
    private String getApiKeyNotConfiguredMessage() {
        switch (llmProvider.toLowerCase()) {
            case "gemini":
                return "I'm sorry, but my AI capabilities are not configured. Please ask the administrator to configure the Gemini API key in the application.properties file. For now, I can help you with basic commands like 'show candidates' or 'merge all'.";
            case "openai":
            default:
                return "I'm sorry, but my AI capabilities are not configured. Please ask the administrator to configure the OpenAI API key in the application.properties file. For now, I can help you with basic commands like 'show candidates' or 'merge all'.";
        }
    }

    /**
     * Process user message with OpenAI API.
     * @param userMessage The user's message.
     * @return The bot's response.
     */
    private String processWithOpenAI(String userMessage) throws Exception {
        // Construct the prompt for the LLM
        String prompt = createLlmPrompt(userMessage);

        // Construct the payload for the OpenAI API
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", openaiModel);
        
        ArrayNode messagesArray = payload.putArray("messages");
        ObjectNode systemMessage = messagesArray.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are an MDM (Master Data Management) assistant bot. Your purpose is to help human agents manage merge candidates in a data quality system. Always respond with valid JSON containing 'action' and 'data' fields.");
        
        ObjectNode userMessageObj = messagesArray.addObject();
        userMessageObj.put("role", "user");
        userMessageObj.put("content", prompt);

        // Add response format for structured JSON
        ObjectNode responseFormat = payload.putObject("response_format");
        responseFormat.put("type", "json_object");

        // Make the API call to OpenAI
        String openaiApiUrl = "https://api.openai.com/v1/chat/completions";
        JsonNode openaiResult = webClient.post()
                .uri(openaiApiUrl)
                .header("Authorization", "Bearer " + openaiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(objectMapper.writeValueAsString(payload)))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        System.out.println("OpenAI Raw Response: " + openaiResult.toPrettyString());

        if (openaiResult != null && openaiResult.has("choices")) {
            JsonNode choice = openaiResult.get("choices").get(0);
            if (choice != null && choice.has("message") && choice.get("message").has("content")) {
                String openaiText = choice.get("message").get("content").asText();
                System.out.println("OpenAI Parsed Text: " + openaiText);

                try {
                    JsonNode parsedJson = objectMapper.readTree(openaiText);
                    String action = parsedJson.has("action") ? parsedJson.get("action").asText() : "general_response";
                    JsonNode data = parsedJson.has("data") ? parsedJson.get("data") : objectMapper.createObjectNode();

                    return executeBotAction(action, data);

                } catch (JsonProcessingException e) {
                    System.err.println("Error parsing OpenAI JSON response: " + e.getMessage());
                    return "I got a response, but couldn't parse it. " + openaiText;
                }
            }
        }
        
        return "I'm sorry, I couldn't get a proper response from the AI service.";
    }

    /**
     * Process user message with Gemini API.
     * @param userMessage The user's message.
     * @return The bot's response.
     */
    private String processWithGemini(String userMessage) throws Exception {
        // Construct the prompt for the LLM
        String prompt = createLlmPrompt(userMessage);

        // Construct the payload for the Gemini API
        ObjectNode payload = objectMapper.createObjectNode();
        ArrayNode contentsArray = payload.putArray("contents");
        ObjectNode userPart = contentsArray.addObject();
        ArrayNode partsArray = userPart.putArray("parts");
        partsArray.addObject().put("text", prompt);

        // Add generationConfig for structured JSON response for tool calls
        ObjectNode generationConfig = payload.putObject("generationConfig");
        generationConfig.put("responseMimeType", "application/json");

        ObjectNode responseSchema = generationConfig.putObject("responseSchema");
        responseSchema.put("type", "OBJECT");
        ObjectNode properties = responseSchema.putObject("properties");

        // Define the schema for the tool call
        ObjectNode actionProperty = properties.putObject("action");
        actionProperty.put("type", "STRING");
        actionProperty.put("description", "The action to perform (e.g., 'show_candidates', 'preview_merge', 'merge_all', 'general_response', 'show_audit_logs')");

        ObjectNode dataProperty = properties.putObject("data");
        dataProperty.put("type", "OBJECT");
        dataProperty.put("description", "Any data needed for the action, such as candidateId for preview_merge.");
        dataProperty.put("additionalProperties", true); // Allow arbitrary properties

        ArrayNode propertyOrdering = responseSchema.putArray("propertyOrdering");
        propertyOrdering.add("action");
        propertyOrdering.add("data");

        // Make the fetch call to Gemini API
        String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;
        JsonNode geminiResult = webClient.post()
                .uri(geminiApiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(objectMapper.writeValueAsString(payload)))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(); // Block for simplicity in this example

        System.out.println("Gemini Raw Response: " + geminiResult.toPrettyString());

        if (geminiResult != null && geminiResult.has("candidates")) {
            JsonNode candidate = geminiResult.get("candidates").get(0);
            if (candidate != null && candidate.has("content") && candidate.get("content").has("parts")) {
                JsonNode parts = candidate.get("content").get("parts").get(0);
                if (parts != null && parts.has("text")) {
                    String geminiText = parts.get("text").asText();
                    System.out.println("Gemini Parsed Text: " + geminiText);

                    try {
                        JsonNode parsedJson = objectMapper.readTree(geminiText);
                        String action = parsedJson.has("action") ? parsedJson.get("action").asText() : "general_response";
                        JsonNode data = parsedJson.has("data") ? parsedJson.get("data") : objectMapper.createObjectNode();

                        return executeBotAction(action, data);

                    } catch (JsonProcessingException e) {
                        System.err.println("Error parsing Gemini JSON response: " + e.getMessage());
                        return "I got a response, but couldn't parse it. " + geminiText;
                    }
                }
            }
        }
        
        return "I'm sorry, I couldn't get a proper response from the AI service.";
    }

    /**
     * Constructs the detailed prompt for the LLM, including its role and available tools.
     * @param userMessage The user's input.
     * @return The complete prompt string.
     */
    private String createLlmPrompt(String userMessage) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an MDM (Master Data Management) assistant bot. Your purpose is to help human agents manage merge candidates in a data quality system.\n");
        prompt.append("You have access to the following tools/actions:\n\n");

        prompt.append("1. **show_candidates()**:\n");
        prompt.append("   - Description: Use this when the user asks to see pending merge candidates. Displays a list of merge candidates that are awaiting human review.\n");
        prompt.append("   - Example phrases: \"Show me merge candidates\", \"What needs review?\", \"List pending merges\"\n\n");

        prompt.append("2. **preview_merge(candidateId: Long)**:\n");
        prompt.append("   - Description: Use this when the user asks for a detailed preview of a specific merge candidate. Requires the ID of the candidate.\n");
        prompt.append("   - Example phrases: \"Preview merge 123\", \"Show details for candidate ID 45\", \"What's merge 789 about?\"\n");
        prompt.append("   - Important: If the user asks for a preview but doesn't provide an ID, ask them to provide one.\n\n");

        prompt.append("3. **merge_all()**:\n");
        prompt.append("   - Description: Use this when the user explicitly asks to approve and merge all currently pending candidates. This will approve all candidates listed as 'PENDING_REVIEW'.\n");
        prompt.append("   - Example phrases: \"Merge all pending\", \"Approve all candidates\", \"Process all merges at once\"\n\n");

        prompt.append("4. **show_audit_logs(candidateId: Long)**:\n");
        prompt.append("   - Description: Use this when the user asks to see the bot's audit logs or reasoning for a specific merge candidate. Requires the ID of the candidate.\n");
        prompt.append("   - Example phrases: \"Show audit logs for 123\", \"What was the bot's reasoning for candidate 45?\", \"View history for merge 789\"\n");
        prompt.append("   - Important: If the user asks for audit logs but doesn't provide an ID, ask them to provide one.\n\n");

        prompt.append("If the user's request does not fit any of the above tools, return a 'general_response' and respond conversationally.\n\n");

        prompt.append("Your response MUST be a JSON object with two fields: 'action' (string) and 'data' (object).\n");
        prompt.append("If the action is 'general_response', the 'data' object should contain a 'response' field with your conversational reply.\n");
        prompt.append("If the action is a tool call, the 'data' object should contain the necessary arguments for that tool.\n\n");

        prompt.append("User's Message: \"" + userMessage + "\"\n");
        prompt.append("Your JSON response:\n");

        return prompt.toString();
    }

    /**
     * Executes the bot action based on the LLM's parsed intent.
     * This method now directly calls the mdm-bot-core service.
     * @param action The action string (e.g., "show_candidates", "preview_merge").
     * @param data The JSON node containing parameters for the action.
     * @return A formatted string response for the user.
     */
    private String executeBotAction(String action, JsonNode data) {
        StringBuilder response = new StringBuilder();
        try {
            switch (action) {
                case "show_candidates":
                    List<MergeCandidatePair> candidates = webClient.get()
                            .uri(botCoreBaseUrl + "/api/merge/candidates/pending-review")
                            .retrieve()
                            .bodyToFlux(MergeCandidatePair.class)
                            .collectList()
                            .block();

                    if (candidates == null || candidates.isEmpty()) {
                        response.append("There are no pending merge candidates at the moment.");
                    } else {
                        response.append("Here are the pending merge candidates:\n\n");
                        for (MergeCandidatePair candidate : candidates) {
                            Optional<MDMEntity> entity1Opt = deserializeMdmEntity(candidate.getEntity1Json());
                            Optional<MDMEntity> entity2Opt = deserializeMdmEntity(candidate.getEntity2Json());

                            response.append("• ID: ").append(candidate.getId())
                                    .append(", Status: ").append(candidate.getStatus());

                            entity1Opt.ifPresent(e1 -> response.append(", Entity 1: ").append(e1.getName()).append(" (").append(e1.getId()).append(")"));
                            entity2Opt.ifPresent(e2 -> response.append(", Entity 2: ").append(e2.getName()).append(" (").append(e2.getId()).append(")"));
                            response.append("\n");
                        }
                        response.append("\nTo see more details, use 'preview merge <ID>'.");
                        response.append("\nTo approve all, type 'merge all'.");
                    }
                    break;
                case "preview_merge":
                    if (data.has("candidateId") && data.get("candidateId").isNumber()) {
                        Long candidateId = data.get("candidateId").asLong();
                        
                        // Get all pending candidates and find the one with matching ID
                        List<MergeCandidatePair> allCandidates = webClient.get()
                                .uri(botCoreBaseUrl + "/api/merge/candidates/pending-review")
                                .retrieve()
                                .bodyToFlux(MergeCandidatePair.class)
                                .collectList()
                                .block();

                        Optional<MergeCandidatePair> candidateOpt = allCandidates != null ? 
                            allCandidates.stream().filter(c -> c.getId().equals(candidateId)).findFirst() : 
                            Optional.empty();

                        if (candidateOpt.isPresent()) {
                            MergeCandidatePair candidate = candidateOpt.get();
                            MDMEntity entity1 = deserializeMdmEntity(candidate.getEntity1Json()).orElse(new MDMEntity());
                            MDMEntity entity2 = deserializeMdmEntity(candidate.getEntity2Json()).orElse(new MDMEntity());
                            MDMEntity proposed = deserializeMdmEntity(candidate.getProposedMergedEntityJson()).orElse(new MDMEntity());

                            response.append("Preview for Merge Candidate ID: ").append(candidate.getId()).append(":\n\n");
                            response.append("--- Original Entity 1 ---\n").append(entity1.toString()).append("\n");
                            response.append("Details: ").append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entity1)).append("\n\n");

                            response.append("--- Original Entity 2 ---\n").append(entity2.toString()).append("\n");
                            response.append("Details: ").append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entity2)).append("\n\n");

                            response.append("--- Proposed Merged Entity ---\n").append(proposed.toString()).append("\n");
                            response.append("Details: ").append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(proposed)).append("\n\n");

                            response.append("--- Bot's Reasoning ---\n").append(candidate.getReasoningJson()).append("\n\n");
                            response.append("You can approve or reject this merge on the dashboard.\n");
                            response.append("For historical reasoning, ask me 'show audit logs for ").append(candidate.getId()).append("'.");
                        } else {
                            response.append("Merge candidate with ID ").append(candidateId).append(" not found.");
                        }
                    } else {
                        response.append("Please provide a valid candidate ID for preview. Example: 'preview merge 123'.");
                    }
                    break;
                case "merge_all":
                    List<MergeCandidatePair> pending = webClient.get()
                            .uri(botCoreBaseUrl + "/api/merge/candidates/pending-review")
                            .retrieve()
                            .bodyToFlux(MergeCandidatePair.class)
                            .collectList()
                            .block();

                    if (pending == null || pending.isEmpty()) {
                        response.append("There are no pending merge candidates to merge all at once.");
                    } else {
                        int approvedCount = 0;
                        for (MergeCandidatePair candidate : pending) {
                            Map<String, String> requestBody = Map.of(
                                    "status", MergeCandidatePair.MergeStatus.APPROVED.name(),
                                    "comment", "Approved by bot 'merge all' command."
                            );
                            Optional<MergeCandidatePair> approved = webClient.put()
                                    .uri(botCoreBaseUrl + "/api/merge/candidates/{id}/status", candidate.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(BodyInserters.fromValue(requestBody))
                                    .retrieve()
                                    .bodyToMono(MergeCandidatePair.class)
                                    .blockOptional();
                            if (approved.isPresent()) {
                                approvedCount++;
                            }
                        }
                        response.append("Successfully initiated approval for ").append(approvedCount).append(" merge candidates.");
                        if (pending.size() > approvedCount) {
                            response.append(" ").append(pending.size() - approvedCount).append(" candidates could not be approved.");
                        }
                        response.append(" Please refresh the dashboard to see the updated status.");
                    }
                    break;
                case "show_audit_logs":
                    if (data.has("candidateId") && data.get("candidateId").isNumber()) {
                        Long candidateId = data.get("candidateId").asLong();
                        List<AuditLog> auditLogs = webClient.get()
                                .uri(botCoreBaseUrl + "/api/merge/candidates/{id}/audit-logs", candidateId)
                                .retrieve()
                                .bodyToFlux(AuditLog.class)
                                .collectList()
                                .block();

                        if (auditLogs == null || auditLogs.isEmpty()) {
                            response.append("No audit logs found for merge candidate ID ").append(candidateId).append(".");
                        } else {
                            response.append("Audit Logs for Merge Candidate ID ").append(candidateId).append(":\n\n");
                            for (AuditLog log : auditLogs) {
                                response.append("• Rule: ").append(log.getRuleName())
                                        .append("\n  Details: ").append(log.getRuleDetails())
                                        .append("\n  Decision: ").append(log.isBotDecisionToMerge() ? "MERGE" : "NO MERGE")
                                        .append("\n  Timestamp: ").append(log.getTimestamp()).append("\n\n");
                            }
                        }
                    } else {
                        response.append("Please provide a valid candidate ID to show audit logs. Example: 'show audit logs for 123'.");
                    }
                    break;
                case "general_response":
                default:
                    // Fallback for conversational responses
                    if (data.has("response")) {
                        response.append(data.get("response").asText());
                    } else {
                        response.append("I'm sorry, I couldn't understand that. Please try rephrasing or ask me about merge candidates, previewing merges, or merging all.");
                    }
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error executing bot action '" + action + "': " + e.getMessage());
            e.printStackTrace();
            response.append("An error occurred while trying to fulfill your request: ").append(e.getMessage());
        }
        return response.toString();
    }

    /**
     * Helper method to deserialize JSON string of MDMEntity back into MDMEntity object.
     * This helper is now part of BotService as it deals with MDMEntity objects fetched from bot-core.
     * @param jsonString The JSON string representation of an MDMEntity.
     * @return An Optional containing the MDMEntity if deserialization is successful.
     */
    private Optional<MDMEntity> deserializeMdmEntity(String jsonString) {
        try {
            return Optional.of(objectMapper.readValue(jsonString, MDMEntity.class));
        } catch (JsonProcessingException e) {
            System.err.println("Error deserializing MDMEntity from JSON: " + e.getMessage());
            return Optional.empty();
        }
    }
}
