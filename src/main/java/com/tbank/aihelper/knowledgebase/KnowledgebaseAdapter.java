package com.tbank.aihelper.knowledgebase;

import java.util.List;
import java.util.Optional;

public interface KnowledgebaseAdapter {
    Optional<KnowledgeItem> getArticle(String id);

    List<KnowledgeSearchResult> search(String query, int limit);

    KnowledgeItem createArticle(String title, String content, String bookId, String chapterId);

    KnowledgeItem updateArticle(String id, String newTitle, String newContent);

    boolean deleteArticle(String id);
}
