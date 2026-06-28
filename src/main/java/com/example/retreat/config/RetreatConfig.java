package com.example.retreat.config;

import org.springframework.ai.chat.client.ChatClient;
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
     * Базовый ChatClient без advisors — настройки на уровне сервиса.
     *
     * @param chatModel модель OpenAI
     * @return ChatClient
     */
    @Bean
    ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
