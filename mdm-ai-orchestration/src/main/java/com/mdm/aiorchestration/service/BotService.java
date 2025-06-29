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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public Map<String, Object> processUserMessage(String userMessage) {
        Map<String, Object> botResponse = Map.of("response", "I'm sorry, I couldn't understand that. Please try rephrasing or ask me about merge candidates, previewing merges, or merging all.");

        // Check if API key is configured for the selected provider
        if (!isApiKeyConfigured()) {
            return Map.of("response", getApiKeyNotConfiguredMessage());
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
            botResponse = Map.of("response", "I'm having trouble connecting to my brain. Please try again later. Error: " + e.getMessage());
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
    private Map<String, Object> processWithOpenAI(String userMessage) throws Exception {
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
                    return Map.of("response", "I got a response, but couldn't parse it. " + openaiText);
                }
            }
        }
        
        return Map.of("response", "I'm sorry, I couldn't get a proper response from the AI service.");
    }

    /**
     * Process user message with Gemini API.
     * @param userMessage The user's message.
     * @return The bot's response.
     */
    private Map<String, Object> processWithGemini(String userMessage) throws Exception {
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
                        return Map.of("response", "I got a response, but couldn't parse it. " + geminiText);
                    }
                }
            }
        }
        
        return Map.of("response", "I'm sorry, I couldn't get a proper response from the AI service.");
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
        prompt.append("   - Description: Use this when the user asks to see pending merge candidates. Displays a list of merge candidates waiting for review.\n");
        prompt.append("   - Use for: 'show pending merges', 'show candidates', 'what needs review', 'pending items'\n\n");

        prompt.append("2. **show_approved_candidates()**:\n");
        prompt.append("   - Description: Use this when the user asks to see approved/merged entities. Displays a list of merge candidates that have been approved.\n");
        prompt.append("   - Use for: 'show merged entities', 'show approved', 'show completed merges', 'approved items', 'show me merged records', 'merged records'\n\n");

        prompt.append("3. **show_rejected_candidates()**:\n");
        prompt.append("   - Description: Use this when the user asks to see rejected entities. Displays a list of merge candidates that have been rejected.\n");
        prompt.append("   - Use for: 'show rejected', 'show rejected entities', 'rejected items'\n\n");

        prompt.append("4. **show_all_candidates()**:\n");
        prompt.append("   - Description: Use this when the user asks to see all entities regardless of status. Displays all merge candidates.\n");
        prompt.append("   - Use for: 'show all', 'show all entities', 'show everything'\n\n");

        prompt.append("5. **preview_merge(candidateId)**:\n");
        prompt.append("   - Description: Use this when the user wants to see detailed information about a specific merge candidate.\n");
        prompt.append("   - Parameters: candidateId (number) - The ID of the merge candidate to preview.\n");
        prompt.append("   - Use for: 'preview merge <ID>', 'show details for <ID>', 'view candidate <ID>'\n\n");

        prompt.append("6. **merge_all()**:\n");
        prompt.append("   - Description: Use this when the user wants to approve all pending merge candidates at once.\n");
        prompt.append("   - Use for: 'merge all', 'approve all', 'approve everything'\n\n");

        prompt.append("7. **reject_candidate(candidateId, reason)**:\n");
        prompt.append("   - Description: Use this when the user wants to reject a specific merge candidate with a reason.\n");
        prompt.append("   - Parameters: candidateId (number) - The ID of the merge candidate to reject, reason (string) - The reason for rejection.\n");
        prompt.append("   - Use for: 'reject <ID> because <reason>', 'reject candidate <ID>'\n\n");

        prompt.append("8. **show_audit_logs(candidateId)**:\n");
        prompt.append("   - Description: Use this when the user wants to see the audit trail and reasoning for a specific merge candidate.\n");
        prompt.append("   - Parameters: candidateId (number) - The ID of the merge candidate to show audit logs for.\n");
        prompt.append("   - Use for: 'show audit logs for <ID>', 'show history for <ID>', 'show reasoning for <ID>'\n\n");

        prompt.append("IMPORTANT: Be flexible in understanding user intent. If someone asks for \"merges\", \"candidates\", \"pending items\", \"what needs review\", etc., they likely want to see candidates.\n");
        prompt.append("If the user's request does not fit any of the above tools, return a 'general_response' and respond conversationally.\n\n");

        prompt.append("IMPORTANT: After every action, always suggest the next possible actions the user can take. For example:\n");
        prompt.append("- After showing candidates: \"Next possible actions: preview merge <ID>, merge all, or ask me about specific candidates\"\n");
        prompt.append("- After previewing a merge: \"Next possible actions: approve this merge, reject with reason, or ask me about other candidates\"\n");
        prompt.append("- After rejecting: \"Next possible actions: review other candidates, show audit logs, or ask me for help\"\n\n");

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
     * @return A Map containing the response and candidates for the NLP dashboard.
     */
    private Map<String, Object> executeBotAction(String action, JsonNode data) {
        StringBuilder response = new StringBuilder();
        Map<String, Object> result = new HashMap<>();
        
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
                        response.append("\n\nNext possible actions: check back later, or ask me to help you with other tasks.");
                        result.put("response", response.toString());
                    } else {
                        response.append("Found ").append(candidates.size()).append(" pending merge candidates:\n\n");
                        for (MergeCandidatePair candidate : candidates) {
                            response.append("ID: ").append(candidate.getId()).append(" - Status: ").append(candidate.getStatus()).append("\n");
                        }
                        response.append("\nNext possible actions: preview merge <ID>, merge all, or ask me about specific candidates.");
                        
                        result.put("response", response.toString());
                        result.put("candidates", candidates);
                    }
                    break;

                case "show_approved_candidates":
                    List<MergeCandidatePair> approvedCandidates = webClient.get()
                            .uri(botCoreBaseUrl + "/api/merge/candidates/approved")
                            .retrieve()
                            .bodyToFlux(MergeCandidatePair.class)
                            .collectList()
                            .block();

                    if (approvedCandidates == null || approvedCandidates.isEmpty()) {
                        response.append("There are no approved merge candidates at the moment.");
                        response.append("\n\nNext possible actions: check pending candidates, or ask me to help you with other tasks.");
                        result.put("response", response.toString());
                    } else {
                        response.append("Found ").append(approvedCandidates.size()).append(" approved merge candidates (merged records):\n\n");
                        for (MergeCandidatePair candidate : approvedCandidates) {
                            response.append("ID: ").append(candidate.getId()).append(" - Status: ").append(candidate.getStatus()).append("\n");
                        }
                        response.append("\nNext possible actions: preview merge <ID>, show pending candidates, or ask me for help.");
                        
                        result.put("response", response.toString());
                        result.put("candidates", approvedCandidates);
                    }
                    break;

                case "show_rejected_candidates":
                    List<MergeCandidatePair> rejectedCandidates = webClient.get()
                            .uri(botCoreBaseUrl + "/api/merge/candidates/rejected")
                            .retrieve()
                            .bodyToFlux(MergeCandidatePair.class)
                            .collectList()
                            .block();

                    if (rejectedCandidates == null || rejectedCandidates.isEmpty()) {
                        response.append("There are no rejected merge candidates at the moment.");
                        response.append("\n\nNext possible actions: check pending candidates, or ask me to help you with other tasks.");
                        result.put("response", response.toString());
                    } else {
                        response.append("Found ").append(rejectedCandidates.size()).append(" rejected merge candidates:\n\n");
                        for (MergeCandidatePair candidate : rejectedCandidates) {
                            response.append("ID: ").append(candidate.getId()).append(" - Status: ").append(candidate.getStatus()).append("\n");
                        }
                        response.append("\nNext possible actions: preview merge <ID>, show pending candidates, or ask me for help.");
                        
                        result.put("response", response.toString());
                        result.put("candidates", rejectedCandidates);
                    }
                    break;

                case "show_all_candidates":
                    List<MergeCandidatePair> allCandidates = webClient.get()
                            .uri(botCoreBaseUrl + "/api/merge/candidates/all")
                            .retrieve()
                            .bodyToFlux(MergeCandidatePair.class)
                            .collectList()
                            .block();

                    if (allCandidates == null || allCandidates.isEmpty()) {
                        response.append("There are no merge candidates in the system at the moment.");
                        response.append("\n\nNext possible actions: check back later, or ask me to help you with other tasks.");
                        result.put("response", response.toString());
                    } else {
                        response.append("Found ").append(allCandidates.size()).append(" total merge candidates:\n\n");
                        for (MergeCandidatePair candidate : allCandidates) {
                            response.append("ID: ").append(candidate.getId()).append(" - Status: ").append(candidate.getStatus()).append("\n");
                        }
                        response.append("\nNext possible actions: preview merge <ID>, show specific status candidates, or ask me for help.");
                        
                        result.put("response", response.toString());
                        result.put("candidates", allCandidates);
                    }
                    break;

                case "preview_merge":
                    if (data.has("candidateId") && data.get("candidateId").isNumber()) {
                        Long candidateId = data.get("candidateId").asLong();
                        
                        // Get all pending candidates and find the one with matching ID
                        List<MergeCandidatePair> pendingCandidates = webClient.get()
                                .uri(botCoreBaseUrl + "/api/merge/candidates/pending-review")
                                .retrieve()
                                .bodyToFlux(MergeCandidatePair.class)
                                .collectList()
                                .block();

                        Optional<MergeCandidatePair> candidateOpt = pendingCandidates != null ? 
                            pendingCandidates.stream().filter(c -> c.getId().equals(candidateId)).findFirst() : 
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
                            response.append("Next possible actions: approve this merge, reject with reason, or ask me about other candidates.");
                            response.append("\nFor historical reasoning, ask me 'show audit logs for ").append(candidate.getId()).append("'.");
                        } else {
                            response.append("Merge candidate with ID ").append(candidateId).append(" not found.");
                            response.append("\n\nNext possible actions: check the candidate ID, show all candidates, or ask me for help.");
                        }
                    } else {
                        response.append("Please provide a valid candidate ID for preview. Example: 'preview merge 123'.");
                        response.append("\n\nNext possible actions: show all candidates, or ask me for help.");
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
                        response.append("\n\nNext possible actions: check back later, or ask me to help you with other tasks.");
                        result.put("response", response.toString());
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
                        response.append("\n\nNext possible actions: refresh the dashboard to see updated status, or ask me for help.");
                        result.put("response", response.toString());
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
                        response.append("\nNext possible actions: review other candidates, preview merge <ID>, or ask me for help.");
                    } else {
                        response.append("Please provide a valid candidate ID to show audit logs. Example: 'show audit logs for 123'.");
                    }
                    break;
                case "reject_candidate":
                    if (data.has("candidateId") && data.get("candidateId").isNumber() && data.has("reason")) {
                        Long candidateId = data.get("candidateId").asLong();
                        String reason = data.get("reason").asText();
                        
                        Map<String, String> requestBody = Map.of(
                                "status", MergeCandidatePair.MergeStatus.REJECTED.name(),
                                "comment", "Rejected by bot: " + reason
                        );
                        
                        Optional<MergeCandidatePair> rejected = webClient.put()
                                .uri(botCoreBaseUrl + "/api/merge/candidates/{id}/status", candidateId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(requestBody))
                                .retrieve()
                                .bodyToMono(MergeCandidatePair.class)
                                .blockOptional();
                        
                        if (rejected.isPresent()) {
                            response.append("Successfully rejected merge candidate ID: ").append(candidateId).append("\n");
                            response.append("Reason: ").append(reason).append("\n\n");
                            response.append("Next possible actions: review other candidates, show audit logs, or ask me for help.");
                        } else {
                            response.append("Failed to reject merge candidate ID: ").append(candidateId).append(". Candidate not found.");
                        }
                    } else {
                        response.append("Please provide a valid candidate ID and reason to reject a candidate. Example: 'reject candidate 123 because it's a false positive'.");
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
                    response.append("\n\nNext possible actions: show candidates, preview merge <ID>, merge all, or ask me for help.");
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error executing bot action '" + action + "': " + e.getMessage());
            e.printStackTrace();
            response.append("An error occurred while trying to fulfill your request: ").append(e.getMessage());
        }
        result.put("response", response.toString());
        return result;
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
