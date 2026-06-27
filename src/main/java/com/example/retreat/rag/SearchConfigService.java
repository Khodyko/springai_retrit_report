package com.example.retreat.rag;

import com.example.retreat.dto.SearchConfigDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Настройки similarity-поиска из application.yml, runtime-изменение через PUT.
 */
@Service
public class SearchConfigService {

    private SearchConfigDto config;

    /**
     * @param similarityThreshold порог из application.yml
     * @param topK                topK из application.yml
     */
    public SearchConfigService(
            @Value("${retreat.search.similarity-threshold}") double similarityThreshold,
            @Value("${retreat.search.top-k}") int topK) {
        this.config = new SearchConfigDto(similarityThreshold, topK);
    }

    /**
     * Возвращает текущие настройки поиска.
     *
     * @return порог и topK
     */
    public SearchConfigDto getConfig() {
        return config;
    }

    /**
     * Обновляет настройки в памяти (демо freud на сцене).
     *
     * @param newConfig новые значения
     * @return обновлённый конфиг
     */
    public SearchConfigDto updateConfig(SearchConfigDto newConfig) {
        this.config = newConfig;
        return config;
    }
}
