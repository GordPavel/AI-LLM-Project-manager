Запуск проекта
1. Построить образ приложения (если исходники изменены)
```
docker compose build
```

2. Поднять сервисы
```
docker compose up -d
```

Пересборка при изменении кода
```
docker compose down
docker compose build
docker compose up -d
```
