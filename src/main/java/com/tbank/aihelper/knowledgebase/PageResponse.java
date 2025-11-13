package com.tbank.aihelper.knowledgebase;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse {

    private Long id;

    @JsonProperty("book_id")
    private Long bookId;

    @JsonProperty("chapter_id")
    private Long chapterId;

    @JsonProperty("owned_by")
    private UserInfo ownedBy;

    @JsonProperty("created_by")
    private UserInfo createdBy;

    @JsonProperty("updated_by")
    private UserInfo updatedBy;

    private String name;

    private String slug;

    private String html;

    private String markdown;

    private Integer priority;

    private Boolean draft;

    private Boolean template;

    @JsonProperty("revision_count")
    private Integer revisionCount;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    private String url;

    private List<TagResponse> tags;

    @Data
    public static class UserInfo {
        private Long id;
        private String name;
        private String slug;
        private String email;
    }

    @Data
    public static class TagResponse {
        private Long id;
        private String name;
        private String value;
        private Integer order;
    }
}
