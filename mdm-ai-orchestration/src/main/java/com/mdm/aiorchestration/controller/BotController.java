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
    @PostMapping("/bot-chat")
    public ResponseEntity<?> botChat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        try {
            Map<String, Object> response = botService.processUserMessage(message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error processing bot request: " + e.getMessage()));
        }
    }
}
