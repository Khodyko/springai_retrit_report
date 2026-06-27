package com.example.retreat.config;

import com.example.retreat.advisor.LoggingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Spring AI: ChatClient, память диалога, системный промпт.
 */
@Configuration
public class RetreatConfig {

    /**
     * Системный промпт ассистента ретрита.
     */
    public static final String SYSTEM_PROMPT = """
            Ты — ассистент корпоративного ретрита.
            Отвечай на вопросы о ретрите ТОЛЬКО на основе CONTEXT ниже.
            Если CONTEXT пуст или не относится к вопросу — скажи, что в базе знаний ретрита нет ответа.
            Не выдумывай расписание, спикеров и правила.
            """;

    /**
     * Память диалога: окно из 20 сообщений в PostgreSQL.
     *
     * @param repository JDBC-репозиторий Spring AI
     * @return ChatMemory
     */
    @Bean
    ChatMemory chatMemory(JdbcChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(20)
                .build();
    }

    /**
     * ChatClient с памятью и логированием для RAG-чата.
     *
     * @param chatModel      модель OpenAI
     * @param chatMemory     память диалога
     * @param loggingAdvisor логирование prompt
     * @return настроенный ChatClient
     */
    @Bean
    ChatClient chatClient(ChatModel chatModel,
                          ChatMemory chatMemory,
                          LoggingAdvisor loggingAdvisor) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        loggingAdvisor
                )
                .build();
    }

    /**
     * Простой ChatClient без advisors — для блока 1 (введение).
     *
     * @param chatModel модель OpenAI
     * @return ChatClient без RAG и памяти
     */
    @Bean
    ChatClient simpleChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
