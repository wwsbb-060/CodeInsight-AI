package com.codeinsight.service.impl;

import com.codeinsight.entity.Repository;
import com.codeinsight.mapper.RepositoryMapper;
import com.codeinsight.service.KnowledgeBaseService;
import com.codeinsight.service.ai.CodeChunker;
import com.codeinsight.service.ai.ProjectScanner;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 知识库服务实现。
 * 使用内存级向量存储，每个仓库一个独立的 EmbeddingStore。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final EmbeddingModel embeddingModel;
    private final RepositoryMapper repositoryMapper;
    private final ProjectScanner projectScanner;
    private final CodeChunker chunker;

    /** repositoryId → 向量存储 */
    private final Map<Long, InMemoryEmbeddingStore<TextSegment>> stores = new ConcurrentHashMap<>();

    @Override
    public void build(Long repositoryId) {
        Repository repo = repositoryMapper.selectById(repositoryId);
        if (repo == null || repo.getLocalPath() == null) {
            log.warn("知识库构建跳过: 仓库不存在或未 Clone, repoId={}", repositoryId);
            return;
        }

        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        int totalChunks = 0;

        try {
            ProjectScanner.ScanResult scan = projectScanner.scan(Paths.get(repo.getLocalPath()));

            for (ProjectScanner.FileContent fc : scan.contents) {
                // 去掉 ProjectScanner 加的行号前缀 "   1| " → 纯代码
                String rawContent = stripLineNumbers(fc.content());
                List<CodeChunker.Chunk> chunks = chunker.chunk(fc.path(), rawContent);

                for (CodeChunker.Chunk chunk : chunks) {
                    // 构建可检索的文本段：元数据 + 代码
                    TextSegment segment = TextSegment.from(
                            chunk.toMetadata() + "\n" + chunk.content());
                    Embedding embedding = embeddingModel.embed(chunk.content()).content();
                    store.add(embedding, segment);
                    totalChunks++;
                }
            }

            stores.put(repositoryId, store);
            log.info("知识库构建完成: repoId={}, files={}, chunks={}",
                    repositoryId, scan.fileCount, totalChunks);

        } catch (Exception e) {
            log.error("知识库构建失败: repoId={}", repositoryId, e);
            stores.remove(repositoryId);
        }
    }

    @Override
    public boolean isReady(Long repositoryId) {
        InMemoryEmbeddingStore<TextSegment> store = stores.get(repositoryId);
        return store != null;
    }

    @Override
    public List<TextSegment> search(Long repositoryId, String query, int maxResults) {
        InMemoryEmbeddingStore<TextSegment> store = stores.get(repositoryId);
        if (store == null) {
            return List.of();
        }

        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchResult<TextSegment> result = store.search(
                dev.langchain4j.store.embedding.EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(maxResults)
                        .minScore(0.6)
                        .build());

        return result.matches().stream()
                .map(dev.langchain4j.store.embedding.EmbeddingMatch::embedded)
                .toList();
    }

    /**
     * 去掉 ProjectScanner 加的行号前缀 "   1| " → 纯代码
     */
    private String stripLineNumbers(String content) {
        return content.replaceAll("(?m)^\\s*\\d+\\| ", "");
    }
}
