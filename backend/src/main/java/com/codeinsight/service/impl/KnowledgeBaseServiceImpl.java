package com.codeinsight.service.impl;

import com.codeinsight.dto.QaResponse;
import com.codeinsight.entity.Repository;
import com.codeinsight.mapper.RepositoryMapper;
import com.codeinsight.service.KnowledgeBaseService;
import com.codeinsight.service.ai.CodeChunker;
import com.codeinsight.service.ai.ProjectScanner;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 知识库服务实现。
 * 使用内存级向量存储，每个仓库一个独立的 EmbeddingStore。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final EmbeddingModel embeddingModel;
    private final ChatLanguageModel chatLanguageModel;
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
        return searchMatches(repositoryId, query, maxResults).stream()
                .map(EmbeddingMatch::embedded)
                .toList();
    }

    @Override
    public QaResponse qa(Long repositoryId, String question) {
        if (!isReady(repositoryId)) {
            QaResponse resp = new QaResponse();
            resp.setAnswer("知识库尚未构建完成，请等待评审结束后再试。");
            resp.setReferences(List.of());
            return resp;
        }

        // 1. 检索 Top-5 相关代码块
        List<EmbeddingMatch<TextSegment>> matches = searchMatches(repositoryId, question, 5);
        if (matches.isEmpty()) {
            QaResponse resp = new QaResponse();
            resp.setAnswer("未找到与问题相关的代码片段，请尝试换个问法。");
            resp.setReferences(List.of());
            return resp;
        }

        // 2. 拼装 Prompt
        StringBuilder context = new StringBuilder();
        List<QaResponse.Reference> refs = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : matches) {
            TextSegment seg = match.embedded();
            String text = seg.text();
            context.append("```\n").append(text).append("\n```\n\n");

            // 从 TextSegment 文本中解析文件路径和行号（格式：path/to/file.java L10-25）
            refs.add(parseReference(text));
        }

        String prompt = "你是一位代码分析助手。请根据以下代码片段回答用户的问题。\n" +
                "如果代码片段不足以回答问题，请如实说明。\n" +
                "回答时尽量引用具体的文件名和行号。\n\n" +
                "## 相关代码\n\n" + context + "\n" +
                "## 用户问题\n\n" + question;

        // 3. 调 LLM 生成回答
        String answer = chatLanguageModel.generate(prompt);

        QaResponse resp = new QaResponse();
        resp.setAnswer(answer);
        resp.setReferences(refs);
        return resp;
    }

    private List<EmbeddingMatch<TextSegment>> searchMatches(Long repositoryId, String query, int maxResults) {
        InMemoryEmbeddingStore<TextSegment> store = stores.get(repositoryId);
        if (store == null) return List.of();

        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchResult<TextSegment> result = store.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(maxResults)
                        .minScore(0.6)
                        .build());
        return result.matches();
    }

    // 格式: path/to/File.java L10-25\ncode...
    private static final Pattern REF_PATTERN = Pattern.compile("^(.+?)\\s+L(\\d+)-(\\d+)$", Pattern.MULTILINE);

    private QaResponse.Reference parseReference(String text) {
        String firstLine = text.split("\n")[0].trim();
        Matcher m = REF_PATTERN.matcher(firstLine);
        if (m.find()) {
            String snippet = text.substring(Math.min(text.indexOf('\n') + 1, text.length())).trim();
            if (snippet.length() > 200) snippet = snippet.substring(0, 200) + "...";
            return new QaResponse.Reference(
                    m.group(1),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    snippet);
        }
        // 兜底：第一行作为 snippet
        return new QaResponse.Reference("unknown", 0, 0,
                firstLine.length() > 100 ? firstLine.substring(0, 100) + "..." : firstLine);
    }

    /**
     * 去掉 ProjectScanner 加的行号前缀 "   1| " → 纯代码
     */
    private String stripLineNumbers(String content) {
        return content.replaceAll("(?m)^\\s*\\d+\\| ", "");
    }
}
