# Transaction Service

Микросервис для обработки банковских транзакций и управления лимитами.

## Запуск

1. Установите PostgreSQL и создайте базу `bank_db`.
2. Настройте `application.yml` с вашими данными БД.
3. Получите API-ключ от Open Exchange Rates и добавьте в `ExchangeRateClient`.
4. Выполните `mvn spring-boot:run`.

## API
Документация доступна через Swagger: `/swagger-ui.html`.

### Endpoints
- `POST /api/transactions` — Приём транзакций.
- `GET /api/client/exceeded` — Получение транзакций, превысивших лимит.
- `POST /api/client/limits` — Установка нового лимита.