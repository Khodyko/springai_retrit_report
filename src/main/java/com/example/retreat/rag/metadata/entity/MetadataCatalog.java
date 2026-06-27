package com.example.retreat.rag.metadata.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Допустимая пара ключ/значение metadata из каталога Liquibase.
 */
@Entity
@Table(name = "metadata_catalog")
public class MetadataCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field_name", nullable = false, length = 64)
    private String fieldName;

    @Column(nullable = false, length = 128)
    private String value;

    private String description;

    /**
     * @return идентификатор
     */
    public Long getId() {
        return id;
    }

    /**
     * @return ключ metadata, например source или topic
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @return допустимое значение
     */
    public String getValue() {
        return value;
    }

    /**
     * @return описание
     */
    public String getDescription() {
        return description;
    }
}
