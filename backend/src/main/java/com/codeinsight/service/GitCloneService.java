package com.codeinsight.service;

import com.codeinsight.entity.Repository;
import com.codeinsight.mapper.RepositoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * 仓库克隆服务。
 * 使用系统 git 命令而非 JGit，复用系统网络配置（代理、SSL 等）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitCloneService {

    private final RepositoryMapper repositoryMapper;

    @Value("${repository.base-path}")
    private String basePath;

    @Async
    public void cloneRepository(Repository repo) {
        try {
            repo.setStatus("CLONING");
            repositoryMapper.updateById(repo);

            String localPath = Paths.get(basePath, repo.getUserId().toString(), repo.getName()).toString();
            File dir = new File(localPath);
            if (dir.exists()) {
                deleteDirectory(dir);
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "git", "clone", "--depth", "1",
                    "--branch", repo.getBranch(),
                    repo.getUrl(),
                    localPath
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            boolean finished = process.waitFor(2, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Clone 超时（2 分钟）");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                }
                throw new RuntimeException("git clone 失败: " + sb);
            }

            log.info("仓库 Clone 成功: {}", repo.getUrl());

            repo.setLocalPath(localPath);
            repo.setStatus("READY");
            repositoryMapper.updateById(repo);

        } catch (Exception e) {
            log.error("仓库 Clone 失败: {}", repo.getUrl(), e);
            repo.setStatus("ERROR");
            repo.setErrorMsg(e.getMessage());
            repositoryMapper.updateById(repo);
        }
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
