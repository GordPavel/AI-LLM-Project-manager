package com.tbank.aihelper.TaskTracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbank.aihelper.TaskTracker.config.TrelloConfig;
import com.tbank.aihelper.TaskTracker.trello.pojo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrelloTrackerAdapterImpl implements TaskTrackerAdapter {

    private static final String API_BASE_URL = "https://api.trello.com/1";


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TrelloConfig config;

    private final Map<String, String> statusToListId = new ConcurrentHashMap<>();
    private final Map<String, String> listIdToStatus = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadBoardLists();
    }

    private void loadBoardLists() {
        String url = String.format("%s/boards/%s/lists?key=%s&token=%s",
                API_BASE_URL, config.getBoardId(), config.getApiKey(), config.getToken());

        TrelloList[] lists = restTemplate.getForObject(url, TrelloList[].class);

        if (lists == null || lists.length == 0) {
            throw new IllegalStateException("Не удалось загрузить списки с доски Trello: " + config.getBoardId());
        }

        statusToListId.clear();
        listIdToStatus.clear();

        for (TrelloList list : lists) {
            if (!list.isClosed()) {
                statusToListId.put(list.getName(), list.getId());
                listIdToStatus.put(list.getId(), list.getName());
            }
        }

        System.out.println("Trello: загружены списки → " + statusToListId.keySet());
    }

    private String buildUrl(String path) {
        return String.format("%s%s?key=%s&token=%s", API_BASE_URL, path, config.getApiKey(), config.getToken());
    }

    private TaskItem toTaskItem(TrelloCard card, List<TaskComment> comments) {
        String status = listIdToStatus.getOrDefault(card.getIdList(), "Unknown");

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

    @Override
    public Optional<TaskItem> getTask(String key) {
        try {
            TrelloCard card = restTemplate.getForObject(buildUrl("/cards/" + key), TrelloCard.class);
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
        String listId = statusToListId.get("To Do");
        if (listId == null) {
            throw new IllegalStateException("На доске Trello не найден список 'To Do'. Проверь названия списков.");
        }

        Map<String, String> params = Map.of(
                "name", summary,
                "desc", description,
                "idList", listId,
                "idMembers", assignee.isEmpty() ? "" : assignee
        );

        TrelloCard card = restTemplate.postForObject(buildUrl("/cards"), params, TrelloCard.class);
        return toTaskItem(Objects.requireNonNull(card), Collections.emptyList());
    }

    @Override
    public TaskItem updateTask(String key, String newSummary, String newDescription, String newStatus) {
        String listId = statusToListId.get(newStatus);
        if (listId == null) throw new IllegalArgumentException("Неизвестный статус: " + newStatus);

        restTemplate.put(buildUrl("/cards/" + key),
                Map.of("name", newSummary, "desc", newDescription, "idList", listId));

        return getTask(key).orElseThrow();
    }

    @Override
    public TaskItem transitionTask(String key, String newStatus) {
        String listId = statusToListId.get(newStatus);
        if (listId == null) throw new IllegalArgumentException("Неизвестный статус: " + newStatus);

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
                            .text(a.getData() != null ? a.getData().getText() : "")
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