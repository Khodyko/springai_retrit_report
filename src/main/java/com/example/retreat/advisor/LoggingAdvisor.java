package com.example.retreat.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Логирует пользовательский запрос до и ответ после вызова LLM.
 */
@Component
public class LoggingAdvisor implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LoggingAdvisor.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        log.info("[LoggingAdvisor] user: {}", request.prompt().getUserMessage());
        ChatClientResponse response = chain.nextCall(request);
        log.info("[LoggingAdvisor] answer: {}", response.chatResponse().getResult().getOutput().getText());
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}
