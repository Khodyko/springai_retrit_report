package com.example.retreat.service;

import com.example.retreat.advisor.LoggingAdvisor;
import com.example.retreat.config.RetreatConfig;
import com.example.retreat.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * RAG-чат: advisors и system prompt подключаются в методах сервиса.
 */
@Service
public class RagChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final LoggingAdvisor loggingAdvisor;
    private final VectorStore vectorStore;
    private final double similarityThreshold;
    private final int topK;

    /**
     * @param chatClient            базовый ChatClient
     * @param chatMemory            память диалога
     * @param loggingAdvisor        логирование prompt
     * @param vectorStore           pgvector store
     * @param similarityThreshold   порог из application.yml
     * @param topK                  topK из application.yml
     */
    public RagChatService(ChatClient chatClient,
                          ChatMemory chatMemory,
                          LoggingAdvisor loggingAdvisor,
                          VectorStore vectorStore,
                          @Value("${retreat.search.similarity-threshold}") double similarityThreshold,
                          @Value("${retreat.search.top-k}") int topK) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.loggingAdvisor = loggingAdvisor;
        this.vectorStore = vectorStore;
        this.similarityThreshold = similarityThreshold;
        this.topK = topK;
    }

    /**
     * Отвечает на вопрос с RAG или без (блок 1).
     *
     * @param message текст вопроса
     * @param useRag  включить RAG-advisor
     * @param userId  X-User-Id для памяти диалога
     * @return ответ
     */
    public ChatResponse chat(String message, boolean useRag, String userId) {
        if (!useRag) {
            return chatWithoutRag(message);
        }
        return chatWithRag(message, userId);
    }

    /**
     * Простой вызов LLM без advisors (блок 1 доклада).
     *
     * @param message текст вопроса
     * @return ответ
     */
    private ChatResponse chatWithoutRag(String message) {
        return new ChatResponse(chatClient.prompt().user(message).call().content());
    }

    /**
     * RAG-чат: system prompt, память, логирование, QuestionAnswerAdvisor.
     *
     * @param message текст вопроса
     * @param userId  идентификатор пользователя для памяти (может быть null)
     * @return ответ
     */
    private ChatResponse chatWithRag(String message, String userId) {
        SearchRequest searchRequest = SearchRequest.builder()
                .similarityThreshold(similarityThreshold)
                .topK(topK)
                .build();

        var prompt = chatClient.prompt()
                .system(RetreatConfig.SYSTEM_PROMPT)
                .advisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        loggingAdvisor,
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(searchRequest)
                                .build()
                )
                .user(message);

        if (userId != null && !userId.isBlank()) {
            prompt = prompt.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId));
        }

        return new ChatResponse(prompt.call().content());
    }
}
