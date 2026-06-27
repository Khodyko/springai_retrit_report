package com.example.retreat.controller;

import com.example.retreat.dto.ChatRequest;
import com.example.retreat.dto.ChatResponse;
import com.example.retreat.service.RagChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Основной чат-эндпоинт: RAG + память (блоки 1–5).
 */
@RestController
@RequestMapping("/api/chat")
public class ConversationChatController {

    private final RagChatService ragChatService;

    /**
     * @param ragChatService сервис RAG-чата
     */
    public ConversationChatController(RagChatService ragChatService) {
        this.ragChatService = ragChatService;
    }

    /**
     * Отвечает на вопрос пользователя.
     *
     * @param request тело запроса
     * @param userId  X-User-Id для памяти диалога
     * @return ответ
     */
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request,
                             @RequestHeader(value = "X-User-Id", required = false) String userId) {
        boolean useRag = request.rag() == null || request.rag();
        return ragChatService.chat(request.message(), useRag, userId);
    }
}
