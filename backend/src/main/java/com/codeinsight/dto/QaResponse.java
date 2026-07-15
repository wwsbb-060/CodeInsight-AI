package com.codeinsight.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class QaResponse {

    private String answer;
    private List<Reference> references;

    @Data
    @AllArgsConstructor
    public static class Reference {
        private String file;
        private int startLine;
        private int endLine;
        private String snippet;
    }
}
