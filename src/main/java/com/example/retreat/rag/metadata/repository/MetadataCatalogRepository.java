package com.example.retreat.rag.metadata.repository;

import com.example.retreat.rag.metadata.entity.MetadataCatalog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий каталога metadata.
 */
public interface MetadataCatalogRepository extends JpaRepository<MetadataCatalog, Long> {

    /**
     * Возвращает все записи каталога.
     *
     * @return список пар ключ/значение
     */
    List<MetadataCatalog> findAllByOrderByFieldNameAscValueAsc();

    /**
     * Проверяет наличие пары ключ/значение.
     *
     * @param fieldName ключ
     * @param value     значение
     * @return true если найдено
     */
    boolean existsByFieldNameAndValue(String fieldName, String value);
}
