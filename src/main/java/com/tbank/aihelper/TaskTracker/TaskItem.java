package com.tbank.aihelper.TaskTracker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskItem {
    private String key;
    private String summary;
    private String description;
    private String status;
    private String assignee;
    private String reporter;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String url;
}