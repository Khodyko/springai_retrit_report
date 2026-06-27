package com.example.retreat.controller;

import com.example.retreat.dto.ChatRequest;
import com.example.retreat.service.AgentChatService;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent-чат с String-tools (блок 4).
 */
@RestController
@RequestMapping("/api/agent")
public class AgentChatController {

    private final AgentChatService agentChatService;

    /**
     * @param agentChatService сервис agent-чата
     */
    public AgentChatController(AgentChatService agentChatService) {
        this.agentChatService = agentChatService;
    }

    /**
     * Отвечает через function calling.
     *
     * @param request тело запроса
     * @return ответ
     */
    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody ChatRequest request) {
        return Map.of("answer", agentChatService.chat(request.message()));
    }
}
