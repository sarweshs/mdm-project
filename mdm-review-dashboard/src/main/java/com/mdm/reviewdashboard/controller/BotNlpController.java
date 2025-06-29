package com.mdm.reviewdashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/bot")
public class BotNlpController {

    private final WebClient webClient;
    private final String aiOrchestrationBaseUrl;

    @Autowired
    public BotNlpController(WebClient.Builder webClientBuilder, 
                          @Value("${mdm.ai-orchestration.base-url}") String aiOrchestrationBaseUrl) {
        this.webClient = webClientBuilder.build();
        this.aiOrchestrationBaseUrl = aiOrchestrationBaseUrl;
    }

    @PostMapping("/nlp-chat")
    public Mono<ResponseEntity<Map>> nlpChat(@RequestBody Map<String, String> request) {
        return webClient.post()
                .uri(aiOrchestrationBaseUrl + "/api/bot/nlp-chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> ResponseEntity.ok(response))
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    @PostMapping("/nlp-chat-json")
    public Mono<ResponseEntity<Map>> nlpChatJson(@RequestBody Map<String, String> request) {
        return webClient.post()
                .uri(aiOrchestrationBaseUrl + "/api/bot/nlp-chat-json")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .toEntity(Map.class);
    }
} 