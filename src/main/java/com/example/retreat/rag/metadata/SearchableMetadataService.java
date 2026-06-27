package com.example.retreat.rag.metadata;

import com.example.retreat.dto.MetadataCatalogDto;
import com.example.retreat.rag.metadata.entity.MetadataCatalog;
import com.example.retreat.rag.metadata.repository.MetadataCatalogRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Каталог допустимых metadata из Liquibase.
 */
@Service
public class SearchableMetadataService {

    private final MetadataCatalogRepository catalogRepository;

    /**
     * @param catalogRepository репозиторий каталога
     */
    public SearchableMetadataService(MetadataCatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    /**
     * Возвращает все допустимые пары ключ/значение.
     *
     * @return каталог metadata
     */
    public List<MetadataCatalogDto> findAll() {
        return catalogRepository.findAllByOrderByFieldNameAscValueAsc().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Проверяет, что значение поля есть в каталоге.
     *
     * @param fieldName ключ
     * @param value     значение
     * @return true если допустимо
     */
    public boolean isValid(String fieldName, String value) {
        if (fieldName == null || value == null) {
            return false;
        }
        return catalogRepository.existsByFieldNameAndValue(fieldName, value);
    }

    /**
     * Преобразует entity в DTO.
     *
     * @param entry запись каталога
     * @return DTO
     */
    private MetadataCatalogDto toDto(MetadataCatalog entry) {
        return new MetadataCatalogDto(entry.getFieldName(), entry.getValue(), entry.getDescription());
    }
}
