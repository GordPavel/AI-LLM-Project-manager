package com.tbank.aihelper.knowledgebase;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class KnowledgeSearchResult {

    private String id;
    private String title;
    private String snippet;
    private String url;
}
