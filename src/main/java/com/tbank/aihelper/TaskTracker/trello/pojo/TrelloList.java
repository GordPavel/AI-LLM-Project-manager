package com.tbank.aihelper.TaskTracker.trello.pojo;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrelloList {
    private String id;
    private String name;
    private boolean closed;
}