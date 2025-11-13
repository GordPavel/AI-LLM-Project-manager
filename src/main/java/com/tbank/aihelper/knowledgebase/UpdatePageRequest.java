package com.tbank.aihelper.knowledgebase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePageRequest {

    private String name;

    private String html;

    private String markdown;

    private Integer priority;

    private Integer chapterId;

    private List<CreatePageRequest.TagDto> tags;
}
