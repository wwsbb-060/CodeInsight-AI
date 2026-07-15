package com.codeinsight.service;

import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

/**
 * 知识库服务。
 * 负责代码文本的向量化存储和语义检索。
 */
public interface KnowledgeBaseService {

    /**
     * 为指定仓库构建知识库（全量扫描 → 切块 → 转向量 → 存储）
     */
    void build(Long repositoryId);

    /**
     * 检查知识库是否已构建
     */
    boolean isReady(Long repositoryId);

    /**
     * 语义检索：返回与查询最相关的代码片段
     */
    List<TextSegment> search(Long repositoryId, String query, int maxResults);
}
