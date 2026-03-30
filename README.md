# Shop Backend (Ktor + PostgreSQL + Exposed + JWT + Redis + RabbitMQ)

Готовый учебный backend-сервис интернет-магазина.

## Что реализовано
- регистрация и логин через JWT
- пользовательские маршруты: товары, оформление заказа, отмена заказа, история покупок
- админские маршруты: создание, изменение и удаление товаров, статистика заказов
- PostgreSQL + Exposed ORM
- миграции Flyway
- Redis-кэш заказов
- RabbitMQ producer + отдельный worker consumer
- OpenAPI YAML + Swagger UI
- Docker, docker-compose, GitHub Actions
- шаблон для деплоя в Render

## Важное
Сборка Docker для backend настроена через `shadowJar`, чтобы приложение поднималось сразу, даже если тесты ты будешь доделывать позже.

## Структура
- `app` — основной Ktor backend
- `worker` — фоновый consumer для очереди
- `app/src/main/resources/db/migration` — Flyway SQL миграции
- `app/src/main/resources/openapi/documentation.yaml` — OpenAPI спецификация

## Быстрый запуск через Docker
### 1. Требования
- Docker Desktop

### 2. Запуск
```bash
docker compose down -v
docker compose up --build
```

### 3. Проверка
- API: `http://localhost:8080/`
- Swagger UI: `http://localhost:8080/swagger`
- OpenAPI YAML: `http://localhost:8080/openapi`
- RabbitMQ UI: `http://localhost:15672`

## Как создать администратора
Пользователь с email, который заканчивается на `@admin.local`, получает роль `ADMIN`.

Пример регистрации:
```json
{
  "email": "boss@admin.local",
  "password": "secret123",
  "fullName": "Main Admin"
}
```

## Примеры запросов
### Регистрация
```bash
curl -X POST http://localhost:8080/auth/register   -H "Content-Type: application/json"   -d '{"email":"boss@admin.local","password":"secret123","fullName":"Main Admin"}'
```

### Логин
```bash
curl -X POST http://localhost:8080/auth/login   -H "Content-Type: application/json"   -d '{"email":"boss@admin.local","password":"secret123"}'
```

### Создание товара
```bash
curl -X POST http://localhost:8080/products   -H "Authorization: Bearer <JWT_TOKEN>"   -H "Content-Type: application/json"   -d '{"name":"Phone","description":"128GB","price":"599.99","stock":10}'
```

### Создание заказа
```bash
curl -X POST http://localhost:8080/orders   -H "Authorization: Bearer <JWT_TOKEN>"   -H "Content-Type: application/json"   -d '{"items":[{"productId":1,"quantity":2}]}'
```

## Если захочешь вернуть тесты в обычную сборку
1. Исправь или допиши тесты.
2. Запускай:
```bash
gradle :app:build
gradle :app:test
```

## Деплой в Render
1. Залей проект в GitHub.
2. В Render создай новый Blueprint из репозитория.
3. Укажи `render.yaml`.
4. После деплоя открой URL web-сервиса.
