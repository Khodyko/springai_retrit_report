package com.example.retreat.rag.loader;

import com.example.retreat.rag.metadata.SearchableMetadataService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Валидация metadata чанков по каталогу из Liquibase.
 */
@Component
public class MetadataSearcher {

    private final SearchableMetadataService metadataService;

    /**
     * @param metadataService каталог metadata
     */
    public MetadataSearcher(SearchableMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * Проверяет и возвращает metadata для чанка.
     *
     * @param source      источник (имя файла)
     * @param topic       тема из frontmatter
     * @param retreatYear год ретрита
     * @return metadata для Document
     */
    public Map<String, Object> buildMetadata(String source, String topic, int retreatYear) {
        if (!metadataService.isValid("source", source)) {
            throw new IllegalArgumentException("Недопустимый source: " + source);
        }
        if (!metadataService.isValid("topic", topic)) {
            throw new IllegalArgumentException("Недопустимый topic: " + topic);
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", source);
        metadata.put("topic", topic);
        metadata.put("retreatYear", retreatYear);
        return metadata;
    }
}
