package com.example.retreat.dto;

/**
 * Запрос к чат-эндпоинту.
 *
 * @param message текст вопроса пользователя
 * @param rag     если false — запрос без RAG-advisor (блок 1 доклада)
 */
public record ChatRequest(String message, Boolean rag) {
}
