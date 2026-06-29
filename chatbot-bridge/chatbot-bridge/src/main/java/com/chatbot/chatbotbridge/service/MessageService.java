package com.chatbot.chatbotbridge.service;

import com.chatbot.chatbotbridge.entity.Message;
import com.chatbot.chatbotbridge.repository.MessageRepository;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = repository;
    }

    public Message saveMessage(Message message) {
        return repository.save(message);
    }
}