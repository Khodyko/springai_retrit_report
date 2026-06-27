package com.example.retreat.controller;

import com.example.retreat.dto.MetadataCatalogDto;
import com.example.retreat.rag.metadata.SearchableMetadataService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Каталог metadata из Liquibase.
 */
@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    private final SearchableMetadataService metadataService;

    /**
     * @param metadataService сервис каталога metadata
     */
    public MetadataController(SearchableMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * Возвращает каталог допустимых metadata.
     *
     * @return список пар ключ/значение
     */
    @GetMapping("/searchable")
    public List<MetadataCatalogDto> searchable() {
        return metadataService.findAll();
    }
}
