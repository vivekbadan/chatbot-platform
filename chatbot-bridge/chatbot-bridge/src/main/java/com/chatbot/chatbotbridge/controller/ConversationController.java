package com.chatbot.chatbotbridge.controller;

import com.chatbot.chatbotbridge.entity.Conversation;
import com.chatbot.chatbotbridge.service.ConversationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService service;

    public ConversationController(ConversationService service) {
        this.service = service;
    }

    // Create Conversation
    @PostMapping
    public Conversation createConversation(
            @RequestBody Conversation conversation,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        System.out.println("===== CREATE CONVERSATION CALLED =====");

        conversation.setUserId(userId);

        return service.createConversation(conversation);
    }
    // Get All Conversations
    @GetMapping
    public List<Conversation> getAllConversations() {
        return service.getAllConversations();
    }

    // Get Conversation By ID
    @GetMapping("/{id}")
    public Conversation getConversationById(@PathVariable Long id) {
        return service.getConversationById(id);
    }

    // Update Conversation
    @PatchMapping("/{id}")
    public Conversation updateConversation(
            @PathVariable Long id,
            @RequestBody Conversation conversation) {

        return service.updateConversation(id, conversation);
    }


    // Delete Conversation
    @DeleteMapping("/{id}")
    public String deleteConversation(@PathVariable Long id) {

        service.deleteConversation(id);

        return "Conversation deleted successfully";
    }
}