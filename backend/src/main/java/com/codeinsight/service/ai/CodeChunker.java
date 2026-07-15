package com.codeinsight.service.ai;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码切块器。
 * 将一个代码文件按语义边界切割成多个文本块，每个块携带文件路径和起止行号。
 */
@Component
public class CodeChunker {

    private static final int MAX_CHUNK_CHARS = 2000;

    // Java 代码：匹配 public/private/protected 开头的方法或类声明（含注解前导行）
    private static final Pattern JAVA_BOUNDARY = Pattern.compile(
            "\\s*(public|private|protected)\\s+(static\\s+)?(class|interface|\\w+\\s+\\w+\\()");

    /**
     * 将文件内容按语义边界切块。
     * Java 文件按方法切，其他文件整体一块。
     */
    public List<Chunk> chunk(String filePath, String content) {
        List<Chunk> chunks = new ArrayList<>();

        if (filePath.endsWith(".java")) {
            chunks.addAll(chunkJava(filePath, content));
        } else {
            if (content.length() <= MAX_CHUNK_CHARS) {
                chunks.add(new Chunk(filePath, 1, countLines(content), content));
            } else {
                chunks.addAll(chunkBySize(filePath, content));
            }
        }

        return chunks;
    }

    private List<Chunk> chunkJava(String filePath, String content) {
        List<Chunk> chunks = new ArrayList<>();
        String[] lines = content.split("\n", -1);
        List<Integer> boundaries = new ArrayList<>();
        boundaries.add(0); // 文件头（package / import）

        for (int i = 0; i < lines.length; i++) {
            Matcher m = JAVA_BOUNDARY.matcher(lines[i]);
            if (m.find()) {
                // 如果前一行是注解，把边界前移
                int boundary = (i > 0 && lines[i - 1].trim().startsWith("@")) ? i - 1 : i;
                if (boundary > boundaries.get(boundaries.size() - 1)) {
                    boundaries.add(boundary);
                }
            }
        }
        boundaries.add(lines.length); // 文件尾

        // 按边界切块
        for (int i = 0; i < boundaries.size() - 1; i++) {
            int start = boundaries.get(i);
            int end = boundaries.get(i + 1);
            StringBuilder sb = new StringBuilder();
            for (int j = start; j < end; j++) {
                sb.append(lines[j]).append("\n");
            }
            String chunkText = sb.toString().trim();
            if (chunkText.isEmpty()) continue;

            // 超长硬切
            if (chunkText.length() > MAX_CHUNK_CHARS) {
                chunks.addAll(chunkBySize(filePath, chunkText));
            } else {
                chunks.add(new Chunk(filePath, start + 1, end, chunkText));
            }
        }

        // 合并小块：如果第一个块（package/import）太短，合并到下一个
        if (chunks.size() >= 2 && chunks.get(0).content.length() < 200) {
            Chunk first = chunks.remove(0);
            Chunk second = chunks.get(0);
            chunks.set(0, new Chunk(filePath, first.startLine, second.endLine,
                    first.content + "\n" + second.content));
        }

        return chunks;
    }

    private List<Chunk> chunkBySize(String filePath, String content) {
        List<Chunk> chunks = new ArrayList<>();
        String[] lines = content.split("\n", -1);
        int lineCount = lines.length;

        for (int offset = 0; offset < lineCount; ) {
            StringBuilder sb = new StringBuilder();
            int startLine = offset + 1;
            int consumed = 0;

            while (offset < lineCount && sb.length() < MAX_CHUNK_CHARS) {
                sb.append(lines[offset]).append("\n");
                offset++;
                consumed++;
            }
            chunks.add(new Chunk(filePath, startLine, startLine + consumed - 1, sb.toString().trim()));
        }
        return chunks;
    }

    private int countLines(String content) {
        return content.split("\n", -1).length;
    }

    /**
     * 一个代码块，携带文件路径、起止行号、代码内容。
     */
    public record Chunk(String filePath, int startLine, int endLine, String content) {
        public String toMetadata() {
            return String.format("%s L%d-%d", filePath, startLine, endLine);
        }
    }
}
