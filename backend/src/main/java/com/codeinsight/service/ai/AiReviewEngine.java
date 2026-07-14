package com.codeinsight.service.ai;

import com.codeinsight.entity.Repository;
import com.codeinsight.mapper.RepositoryMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 评审引擎 — 协调者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiReviewEngine {

    private final ChatLanguageModel chatLanguageModel;
    private final RepositoryMapper repositoryMapper;
    private final ProjectScanner scanner;
    private final PromptBuilder promptBuilder;
    private final String currentModelName;

    /** 从 LLM 返回内容中提取末尾 JSON 摘要 */
    private static final Pattern DIGEST_PATTERN =
            Pattern.compile("\\{[^{}]*\"overview\"[^{}]*\"techStack\"[^{}]*\"findings\"[^{}]*\\{[^}]*\\}[^{}]*\\}",
                    Pattern.DOTALL);

    public ReviewResult analyze(Long repositoryId) throws IOException {
        Repository repo = repositoryMapper.selectById(repositoryId);
        if (repo == null || repo.getLocalPath() == null) {
            throw new IllegalArgumentException("仓库不存在或未 Clone");
        }

        ProjectScanner.ScanResult scanResult = scanner.scan(Paths.get(repo.getLocalPath()));
        log.info("项目扫描完成: repoId={}, files={}", repositoryId, scanResult.fileCount);

        String prompt = promptBuilder.build(scanResult);

        log.info("开始调用 LLM: repoId={}", repositoryId);
        String response = chatLanguageModel.generate(prompt);
        log.info("LLM 响应完成: repoId={}, length={}", repositoryId, response.length());

        ReviewResult result = new ReviewResult();
        result.model = currentModelName;
        result.tokenUsed = estimateTokens(prompt) + estimateTokens(response);
        result.summary = extractSummary(response);
        result.markdown = buildMarkdownReport(repo, scanResult, response);
        result.digest = extractDigest(response);

        return result;
    }

    /**
     * 从 AI 返回中提取 JSON 摘要。
     * 兜底策略：匹配不上就返回紧凑格式的占位 JSON，前端能正常展示。
     */
    private String extractDigest(String aiResponse) {
        if (aiResponse == null) return fallbackDigest();

        // 找最后一个 JSON 对象（包含 overview / techStack / findings 三个 key）
        Matcher m = DIGEST_PATTERN.matcher(aiResponse);
        String last = null;
        while (m.find()) {
            last = m.group();
        }
        if (last != null) {
            return last.trim();
        }

        // try 更宽松的匹配：找最后一个平衡花括号
        int start = aiResponse.lastIndexOf('{');
        if (start >= 0) {
            int end = aiResponse.indexOf('}', start);
            if (end >= 0) {
                String maybe = aiResponse.substring(start, end + 1).trim();
                if (maybe.contains("overview") || maybe.contains("findings")) {
                    return maybe;
                }
            }
        }

        log.warn("未能从 AI 返回中提取 JSON 摘要，使用占位");
        return fallbackDigest();
    }

    private String fallbackDigest() {
        return "{\"overview\":\"报告正在生成中\",\"techStack\":\"请稍后查看\",\"findings\":[]}";
    }

    private String buildMarkdownReport(Repository repo, ProjectScanner.ScanResult scan, String aiResponse) {
        return "# CodeInsight AI 代码评审报告\n\n" +
                "---\n\n" +
                "## 仓库信息\n\n" +
                "| 项目 | 信息 |\n" +
                "|------|------|\n" +
                "| 仓库名称 | " + repo.getName() + " |\n" +
                "| 仓库地址 | " + repo.getUrl() + " |\n" +
                "| 分支 | " + repo.getBranch() + " |\n" +
                "| 分析文件数 | " + scan.fileCount + " |\n" +
                "| 生成时间 | " + java.time.LocalDateTime.now() + " |\n\n" +
                "---\n\n" +
                aiResponse;
    }

    private String extractSummary(String aiResponse) {
        int idx = aiResponse.lastIndexOf("## 9.");
        if (idx == -1) idx = aiResponse.lastIndexOf("总结");
        if (idx != -1) {
            String s = aiResponse.substring(idx);
            return s.length() > 500 ? s.substring(0, 500) : s;
        }
        return aiResponse.length() > 300 ? aiResponse.substring(0, 300) : aiResponse;
    }

    private int estimateTokens(String text) {
        return text == null ? 0 : text.length() / 2;
    }

    public static class ReviewResult {
        public String summary;
        public String markdown;
        public String model;
        public int tokenUsed;
        public String digest;
    }
}
