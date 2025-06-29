package com.mdm.aiorchestration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdm.aiorchestration.service.BotService;
import com.mdm.botcore.domain.model.MergeCandidatePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/bot")
public class NlpBotController {
    private final BotService botService;
    private final ObjectMapper objectMapper;

    @Autowired
    public NlpBotController(BotService botService, ObjectMapper objectMapper) {
        this.botService = botService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/nlp-chat")
    public ResponseEntity<?> nlpChat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        try {
            Map<String, Object> response = botService.processUserMessage(message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error processing NLP bot request: " + e.getMessage()));
        }
    }
    
    @PostMapping("/nlp-chat-json")
    public ResponseEntity<?> nlpChatJson(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        try {
            Map<String, Object> response = botService.processUserMessage(message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error processing NLP bot request: " + e.getMessage()));
        }
    }
    
    private List<Map<String, Object>> extractCandidatesFromText(String text) {
        List<Map<String, Object>> candidates = new ArrayList<>();
        
        // Pattern to match lines like: "• ID: 2, Status: PENDING_REVIEW, Entity 1: Global Pharmaceuticals (org-004), Entity 2: Global Pharmaceuticals (org-005)"
        Pattern pattern = Pattern.compile("• ID: (\\d+), Status: ([^,]+), Entity 1: ([^(]+) \\(([^)]+)\\), Entity 2: ([^(]+) \\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            Map<String, Object> candidate = new HashMap<>();
            candidate.put("id", Long.parseLong(matcher.group(1)));
            candidate.put("status", matcher.group(2));
            
            Map<String, Object> entity1 = new HashMap<>();
            entity1.put("name", matcher.group(3).trim());
            entity1.put("org", matcher.group(4));
            candidate.put("entity1", entity1);
            
            Map<String, Object> entity2 = new HashMap<>();
            entity2.put("name", matcher.group(5).trim());
            entity2.put("org", matcher.group(6));
            candidate.put("entity2", entity2);
            
            candidates.add(candidate);
        }
        
        return candidates;
    }
} 