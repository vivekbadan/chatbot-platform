package com.chatbot.engine.service;

import org.springframework.stereotype.Service;

@Service
public class ChatService {

    public String getResponse(String message) {

        if(message.equalsIgnoreCase("hi")) {
            return "Hello!";
        }

        if(message.equalsIgnoreCase("how are you")) {
            return "I am fine. How can I help you?";
        }

        if(message.equalsIgnoreCase("bye")) {
            return "Goodbye!";
        }

        return "Sorry, I don't understand.";
    }
}