package com.chatbot.chatbotbridge.controller;
import com.chatbot.chatbotbridge.entity.Conversation;
import com.chatbot.chatbotbridge.entity.Message;

import com.chatbot.chatbotbridge.entity.Conversation;
import com.chatbot.chatbotbridge.service.ConversationService;
import com.chatbot.chatbotbridge.service.MessageService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@CrossOrigin(origins = "*")
@RestController
public class ChatController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    public ChatController(ConversationService conversationService,
                          MessageService messageService) {
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    @GetMapping("/chat")
    public String chat(
            @RequestParam Long conversationId,
            @RequestParam String message,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        System.out.println("===== ChatController Called =====");
        System.out.println("Conversation ID = " + conversationId);
        System.out.println("Message = " + message);
        System.out.println("Received X-User-Id = " + userId);

        // Load the conversation from the database
        Conversation conversation =
                conversationService.getConversationById(conversationId);
        Message userMessage = new Message();

        userMessage.setConversation(conversation);
        userMessage.setRole("USER");
        userMessage.setContent(message);

        messageService.saveMessage(userMessage);

        System.out.println("User message saved successfully.");

        System.out.println("Conversation Loaded = " + conversation.getId());

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Key", "chatbot-secret-key");

        System.out.println("Calling FastAPI...");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "http://localhost:8000/chat?message=" + message,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

        String aiResponse = response.getBody();

        System.out.println("FastAPI Response = " + aiResponse);

// Save AI response
        Message assistantMessage = new Message();

        assistantMessage.setConversation(conversation);
        assistantMessage.setRole("ASSISTANT");
        assistantMessage.setContent(aiResponse);

        messageService.saveMessage(assistantMessage);

        System.out.println("AI message saved successfully.");

        return aiResponse;
    }
}