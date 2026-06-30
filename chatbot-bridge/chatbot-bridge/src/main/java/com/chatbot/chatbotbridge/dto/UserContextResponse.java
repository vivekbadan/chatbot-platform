package com.chatbot.chatbotbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContextResponse {

    private String userId;
    private String organizationId;
    private String organizationName;
    private String projectName;
}