package com.tbank.aihelper.knowledgebase;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Builder
public class KnowledgeItem {
    private String id;
    private String title;
    private String content;
    private String bookId;
    private String chapterId;
    private String url;
    private String createdBy;
    private String updatedAt;
}
