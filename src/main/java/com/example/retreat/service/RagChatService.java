package com.example.retreat.service;

import com.example.retreat.dto.ChatResponse;
import com.example.retreat.dto.SearchConfigDto;
import com.example.retreat.rag.SearchConfigService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * RAG-чат: QuestionAnswerAdvisor + настройки из application.yml.
 */
@Service
public class RagChatService {

    private final ChatClient chatClient;
    private final ChatClient simpleChatClient;
    private final VectorStore vectorStore;
    private final SearchConfigService searchConfigService;

    /**
     * @param chatClient          основной ChatClient с памятью и advisors
     * @param simpleChatClient    ChatClient без RAG (блок 1)
     * @param vectorStore         pgvector store
     * @param searchConfigService настройки similarity
     */
    public RagChatService(ChatClient chatClient,
                          @Qualifier("simpleChatClient") ChatClient simpleChatClient,
                          VectorStore vectorStore,
                          SearchConfigService searchConfigService) {
        this.chatClient = chatClient;
        this.simpleChatClient = simpleChatClient;
        this.vectorStore = vectorStore;
        this.searchConfigService = searchConfigService;
    }

    /**
     * Отвечает на вопрос с RAG или без (блок 1).
     *
     * @param message текст вопроса
     * @param useRag  включить RAG-advisor
     * @param userId  идентификатор пользователя для памяти (может быть null)
     * @return ответ
     */
    public ChatResponse chat(String message, boolean useRag, String userId) {
        if (!useRag) {
            return new ChatResponse(simpleChatClient.prompt().user(message).call().content());
        }

        SearchConfigDto config = searchConfigService.getConfig();
        SearchRequest searchRequest = SearchRequest.builder()
                .similarityThreshold(config.similarityThreshold())
                .topK(config.topK())
                .build();

        var prompt = chatClient.prompt()
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(searchRequest)
                        .build())
                .user(message);

        if (userId != null && !userId.isBlank()) {
            prompt = prompt.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId));
        }

        return new ChatResponse(prompt.call().content());
    }
}
