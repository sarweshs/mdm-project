// mdm-ai-orchestration/src/main/java/com/mdm/aiorchestration/controller/BotController.java
package com.mdm.aiorchestration.controller;

import com.mdm.aiorchestration.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller for the chat bot functionality.
 * Receives user messages and returns bot responses.
 * This controller now resides in the mdm-ai-orchestration service.
 */
@RestController
@RequestMapping("/api/bot")
public class BotController {

    private final BotService botService;

    @Autowired
    public BotController(BotService botService) {
        this.botService = botService;
    }

    /**
     * Receives a user message and returns the bot's response.
     * @param request A map containing the "message" from the user.
     * @return ResponseEntity with the bot's response message.
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chatWithBot(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("response", "Please provide a message."));
        }

        String botResponse = botService.processUserMessage(userMessage);
        return ResponseEntity.ok(Map.of("response", botResponse));
    }
}
