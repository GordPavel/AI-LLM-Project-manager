package com.tbank.aihelper.knowledgebase.bookstack;

import com.tbank.aihelper.knowledgebase.*;
import com.tbank.aihelper.knowledgebase.bookstack.config.BookStackProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class BookStackKnowledgebaseAdapter implements KnowledgebaseAdapter {

    private RestTemplate restTemplate;

    private BookStackProperties bookStackProperties;

    public BookStackKnowledgebaseAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bookStackProperties.getBearerToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Override
    public Optional<KnowledgeItem> getArticle(String id) {
        String url = bookStackProperties.getUrl() + "/pages/" + id;

        try {
            ResponseEntity<PageResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders()), PageResponse.class
            );

            return Optional.ofNullable(response.getBody())
                    .map(KnowledgeItem::from);

        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch page " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public KnowledgeItem createArticle(String title, String content, String bookId, String chapterId) {
        var request = CreatePageRequest.builder()
                .name(title)
                .html(content)
                .bookId(bookId != null ? Integer.valueOf(bookId) : null)
                .chapterId(chapterId != null ? Integer.valueOf(chapterId) : null)
                .priority(0)
                .build();

        if (request.getBookId() == null && request.getChapterId() == null) {
            throw new IllegalArgumentException("bookId или chapterId должен быть указан");
        }

        HttpEntity<CreatePageRequest> entity = new HttpEntity<>(request, authHeaders());

        try {
            ResponseEntity<PageResponse> response = restTemplate.exchange(
                    bookStackProperties.getUrl() + "/pages",
                    HttpMethod.POST,
                    entity,
                    PageResponse.class
            );

            return KnowledgeItem.from(response.getBody());

        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException("Invalid request to BookStack: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create page: " + e.getMessage(), e);
        }
    }

    @Override
    public KnowledgeItem updateArticle(String id, String newTitle, String newContent) {
        String url = bookStackProperties.getUrl() + "/pages/" + id;

        var request = UpdatePageRequest.builder()
                .name(newTitle)
                .html(newContent)
                .build();

        HttpEntity<UpdatePageRequest> entity = new HttpEntity<>(request, authHeaders());

        try {
            ResponseEntity<PageResponse> response = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, PageResponse.class
            );

            return KnowledgeItem.from(response.getBody());

        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Page with id " + id + " not found");
        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException("Invalid update data: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to update page " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteArticle(String id) {
        String url = bookStackProperties.getUrl() + "/pages/" + id;

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, new HttpEntity<>(authHeaders()), Void.class
            );

            return response.getStatusCode() == HttpStatus.NO_CONTENT;

        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete page " + id + ": " + e.getMessage(), e);
        }
    }
}
