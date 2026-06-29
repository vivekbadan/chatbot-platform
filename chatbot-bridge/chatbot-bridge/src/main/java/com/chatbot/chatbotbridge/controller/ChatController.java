package com.chatbot.chatbotbridge;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.RequestHeader;

@CrossOrigin(origins = "*")
@RestController
public class ChatController {

    @GetMapping("/chat")
    public String chat(
            @RequestParam String message,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        System.out.println("===== ChatController Called =====");
        System.out.println("Message = " + message);
        System.out.println("Received X-User-Id = " + userId);

        RestTemplate restTemplate = new RestTemplate();

        org.springframework.http.HttpHeaders headers =
                new org.springframework.http.HttpHeaders();

        headers.set("X-Internal-Key", "chatbot-secret-key");

        System.out.println("Calling FastAPI...");

        org.springframework.http.HttpEntity<String> entity =
                new org.springframework.http.HttpEntity<>(headers);

        org.springframework.http.ResponseEntity<String> response =
                restTemplate.exchange(
                        "http://localhost:8000/chat?message=" + message,
                        org.springframework.http.HttpMethod.GET,
                        entity,
                        String.class
                );

        System.out.println("FastAPI Response = " + response.getBody());

        return response.getBody();
    }



}