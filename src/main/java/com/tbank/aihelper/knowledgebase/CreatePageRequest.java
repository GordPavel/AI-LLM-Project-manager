package com.tbank.aihelper.knowledgebase;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CreatePageRequest {

    @JsonProperty("book_id")
    private Integer bookId;

    @JsonProperty("chapter_id")
    private Integer chapterId;

    private String name;

    private String html;

    private String markdown;

    @Builder.Default
    private Integer priority = 0;

    private List<TagDto> tags;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagDto {
        private String name;

        @Builder.Default
        private String value = "";
    }
}
