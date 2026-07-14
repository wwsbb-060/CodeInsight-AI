package com.codeinsight.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeinsight.common.BusinessException;
import com.codeinsight.entity.Repository;
import com.codeinsight.entity.Review;
import com.codeinsight.mapper.ReviewMapper;
import com.codeinsight.service.AsyncReviewExecutor;
import com.codeinsight.service.RepositoryService;
import com.codeinsight.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final RepositoryService repositoryService;
    private final AsyncReviewExecutor asyncReviewExecutor;

    @Override
    public Review create(Long userId, Long repositoryId) {
        Repository repo = repositoryService.getById(repositoryId, userId);

        if (!"READY".equals(repo.getStatus())) {
            throw new BusinessException("仓库尚未准备好，当前状态: " + repo.getStatus());
        }

        Review review = new Review();
        review.setRepositoryId(repositoryId);
        review.setUserId(userId);
        review.setStatus("PENDING");
        reviewMapper.insert(review);

        // 跨类调用，@Async 真正生效
        asyncReviewExecutor.execute(review);

        return review;
    }

    @Override
    public Review getById(Long id, Long userId) {
        Review review = reviewMapper.selectById(id);
        if (review == null || !review.getUserId().equals(userId)) {
            throw new BusinessException(404, "评审记录不存在");
        }
        return review;
    }

    @Override
    public List<Review> listByUser(Long userId) {
        return reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getUserId, userId)
                        .orderByDesc(Review::getCreatedAt));
    }

    @Override
    public String getReportMarkdown(Long id, Long userId) {
        Review review = getById(id, userId);
        if (review.getReportMarkdown() == null) {
            throw new BusinessException("报告尚未生成");
        }
        return review.getReportMarkdown();
    }

    @Override
    public void delete(Long id, Long userId) {
        Review review = getById(id, userId);
        reviewMapper.deleteById(review.getId());
    }

    @Override
    public void updateMemo(Long id, Long userId, String memo) {
        Review review = getById(id, userId);
        review.setMemo(memo);
        reviewMapper.updateById(review);
    }
}
