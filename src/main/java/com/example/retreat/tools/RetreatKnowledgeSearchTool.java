package com.example.retreat.tools;

import com.example.retreat.rag.SearchConfigService;
import com.example.retreat.rag.metadata.SearchableMetadataService;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

/**
 * Поиск в базе знаний с опциональными String-фильтрами metadata (блок 4).
 */
@Component
public class RetreatKnowledgeSearchTool {

    private static final Logger log = LoggerFactory.getLogger(RetreatKnowledgeSearchTool.class);

    private final VectorStore vectorStore;
    private final SearchConfigService searchConfigService;
    private final SearchableMetadataService metadataService;

    /**
     * @param vectorStore       pgvector store
     * @param searchConfigService настройки поиска
     * @param metadataService   каталог metadata
     */
    public RetreatKnowledgeSearchTool(VectorStore vectorStore,
                                      SearchConfigService searchConfigService,
                                      SearchableMetadataService metadataService) {
        this.vectorStore = vectorStore;
        this.searchConfigService = searchConfigService;
        this.metadataService = metadataService;
    }

    /**
     * Ищет релевантные фрагменты в pgvector.
     *
     * @param query  текстовый запрос
     * @param source опциональный источник
     * @param topic  опциональная тема
     * @return найденный контекст
     */
    @Tool(description = """
            Поиск в базе знаний корпоративного ретрита. Вызывай, если вопрос про расписание,
            спикеров, локацию, регистрацию или правила ретрита.
            source: schedule | speakers | location | faq
            topic: yoga | meditation | workshop | logistics | registration | rules
            Если не уверен в metadata — не передавай фильтр.
            """)
    public String searchRetreatKnowledge(
            @ToolParam(description = "Текстовый запрос пользователя о ретрите") String query,
            @ToolParam(description = "Опционально: источник", required = false) String source,
            @ToolParam(description = "Опционально: тема", required = false) String topic) {

        log.info("[Tool] searchRetreatKnowledge query={}, source={}, topic={}", query, source, topic);

        var config = searchConfigService.getConfig();
        var builder = SearchRequest.builder()
                .query(query)
                .similarityThreshold(config.similarityThreshold())
                .topK(config.topK());

        Filter.Expression filter = buildFilter(source, topic);
        if (filter != null) {
            builder.filterExpression(filter);
        }

        List<Document> chunks = vectorStore.similaritySearch(builder.build());
        if (chunks.isEmpty()) {
            return "В базе знаний нет релевантных фрагментов.";
        }

        return chunks.stream().map(Document::getText).collect(Collectors.joining("\n---\n"));
    }

    /**
     * Собирает filter expression из source и topic.
     *
     * @param source источник
     * @param topic  тема
     * @return выражение фильтра или null
     */
    private Filter.Expression buildFilter(String source, String topic) {
        FilterExpressionBuilder filterBuilder = new FilterExpressionBuilder();

        if (source != null && metadataService.isValid("source", source)
                && topic != null && metadataService.isValid("topic", topic)) {
            return filterBuilder.and(
                    filterBuilder.eq("source", source),
                    filterBuilder.eq("topic", topic)
            ).build();
        }
        if (source != null && metadataService.isValid("source", source)) {
            return filterBuilder.eq("source", source).build();
        }
        if (topic != null && metadataService.isValid("topic", topic)) {
            return filterBuilder.eq("topic", topic).build();
        }
        return null;
    }
}
