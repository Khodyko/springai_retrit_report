package com.example.retreat.rag.loader;

import java.io.IOException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP-загрузка документов в pgvector (блок 2 доклада).
 */
@RestController
@RequestMapping("/api/admin")
public class LoaderController {

    private final DocumentLoaderService loaderService;

    /**
     * @param loaderService сервис загрузки документов
     */
    public LoaderController(DocumentLoaderService loaderService) {
        this.loaderService = loaderService;
    }

    /**
     * Загружает документы ретрита в vector store.
     *
     * @return число загруженных чанков
     */
    @PostMapping("/load")
    public ResponseEntity<Map<String, Object>> load() throws IOException {
        int chunks = loaderService.loadDocuments();
        return ResponseEntity.ok(Map.of("loadedChunks", chunks));
    }
}
