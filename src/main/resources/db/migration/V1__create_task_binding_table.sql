CREATE TABLE task_binding (
                              id BIGSERIAL PRIMARY KEY,
                              trello_card_id VARCHAR(255) NOT NULL UNIQUE,
                              telegram_chat_id BIGINT NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для быстрого поиска
CREATE INDEX idx_trello_card_id ON task_binding(trello_card_id);
CREATE INDEX idx_telegram_chat_id ON task_binding(telegram_chat_id);