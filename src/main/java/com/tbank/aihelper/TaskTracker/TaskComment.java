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
public class TaskComment {
    private String id;
    private String author;
    private ZonedDateTime createdAt;
    private String text;
}
