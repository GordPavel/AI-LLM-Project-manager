package com.tbank.aihelper.TaskTracker.trello.pojo;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrelloCard {
    private String id;
    private String name;
    private String desc;
    private String url;
    private String idList;
    private List<String> idMembers;
    private String dateLastActivity;
}