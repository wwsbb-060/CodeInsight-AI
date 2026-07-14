package com.codeinsight.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeinsight.common.BusinessException;
import com.codeinsight.dto.RepositoryRequest;
import com.codeinsight.entity.Repository;
import com.codeinsight.mapper.RepositoryMapper;
import com.codeinsight.service.GitCloneService;
import com.codeinsight.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryServiceImpl implements RepositoryService {

    private final RepositoryMapper repositoryMapper;
    private final GitCloneService gitCloneService;

    @Override
    public Repository create(Long userId, RepositoryRequest request) {
        // 从 URL 提取仓库名
        String name = extractRepoName(request.getUrl());
        String branch = request.getBranch() != null ? request.getBranch() : "main";

        Repository repo = new Repository();
        repo.setUserId(userId);
        repo.setName(name);
        repo.setUrl(request.getUrl());
        repo.setBranch(branch);
        repo.setStatus("PENDING");
        repositoryMapper.insert(repo);

        // 通过独立 Service 调用，@Async 真正生效
        gitCloneService.cloneRepository(repo);

        return repo;
    }

    @Override
    public List<Repository> listByUser(Long userId) {
        return repositoryMapper.selectList(
                new LambdaQueryWrapper<Repository>()
                        .eq(Repository::getUserId, userId)
                        .orderByDesc(Repository::getCreatedAt));
    }

    @Override
    public Repository getById(Long id, Long userId) {
        Repository repo = repositoryMapper.selectById(id);
        if (repo == null || !repo.getUserId().equals(userId)) {
            throw new BusinessException(404, "仓库不存在");
        }
        return repo;
    }

    @Override
    public void delete(Long id, Long userId) {
        Repository repo = getById(id, userId);
        // 删除本地文件
        if (repo.getLocalPath() != null) {
            File dir = new File(repo.getLocalPath());
            deleteDirectory(dir);
        }
        repositoryMapper.deleteById(id);
    }

    private String extractRepoName(String url) {
        String name = url.substring(url.lastIndexOf('/') + 1);
        if (name.endsWith(".git")) {
            name = name.substring(0, name.length() - 4);
        }
        return name;
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }
}
