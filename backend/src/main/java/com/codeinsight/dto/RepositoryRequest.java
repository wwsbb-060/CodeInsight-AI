package com.codeinsight.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RepositoryRequest {

    @NotBlank(message = "仓库 URL 不能为空")
    private String url;

    private String branch;
}
