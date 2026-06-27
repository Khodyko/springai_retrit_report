package com.example.retreat.service;

import com.example.retreat.config.RetreatConfig;
import com.example.retreat.tools.RetreatKnowledgeSearchTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

/**
 * Agent-чат с search-tool (без RAG-advisor) — блок 4.
 */
@Service
public class AgentChatService {

    private final ChatClient agentChatClient;

    /**
     * @param chatModel  модель OpenAI
     * @param searchTool поиск по metadata
     */
    public AgentChatService(ChatModel chatModel, RetreatKnowledgeSearchTool searchTool) {
        this.agentChatClient = ChatClient.builder(chatModel)
                .defaultSystem(RetreatConfig.SYSTEM_PROMPT)
                .defaultTools(searchTool)
                .build();
    }

    /**
     * Отвечает на вопрос через function calling.
     *
     * @param message текст вопроса
     * @return ответ модели
     */
    public String chat(String message) {
        return agentChatClient.prompt().user(message).call().content();
    }
}
