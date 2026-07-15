package com.codeinsight.controller;

import com.codeinsight.common.Result;
import com.codeinsight.dto.QaRequest;
import com.codeinsight.dto.QaResponse;
import com.codeinsight.dto.ReviewRequest;
import com.codeinsight.entity.Review;
import com.codeinsight.security.SecurityUser;
import com.codeinsight.service.KnowledgeBaseService;
import com.codeinsight.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final com.codeinsight.service.PdfService pdfService;

    @PostMapping
    public Result<Review> create(@Valid @RequestBody ReviewRequest request,
                                 @AuthenticationPrincipal SecurityUser user) {
        return Result.success(reviewService.create(user.getUserId(), request.getRepositoryId()));
    }

    @GetMapping("/{id}")
    public Result<Review> getById(@PathVariable Long id,
                                  @AuthenticationPrincipal SecurityUser user) {
        return Result.success(reviewService.getById(id, user.getUserId()));
    }

    @GetMapping
    public Result<java.util.List<Review>> list(@AuthenticationPrincipal SecurityUser user) {
        return Result.success(reviewService.listByUser(user.getUserId()));
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<byte[]> getReport(@PathVariable Long id,
                                            @AuthenticationPrincipal SecurityUser user) {
        String markdown = reviewService.getReportMarkdown(id, user.getUserId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.md")
                .contentType(MediaType.TEXT_MARKDOWN)
                .body(markdown.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/{id}/report/pdf")
    public ResponseEntity<byte[]> getReportPdf(@PathVariable Long id,
                                               @AuthenticationPrincipal SecurityUser user) {
        String markdown = reviewService.getReportMarkdown(id, user.getUserId());
        byte[] pdfBytes = pdfService.markdownToPdf(markdown);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @AuthenticationPrincipal SecurityUser user) {
        reviewService.delete(id, user.getUserId());
        return Result.success();
    }

    @PutMapping("/{id}/memo")
    public Result<Void> updateMemo(@PathVariable Long id,
                                   @AuthenticationPrincipal SecurityUser user,
                                   @RequestBody Map<String, String> body) {
        reviewService.updateMemo(id, user.getUserId(), body.get("memo"));
        return Result.success();
    }

    // ===== Q&A =====

    @PostMapping("/{id}/qa")
    public Result<QaResponse> ask(@PathVariable Long id,
                                  @Valid @RequestBody QaRequest request,
                                  @AuthenticationPrincipal SecurityUser user) {
        Review review = reviewService.getById(id, user.getUserId());
        return Result.success(knowledgeBaseService.qa(review.getRepositoryId(), request.getQuestion()));
    }

    @GetMapping("/{id}/qa/ready")
    public Result<Boolean> isQaReady(@PathVariable Long id,
                                     @AuthenticationPrincipal SecurityUser user) {
        Review review = reviewService.getById(id, user.getUserId());
        return Result.success(knowledgeBaseService.isReady(review.getRepositoryId()));
    }
}
