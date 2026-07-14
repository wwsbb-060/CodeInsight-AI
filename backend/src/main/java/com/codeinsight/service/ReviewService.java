package com.codeinsight.service;

import com.codeinsight.entity.Review;

import java.util.List;

public interface ReviewService {

    Review create(Long userId, Long repositoryId);

    Review getById(Long id, Long userId);

    List<Review> listByUser(Long userId);

    String getReportMarkdown(Long id, Long userId);

    void delete(Long id, Long userId);

    void updateMemo(Long id, Long userId, String memo);
}
