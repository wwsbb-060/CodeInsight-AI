package com.codeinsight.service;

import com.codeinsight.dto.RepositoryRequest;
import com.codeinsight.entity.Repository;

import java.util.List;

public interface RepositoryService {

    Repository create(Long userId, RepositoryRequest request);

    List<Repository> listByUser(Long userId);

    Repository getById(Long id, Long userId);

    void delete(Long id, Long userId);
}
