package com.codeinsight.controller;

import com.codeinsight.common.Result;
import com.codeinsight.dto.RepositoryRequest;
import com.codeinsight.entity.Repository;
import com.codeinsight.service.RepositoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.codeinsight.security.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
public class RepositoryController {

    private final RepositoryService repositoryService;

    @PostMapping
    public Result<Repository> create(@Valid @RequestBody RepositoryRequest request,
                                     @AuthenticationPrincipal SecurityUser user) {
        return Result.success(repositoryService.create(user.getUserId(), request));
    }

    @GetMapping
    public Result<List<Repository>> list(@AuthenticationPrincipal SecurityUser user) {
        return Result.success(repositoryService.listByUser(user.getUserId()));
    }

    @GetMapping("/{id}")
    public Result<Repository> getById(@PathVariable Long id,
                                      @AuthenticationPrincipal SecurityUser user) {
        return Result.success(repositoryService.getById(id, user.getUserId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @AuthenticationPrincipal SecurityUser user) {
        repositoryService.delete(id, user.getUserId());
        return Result.success();
    }
}
