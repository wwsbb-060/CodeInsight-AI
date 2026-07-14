package com.codeinsight.service;

import com.codeinsight.entity.Review;
import com.codeinsight.mapper.ReviewMapper;
import com.codeinsight.service.ai.AiReviewEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncReviewExecutor {

    private final ReviewMapper reviewMapper;
    private final AiReviewEngine aiReviewEngine;

    @Async
    public void execute(Review review) {
        try {
            review.setStatus("ANALYZING");
            reviewMapper.updateById(review);

            AiReviewEngine.ReviewResult result = aiReviewEngine.analyze(review.getRepositoryId());

            review.setStatus("COMPLETED");
            review.setSummary(result.summary);
            review.setDigest(result.digest);
            review.setReportMarkdown(result.markdown);
            review.setAiModel(result.model);
            review.setTokenUsed(result.tokenUsed);
            review.setCompletedAt(LocalDateTime.now());
            safeUpdate(review, "COMPLETED");

            log.info("AI 评审完成: reviewId={}, model={}, tokenUsed={}",
                    review.getId(), result.model, result.tokenUsed);

        } catch (Exception e) {
            log.error("AI 评审失败: reviewId={}", review.getId(), e);
            try {
                review.setStatus("ERROR");
                review.setErrorMsg(truncateMsg(e.getMessage()));
                reviewMapper.updateById(review);
            } catch (Exception dbEx) {
                log.error("保存错误状态也失败了: reviewId={}", review.getId(), dbEx);
            }
        }
    }

    /**
     * 安全更新，写库失败不抛异常（避免覆盖原始错误）
     */
    private void safeUpdate(Review review, String status) {
        try {
            reviewMapper.updateById(review);
        } catch (Exception e) {
            log.error("保存 {} 状态失败: reviewId={}", status, review.getId(), e);
        }
    }

    /**
     * 截断过长的错误信息，避免存库时字段超长
     */
    private String truncateMsg(String msg) {
        if (msg == null) return null;
        return msg.length() > 450 ? msg.substring(0, 450) : msg;
    }
}
