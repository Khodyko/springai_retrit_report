package com.example.retreat.rag.loader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

/**
 * Загрузка markdown-документов ретрита в pgvector.
 */
@Service
public class DocumentLoaderService {

    private static final Logger log = LoggerFactory.getLogger(DocumentLoaderService.class);
    private static final Pattern FRONTMATTER = Pattern.compile("^---\\s*\\ntopic:\\s*(\\w+)\\s*\\n---\\s*\\n", Pattern.MULTILINE);
    private static final int RETREAT_YEAR = 2026;

    private final VectorStore vectorStore;
    private final MetadataSearcher metadataSearcher;

    /**
     * @param vectorStore      pgvector store
     * @param metadataSearcher валидатор metadata
     */
    public DocumentLoaderService(VectorStore vectorStore, MetadataSearcher metadataSearcher) {
        this.vectorStore = vectorStore;
        this.metadataSearcher = metadataSearcher;
    }

    /**
     * Читает документы, чанкует и сохраняет в vector store.
     *
     * @return число загруженных чанков
     */
    public int loadDocuments() throws IOException {
        vectorStore.delete(new FilterExpressionBuilder().gte("retreatYear", 0).build());

        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath:documents/retreat/*.md");

        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> allChunks = new ArrayList<>();

        for (Resource resource : resources) {
            String source = resource.getFilename().replace(".md", "");
            String text = resource.getContentAsString(StandardCharsets.UTF_8);

            for (Document section : splitByFrontmatter(text)) {
                String topic = (String) section.getMetadata().get("topic");
                Map<String, Object> metadata = metadataSearcher.buildMetadata(source, topic, RETREAT_YEAR);
                Document doc = new Document(section.getText(), metadata);
                allChunks.addAll(splitter.apply(List.of(doc)));
            }
        }

        vectorStore.add(allChunks);
        log.info("Загружено {} чанков", allChunks.size());
        return allChunks.size();
    }

    /**
     * Разбивает markdown на секции по YAML-frontmatter.
     *
     * @param text содержимое файла
     * @return секции с topic в metadata
     */
    private List<Document> splitByFrontmatter(String text) {
        List<Document> sections = new ArrayList<>();
        Matcher matcher = FRONTMATTER.matcher(text);
        int lastEnd = 0;
        String lastTopic = null;

        while (matcher.find()) {
            if (lastTopic != null) {
                String body = text.substring(lastEnd, matcher.start()).trim();
                if (!body.isEmpty()) {
                    sections.add(new Document(body, Map.of("topic", lastTopic)));
                }
            }
            lastTopic = matcher.group(1);
            lastEnd = matcher.end();
        }

        if (lastTopic != null) {
            String body = text.substring(lastEnd).trim();
            if (!body.isEmpty()) {
                sections.add(new Document(body, Map.of("topic", lastTopic)));
            }
        }

        return sections;
    }
}
