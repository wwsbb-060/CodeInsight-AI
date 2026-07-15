package com.codeinsight.controller;

import com.codeinsight.common.Result;
import com.codeinsight.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final SqlSession sqlSession;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview(@AuthenticationPrincipal SecurityUser user) {
        Long userId = user.getUserId();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalRepos", sqlSession.selectOne("stats.totalRepos", userId));
        data.put("totalReviews", sqlSession.selectOne("stats.totalReviews", userId));
        data.put("monthReviews", sqlSession.selectOne("stats.monthReviews", userId));
        data.put("totalTokens", sqlSession.selectOne("stats.totalTokens", userId));
        return Result.success(data);
    }

    @GetMapping("/tokens")
    public Result<List<Map<String, Object>>> tokenTrend(@AuthenticationPrincipal SecurityUser user) {
        Long userId = user.getUserId();
        List<Map<String, Object>> list = sqlSession.selectList("stats.tokenTrend", userId);
        return Result.success(list != null ? list : List.of());
    }

    @GetMapping("/activity")
    public Result<List<Map<String, Object>>> activity(@AuthenticationPrincipal SecurityUser user) {
        Long userId = user.getUserId();
        List<Map<String, Object>> list = sqlSession.selectList("stats.recentActivity", userId);
        return Result.success(list != null ? list : List.of());
    }
}
