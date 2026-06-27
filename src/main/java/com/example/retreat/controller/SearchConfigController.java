package com.example.retreat.controller;

import com.example.retreat.dto.SearchConfigDto;
import com.example.retreat.rag.SearchConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Управление порогом similarity и topK (блоки 2b, 6).
 */
@RestController
@RequestMapping("/api/admin/search-config")
public class SearchConfigController {

    private final SearchConfigService searchConfigService;

    /**
     * @param searchConfigService сервис настроек поиска
     */
    public SearchConfigController(SearchConfigService searchConfigService) {
        this.searchConfigService = searchConfigService;
    }

    /**
     * Возвращает текущие настройки поиска.
     *
     * @return similarityThreshold и topK
     */
    @GetMapping
    public SearchConfigDto getConfig() {
        return searchConfigService.getConfig();
    }

    /**
     * Обновляет настройки поиска без перезапуска.
     *
     * @param config новые значения
     * @return обновлённый конфиг
     */
    @PutMapping
    public SearchConfigDto updateConfig(@RequestBody SearchConfigDto config) {
        return searchConfigService.updateConfig(config);
    }
}
