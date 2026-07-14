package com.codeinsight.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull(message = "仓库 ID 不能为空")
    private Long repositoryId;
}
