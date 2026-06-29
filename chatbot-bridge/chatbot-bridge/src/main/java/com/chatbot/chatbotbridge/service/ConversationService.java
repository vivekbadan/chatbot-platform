package com.chatbot.chatbotbridge.service;

import com.chatbot.chatbotbridge.entity.Conversation;
import com.chatbot.chatbotbridge.repository.ConversationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository repository;

    public ConversationService(ConversationRepository repository) {
        this.repository = repository;
    }

    // Create Conversation
    public Conversation createConversation(Conversation conversation) {
        return repository.save(conversation);
    }

    // Get All Conversations
    public List<Conversation> getAllConversations() {
        return repository.findAll();
    }

    // Get Conversation By ID
    public Conversation getConversationById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Conversation not found"));
    }

    // Update Conversation
    public Conversation updateConversation(Long id, Conversation updatedConversation) {

        Conversation conversation = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Conversation not found"));

        conversation.setUserId(updatedConversation.getUserId());
        conversation.setOrganizationId(updatedConversation.getOrganizationId());

        return repository.save(conversation);
    }

    // Delete Conversation
    public void deleteConversation(Long id) {

        Conversation conversation = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Conversation not found"));

        repository.delete(conversation);
    }
}