package com.tbank.aihelper.knowledgebase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeItem {

    private String id;
    private String title;
    private String content;
    private String bookId;
    private String chapterId;
    private String url;
    private String createdBy;
    private String updatedAt;

    public CreatePageRequest toCreateRequest() {
        return CreatePageRequest.builder()
                .bookId(bookId != null ? Integer.parseInt(bookId) : null)
                .chapterId(chapterId != null ? Integer.parseInt(chapterId) : null)
                .name(title)
                .html(content)
                .priority(0)
                .build();
    }

    public static KnowledgeItem from(PageResponse response) {
        return KnowledgeItem.builder()
                .id(String.valueOf(response.getId()))
                .title(response.getName())
                .content(response.getHtml())
                .bookId(String.valueOf(response.getBookId()))
                .chapterId(response.getChapterId() != null ? String.valueOf(response.getChapterId()) : null)
                .url(response.getUrl())
                .createdBy(response.getCreatedBy() != null ? response.getCreatedBy().getName() : null)
                .updatedAt(response.getUpdatedAt() != null ? response.getUpdatedAt().toString() : null)
                .build();
    }
}
