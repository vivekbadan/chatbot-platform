package com.chatbot.chatbotbridge.repository;

import com.chatbot.chatbotbridge.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

}