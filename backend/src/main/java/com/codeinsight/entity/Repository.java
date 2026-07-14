package com.codeinsight.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("repository")
public class Repository {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String url;

    private String branch;

    private String localPath;

    private String status;

    private String errorMsg;

    private Integer fileCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
