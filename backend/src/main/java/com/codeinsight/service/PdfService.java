package com.codeinsight.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class PdfService {

    private static final String HTML_TEMPLATE = """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
            <meta charset="UTF-8" />
            <style>
              body { font-family: 'SimHei', sans-serif; font-size: 13px; line-height: 1.7;
                     color: #333; padding: 20px; }
              h1 { font-size: 22px; border-bottom: 2px solid #1677ff; padding-bottom: 8px; }
              h2 { font-size: 16px; margin-top: 24px; border-left: 3px solid #1677ff; padding-left: 8px; }
              h3 { font-size: 14px; }
              code { background: #f5f5f5; padding: 2px 5px; border-radius: 3px; font-size: 12px; }
              pre { background: #2d2d2d; color: #e0e0e0; padding: 14px; border-radius: 6px;
                    overflow-x: auto; font-size: 12px; line-height: 1.5; }
              pre code { background: none; padding: 0; }
              table { border-collapse: collapse; width: 100%; margin: 10px 0; }
              th, td { border: 1px solid #ddd; padding: 8px 12px; text-align: left; }
              th { background: #f6f8fa; }
              blockquote { border-left: 4px solid #1677ff; background: #f0f5ff;
                           padding: 8px 12px; margin: 10px 0; }
              ul, ol { padding-left: 20px; }
              hr { border: none; border-top: 1px solid #eee; margin: 20px 0; }
              a { color: #1677ff; }
              @page { size: A4; margin: 25mm 20mm; }
            </style>
            </head>
            <body>%s</body>
            </html>
            """;

    public byte[] markdownToPdf(String markdown) {
        try {
            // Markdown → HTML
            Parser parser = Parser.builder().build();
            Node document = parser.parse(markdown);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            String htmlBody = renderer.render(document);
            String fullHtml = HTML_TEMPLATE.replace("%s", htmlBody);

            // HTML → PDF
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // 注册中文字体
            try {
                builder.useFont(
                        new java.io.File("C:/Windows/Fonts/simhei.ttf"),
                        "SimHei");
            } catch (Exception ignored) {}

            builder.withHtmlContent(fullHtml, null);
            builder.toStream(out);
            builder.run();

            return out.toByteArray();

        } catch (Exception e) {
            log.error("PDF 生成失败", e);
            throw new RuntimeException("PDF 生成失败: " + e.getMessage());
        }
    }
}
