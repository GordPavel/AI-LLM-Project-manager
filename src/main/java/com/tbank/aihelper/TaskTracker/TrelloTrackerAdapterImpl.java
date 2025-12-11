package com.tbank.aihelper.TaskTracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbank.aihelper.TaskTracker.config.TrelloConfig;
import com.tbank.aihelper.TaskTracker.trello.pojo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrelloTrackerAdapterImpl implements TaskTrackerAdapter {

    private static final String API_BASE_URL = "https://api.trello.com/1";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TrelloConfig config;



    private String buildUrl(String path) {
        return String.format("%s%s?key=%s&token=%s", API_BASE_URL, path, config.getApiKey(), config.getToken());
    }

    private String getListIdByName(String statusName) {
        String url = String.format("%s/boards/%s/lists?key=%s&token=%s",
                API_BASE_URL, config.getBoardId(), config.getApiKey(), config.getToken());

        TrelloList[] lists = restTemplate.getForObject(url, TrelloList[].class);
        if (lists == null || lists.length == 0) {
            throw new IllegalStateException("Не удалось получить списки с доски Trello");
        }

        System.out.println("Доступные колонки:");
        Arrays.stream(lists).forEach(l -> System.out.println("  - " + l.getName() + " (id: " + l.getId() + ", closed: " + l.isClosed() + ")"));

        return Arrays.stream(lists)
                .filter(list -> !list.isClosed())
                .filter(list -> list.getName().equalsIgnoreCase(statusName.trim()))
                .findFirst()
                .map(TrelloList::getId)
                .orElse("69371e8deac202ae25df3aaa"); // ← fallback на реальный ID
    }

    private TaskItem toTaskItem(TrelloCard card, List<TaskComment> comments) {
        String status = card.getIdList() != null
                ? getStatusNameByListId(card.getIdList())
                : "Unknown";

        ZonedDateTime updatedAt = card.getDateLastActivity() != null
                ? ZonedDateTime.parse(card.getDateLastActivity()).withZoneSameInstant(ZoneId.of("UTC"))
                : ZonedDateTime.now(ZoneId.of("UTC"));

        String assignee = card.getIdMembers() != null && !card.getIdMembers().isEmpty()
                ? card.getIdMembers().get(0)
                : "Unassigned";

        return TaskItem.builder()
                .key(card.getId())
                .summary(card.getName())
                .description(card.getDesc() != null ? card.getDesc() : "")
                .status(status)
                .assignee(assignee)
                .updatedAt(updatedAt)
                .url(card.getUrl())
                .comments(comments)
                .build();
    }

    private String getStatusNameByListId(String idList) {
        String url = String.format("%s/lists/%s?key=%s&token=%s", API_BASE_URL, idList, config.getApiKey(), config.getToken());
        TrelloList list = restTemplate.getForObject(url, TrelloList.class);
        return list != null && !list.isClosed() ? list.getName() : "Unknown";
    }

    @Override
    public Optional<TaskItem> getTask(String key) {
        System.out.println(key);
        try {
            TrelloCard card = restTemplate.getForObject(buildUrl("/cards/" + key), TrelloCard.class);
            System.out.println(key);
            System.out.println(card);
            if (card == null) return Optional.empty();
            return Optional.of(toTaskItem(card, getComments(key)));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения задачи " + key, e);
        }
    }

    @Override
    public TaskItem createTask(String queue, String summary, String description, String assignee) {
        String listId = getListIdByName("To Do"); // ← всегда свежий
        System.out.println(listId);
        Map<String, String> params = Map.of(
                "name", summary,
                "desc", description,
                "idList", listId,
                "idMembers", assignee
        );

        TrelloCard card = restTemplate.postForObject(buildUrl("/cards"), params, TrelloCard.class);
        return toTaskItem(Objects.requireNonNull(card), Collections.emptyList());
    }

    @Override
    public TaskItem updateTask(String key, String newSummary, String newDescription, String newStatus) {
        String listId = getListIdByName(newStatus);

        restTemplate.put(buildUrl("/cards/" + key),
                Map.of("name", newSummary, "desc", newDescription, "idList", listId));

        return getTask(key).orElseThrow();
    }

    @Override
    public TaskItem transitionTask(String key, String newStatus) {
        String listId = getListIdByName(newStatus);

        restTemplate.put(buildUrl("/cards/" + key), Map.of("idList", listId));
        return getTask(key).orElseThrow();
    }

    @Override
    public boolean deleteTask(String key) {
        try {
            restTemplate.delete(buildUrl("/cards/" + key));
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    @Override
    public List<TaskComment> getComments(String key) {
        try {
            TrelloAction[] actions = restTemplate.getForObject(
                    buildUrl("/cards/" + key + "/actions?filter=commentCard"), TrelloAction[].class);

            if (actions == null) return Collections.emptyList();

            return Arrays.stream(actions)
                    .map(a -> TaskComment.builder()
                            .id(a.getId())
                            .author(a.getMemberCreator() != null ? a.getMemberCreator().getFullName() : "Unknown")
                            .createdAt(ZonedDateTime.parse(a.getDate()))
                            .text(a.getData() != null && a.getData().getText() != null ? a.getData().getText() : "")
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Ошибка загрузки комментариев: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public TaskComment addComment(String key, String text, String author) {
        TrelloAction action = restTemplate.postForObject(
                buildUrl("/cards/" + key + "/actions/comments"),
                Map.of("text", text),
                TrelloAction.class);

        return TaskComment.builder()
                .id(action.getId())
                .author(action.getMemberCreator() != null ? action.getMemberCreator().getFullName() : "Bot")
                .createdAt(ZonedDateTime.parse(action.getDate()))
                .text(action.getData() != null ? action.getData().getText() : text)
                .build();
    }

    @Override
    public boolean deleteComment(String key, String commentId) {
        try {
            restTemplate.delete(buildUrl("/actions/" + commentId));
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }
}