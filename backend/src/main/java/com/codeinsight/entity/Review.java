package com.codeinsight.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long repositoryId;

    private Long userId;

    private String status;

    private String summary;

    private String reportMarkdown;

    private String memo;

    private String digest;

    private String aiModel;

    private Integer tokenUsed;

    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
