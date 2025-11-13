package com.tbank.aihelper.TaskTracker.trello.pojo;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrelloAction {
    private String id;
    private String type;
    private String date;
    private Member memberCreator;
    private ActionData data;

    @Getter @Setter
    public static class Member {
        private String fullName;
    }

    @Getter @Setter
    public static class ActionData {
        private String text;
    }
}