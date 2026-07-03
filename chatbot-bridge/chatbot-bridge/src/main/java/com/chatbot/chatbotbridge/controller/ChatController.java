package com.chatbot.chatbotbridge.controller;
import com.chatbot.chatbotbridge.entity.Conversation;
import com.chatbot.chatbotbridge.entity.Message;
import org.springframework.web.client.RestTemplate;

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

import org.springframework.web.util.UriComponentsBuilder;
import com.chatbot.chatbotbridge.dto.UserContextResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@CrossOrigin(origins = "*")
@RestController
public class ChatController {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final RestTemplate restTemplate;

    public ChatController(ConversationService conversationService,
                          MessageService messageService,
                          RestTemplate restTemplate) {

        this.conversationService = conversationService;
        this.messageService = messageService;
        this.restTemplate = restTemplate;
    }
    @Operation(
            summary = "Chat with AI Assistant",
            description = "Receives a user message, fetches user context from the Organization Service, sends the prompt to the FastAPI LLM engine, stores the conversation, and returns the AI response."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI response generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/chat")
    public String chat(

            @Parameter(description = "Conversation ID")
            @RequestParam Long conversationId,

            @Parameter(description = "User message")
            @RequestParam String message,

            @Parameter(description = "User ID extracted from JWT")
            @RequestHeader(value = "X-User-Id", required = false)
            String userId
    ) {
        {

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

            String url =
                    "http://localhost:8081/organizations/internal/context?userId=" + userId;

            UserContextResponse context =
                    restTemplate.getForObject(
                            url,
                            UserContextResponse.class
                    );

            System.out.println("========== USER CONTEXT ==========");
            System.out.println("User ID = " + context.getUserId());
            System.out.println("Organization ID = " + context.getOrganizationId());
            System.out.println("Organization Name = " + context.getOrganizationName());
            System.out.println("Project Name = " + context.getProjectName());

//        RestTemplate restTemplate = new RestTemplate();
            String prompt =
                    "User Information:\n" +
                            "User ID: " + context.getUserId() + "\n" +
                            "Organization: " + context.getOrganizationName() + "\n" +
                            "Project: " + context.getProjectName() + "\n\n" +
                            "User Message:\n" +
                            message;

            System.out.println("========== AI PROMPT ==========");
            System.out.println(prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Key", "chatbot-secret-key");

            System.out.println("Calling FastAPI...");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String finalUrl = UriComponentsBuilder
                    .fromHttpUrl("http://localhost:8000/chat")
                    .queryParam("message", prompt)
                    .build()
                    .encode()
                    .toUriString();

            System.out.println("========== FINAL URL ==========");
            System.out.println(finalUrl);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            finalUrl,
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
}