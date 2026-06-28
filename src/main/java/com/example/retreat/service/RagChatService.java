package com.example.retreat.service;

import com.example.retreat.advisor.LoggingAdvisor;
import com.example.retreat.config.RetreatConfig;
import com.example.retreat.dto.ChatResponse;
import com.example.retreat.dto.SearchConfigDto;
import com.example.retreat.rag.SearchConfigService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
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
    private final SearchConfigService searchConfigService;

    /**
     * @param chatClient          базовый ChatClient
     * @param chatMemory          память диалога
     * @param loggingAdvisor      логирование prompt
     * @param vectorStore         pgvector store
     * @param searchConfigService настройки similarity
     */
    public RagChatService(ChatClient chatClient,
                          ChatMemory chatMemory,
                          LoggingAdvisor loggingAdvisor,
                          VectorStore vectorStore,
                          SearchConfigService searchConfigService) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.loggingAdvisor = loggingAdvisor;
        this.vectorStore = vectorStore;
        this.searchConfigService = searchConfigService;
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
        SearchConfigDto config = searchConfigService.getConfig();
        SearchRequest searchRequest = SearchRequest.builder()
                .similarityThreshold(config.similarityThreshold())
                .topK(config.topK())
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
