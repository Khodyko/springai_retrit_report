package com.example.retreat.dto;

/**
 * Настройки similarity-поиска.
 *
 * @param similarityThreshold порог схожести (0..1)
 * @param topK                число чанков в контексте
 */
public record SearchConfigDto(double similarityThreshold, int topK) {
}
