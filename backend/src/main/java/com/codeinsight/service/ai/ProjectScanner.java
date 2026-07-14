package com.codeinsight.service.ai;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 项目文件扫描器。
 * 单一职责：遍历仓库目录，产出文件树和代码文件内容列表。
 */
@Component
public class ProjectScanner {

    private static final int MAX_FILES = 80;
    private static final int MAX_LINES_PER_FILE = 200;
    private static final int MAX_DEPTH = 5;

    private static final List<String> CODE_EXTENSIONS = List.of(
            ".java", ".kt", ".py", ".js", ".ts", ".tsx", ".jsx",
            ".go", ".rs", ".cpp", ".c", ".h", ".hpp",
            ".xml", ".yml", ".yaml", ".json", ".properties",
            ".sql", ".gradle", ".pom"
    );

    private static final List<String> EXCLUDE_DIRS = List.of(
            "node_modules", ".git", "target", "build", "dist",
            "__pycache__", ".idea", ".vscode", "test", "tests"
    );

    public ScanResult scan(Path repoPath) throws IOException {
        ScanResult result = new ScanResult();
        StringBuilder treeBuilder = new StringBuilder();
        List<Path> codeFiles = new ArrayList<>();

        try (Stream<Path> walk = Files.walk(repoPath, MAX_DEPTH)) {
            walk.filter(path -> !isExcluded(path, repoPath))
                    .forEach(path -> {
                        String relative = repoPath.relativize(path).toString();
                        int depth = relative.split("\\\\|/").length - 1;
                        treeBuilder.append("  ".repeat(Math.max(0, depth)))
                                .append(path.getFileName().toString())
                                .append(Files.isDirectory(path) ? "/" : "")
                                .append("\n");

                        if (Files.isRegularFile(path) && isCodeFile(path)
                                && result.contents.size() < MAX_FILES) {
                            codeFiles.add(path);
                        }
                    });
        }

        result.fileTree = treeBuilder.toString();
        result.fileCount = codeFiles.size();

        // 重要文件优先
        for (Path file : codeFiles) {
            if (isImportantFile(file.getFileName().toString())) {
                readFileContent(file, repoPath, result);
            }
        }
        for (Path file : codeFiles) {
            if (result.contents.size() >= MAX_FILES) break;
            if (!isImportantFile(file.getFileName().toString())) {
                readFileContent(file, repoPath, result);
            }
        }

        return result;
    }

    private void readFileContent(Path file, Path repoPath, ScanResult result) {
        try {
            List<String> lines = Files.readAllLines(file);
            String relative = repoPath.relativize(file).toString();
            StringBuilder content = new StringBuilder();
            int limit = Math.min(lines.size(), MAX_LINES_PER_FILE);
            for (int i = 0; i < limit; i++) {
                content.append(String.format("%4d| %s%n", i + 1, lines.get(i)));
            }
            if (lines.size() > MAX_LINES_PER_FILE) {
                content.append(String.format("    ... (省略 %d 行)%n", lines.size() - MAX_LINES_PER_FILE));
            }
            result.contents.add(new FileContent(relative, content.toString()));
        } catch (IOException ignored) {
            // 跳过无法读取的文件
        }
    }

    private boolean isImportantFile(String fileName) {
        return fileName.equals("pom.xml") || fileName.equals("build.gradle")
                || fileName.equals("application.yml") || fileName.equals("application.properties")
                || fileName.endsWith("Application.java") || fileName.equals("App.java")
                || fileName.equals("package.json") || fileName.equals("Dockerfile")
                || fileName.equals("README.md");
    }

    private boolean isCodeFile(Path path) {
        String name = path.getFileName().toString();
        return CODE_EXTENSIONS.stream().anyMatch(ext ->
                name.endsWith(ext) || name.equals(ext.substring(1)));
    }

    private boolean isExcluded(Path path, Path repoPath) {
        for (Path part : repoPath.relativize(path)) {
            if (EXCLUDE_DIRS.contains(part.toString())) return true;
        }
        return false;
    }

    // ===== 数据类型 =====

    public static class ScanResult {
        public String fileTree;
        public int fileCount;
        public final List<FileContent> contents = new ArrayList<>();
    }

    public record FileContent(String path, String content) {}
}
