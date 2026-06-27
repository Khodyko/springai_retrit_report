# Spring AI Retrit

Демо-проект для доклада по Spring AI: ассистент базы знаний корпоративного ретрита.

Полный план доклада и реализации — в [PLAN.MD](PLAN.MD).

## Запуск

```bash
docker compose up -d
export OPENAI_API_KEY=sk-...   # или: source .env после cp .env.example .env
mvn spring-boot:run
```

Ключ OpenAI **только через переменную окружения** `OPENAI_API_KEY` — в коде и конфигах не хранится.

```bash
cp .env.example .env
# отредактируйте .env, затем:
set -a && source .env && set +a
mvn spring-boot:run
```

Базовый URL: `http://localhost:8080`

---

## HTTP-запросы для live demo

Все запросы ниже — для IntelliJ HTTP Client, VS Code REST Client или импорта в Postman.
Подставьте `http://localhost:8080` или задайте переменную `baseUrl` в своём клиенте.

---

### Блок 1. Введение — проверка без RAG

```http
### Блок 1: приветствие без RAG-advisor
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Привет, кто ты?"
}
```

---

### Блок 2. RAG — Loader и базовый поиск

```http
### Блок 2: каталог searchable metadata (Liquibase)
GET http://localhost:8080/api/metadata/searchable

### Блок 2: текущий порог similarity из application.yml
GET http://localhost:8080/api/admin/search-config

### Блок 2: загрузить документы + metadata чанков в pgvector
POST http://localhost:8080/api/admin/load

### Блок 2: RAG-вопрос про ретрит (порог по умолчанию — высокий)
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Во сколько йога?"
}

### Блок 2: вопрос не про ретрит — высокий порог, контекст не попадает
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Что такое SOLID?"
}
```

---

### Блок 2b. Настройка точности поиска (Spring AI + application.yml)

```http
### Блок 2b: понизить порог similarity (демо «freud»)
PUT http://localhost:8080/api/admin/search-config
Content-Type: application/json

{
  "similarityThreshold": 0.35,
  "topK": 8
}

### Блок 2b: тот же off-topic вопрос — низкий порог пропускает слабый контекст → freud
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Что такое SOLID?"
}

### Блок 2b: вернуть строгий порог
PUT http://localhost:8080/api/admin/search-config
Content-Type: application/json

{
  "similarityThreshold": 0.78,
  "topK": 4
}

### Блок 2b: off-topic снова — честный отказ без галлюцинаций
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Что такое SOLID?"
}
```

---

### Блок 3. Жизненный цикл запроса (advisors)

```http
### Блок 3: вопрос про ретрит — смотреть augmented prompt в логах
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Кто ведёт медитацию?"
}

### Блок 3: ещё один запрос — смотреть цепочку advisors в логах
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Где завтрак?"
}
```

---

### Блок 4. Tools — поиск по metadata через ИИ

```http
### Блок 4: каталог допустимых metadata
GET http://localhost:8080/api/metadata/searchable

### Блок 4: ИИ выберет source=speakers, topic=yoga
POST http://localhost:8080/api/agent/chat
Content-Type: application/json

{
  "message": "Кто ведёт йогу?"
}

### Блок 4: ИИ выберет source=schedule, topic=yoga
POST http://localhost:8080/api/agent/chat
Content-Type: application/json

{
  "message": "Во сколько йога?"
}

### Блок 4: ИИ выберет source=location
POST http://localhost:8080/api/agent/chat
Content-Type: application/json

{
  "message": "Как добраться?"
}

### Блок 4: не про ретрит — tool не вызывается
POST http://localhost:8080/api/agent/chat
Content-Type: application/json

{
  "message": "Что такое SOLID?"
}
```

---

### Блок 5. История сообщений (PostgreSQL, по user id)

```http
### Блок 5: первый вопрос пользователя alice — сохраняется в spring_ai_chat_memory
POST http://localhost:8080/api/chat
Content-Type: application/json
X-User-Id: alice

{
  "message": "Кто ведёт йогу?"
}

### Блок 5: уточнение alice — история из Postgres по X-User-Id
POST http://localhost:8080/api/chat
Content-Type: application/json
X-User-Id: alice

{
  "message": "А во сколько её сессия?"
}

### Блок 5: bob — отдельная история, контекст alice недоступен
POST http://localhost:8080/api/chat
Content-Type: application/json
X-User-Id: bob

{
  "message": "А во сколько её сессия?"
}
```

---

### Блок 6 / 7. Кейсы улучшения качества поиска (Spring AI)

См. [PLAN.MD — Кейсы настройки качества поиска](PLAN.MD#кейсы-настройки-качества-поиска-spring-ai).

```http
### Кейс A: увеличить topK — больше контекста, риск шума
PUT http://localhost:8080/api/admin/search-config
Content-Type: application/json

{
  "similarityThreshold": 0.78,
  "topK": 12
}

POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Расскажи про все утренние активности"
}

### Кейс B: уменьшить topK — точнее, но можно потерять детали
PUT http://localhost:8080/api/admin/search-config
Content-Type: application/json

{
  "similarityThreshold": 0.78,
  "topK": 2
}

POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Расскажи про все утренние активности"
}

### Кейс C: строгий порог + metadata через agent (лучшее качество)
PUT http://localhost:8080/api/admin/search-config
Content-Type: application/json

{
  "similarityThreshold": 0.80,
  "topK": 4
}

POST http://localhost:8080/api/agent/chat
Content-Type: application/json

{
  "message": "Какие правила на ретрите про телефоны?"
}
```
