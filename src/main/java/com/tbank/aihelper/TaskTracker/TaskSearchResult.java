package com.tbank.aihelper.TaskTracker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSearchResult {
    private String key;
    private String summary;
    private String status;
    private String assignee;
    private String url;
}