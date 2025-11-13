package com.tbank.aihelper.TaskTracker;

import java.util.List;
import java.util.Optional;

public interface TaskTrackerAdapter {

    Optional<TaskItem> getTask(String key);

    TaskItem createTask(String queue, String summary, String description, String assignee);

    TaskItem updateTask(String key, String newSummary, String newDescription, String newStatus);

    TaskItem transitionTask(String key, String newStatus);

    boolean deleteTask(String key);

    List<TaskComment> getComments(String key);

    TaskComment addComment(String key, String text, String author);

    boolean deleteComment(String key, String commentId);
}

