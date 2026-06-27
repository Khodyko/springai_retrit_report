package com.example.retreat.dto;

/**
 * Запись каталога metadata.
 *
 * @param fieldName   ключ, например source или topic
 * @param value       допустимое значение
 * @param description описание
 */
public record MetadataCatalogDto(String fieldName, String value, String description) {
}
