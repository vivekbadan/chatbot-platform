package com.chatbot.chatbotbridge;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return "Bridge received: " + message;
    }
}