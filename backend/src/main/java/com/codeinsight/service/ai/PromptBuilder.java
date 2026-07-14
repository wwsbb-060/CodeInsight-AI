package com.codeinsight.service.ai;

import org.springframework.stereotype.Component;

/**
 * Prompt 构建器。
 * 单一职责：拼装评审 Prompt。改评审维度只改这一个类。
 */
@Component
public class PromptBuilder {

    public String build(ProjectScanner.ScanResult scanResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位资深软件架构师和代码评审专家。请对以下项目进行全面分析。\n\n");

        sb.append("## 项目文件结构\n\n```\n");
        sb.append(scanResult.fileTree);
        sb.append("```\n\n");

        if (!scanResult.contents.isEmpty()) {
            sb.append("## 关键文件内容\n\n");
            for (ProjectScanner.FileContent fc : scanResult.contents) {
                sb.append("### ").append(fc.path()).append("\n\n");
                sb.append("```\n");
                sb.append(fc.content());
                sb.append("```\n\n");
            }
        }

        sb.append("## 评审要求\n\n");
        sb.append("请从以下维度进行分析，用 Markdown 格式输出：\n\n");
        sb.append("1. **项目整体概述** — 这是什么项目？主要解决什么问题？\n");
        sb.append("2. **技术栈分析** — 使用了哪些技术、框架和库？\n");
        sb.append("3. **项目架构** — 模块划分、分层设计、目录结构合理性\n");
        sb.append("4. **核心模块分析** — 各模块职责和关键逻辑\n");
        sb.append("5. **入口点和启动流程** — 项目如何启动？关键配置在哪？\n");
        sb.append("6. **数据库设计** — 如有数据库相关代码，分析表结构和关系\n");
        sb.append("7. **潜在问题** — 代码中可能存在的 Bug、性能问题或安全隐患\n");
        sb.append("8. **改进建议** — 代码质量、架构优化、最佳实践建议\n");
        sb.append("9. **总结** — 一段 200 字以内的项目总结\n");
        sb.append("10. **快速摘要 JSON** — 请在本条评审的最后一行，输出一个纯 JSON 对象（不要放在代码块中），格式如下：\n");
        sb.append("```\n{\"overview\":\"项目一句话概述（50-80字）\",\"techStack\":\"主要技术栈列表\",\"findings\":[\"重要发现1\",\"重要发现2\",\"重要发现3\"]}\n```\n\n");
        sb.append("请严格按以上结构输出，使用中文。");

        return sb.toString();
    }
}
