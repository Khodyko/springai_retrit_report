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

### IntelliJ IDEA

1. Откройте этот файл (`README.MD`) или [demo.http](demo.http) — оба в формате **HTTP Client**.
2. Слева от каждого запроса появится **▶ Run Request** (зелёная стрелка в gutter).
3. Если стрелок нет: **Settings → Languages & Frameworks → Markdown** → включите **Inject languages in code fences**.
4. Все запросы подряд (блок за блоком) удобнее запускать из [demo.http](demo.http) — там одна переменная `@baseUrl`.

Формат совместим также с VS Code REST Client и импортом в Postman.

---

### Блок 1. Введение — проверка без RAG

Приветствие **без** RAG-advisor (`"rag": false`).

```http
### Блок 1: приветствие без RAG-advisor
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Привет, кто ты?",
  "rag": false
}
```

---

### Блок 2. RAG — Loader и базовый поиск

Каталог metadata из Liquibase:

```http
### Блок 2: каталог searchable metadata (Liquibase)
GET http://localhost:8080/api/metadata/searchable
```

Текущий порог similarity:

```http
### Блок 2: текущий порог similarity из application.yml
GET http://localhost:8080/api/admin/search-config
```

Загрузка документов в pgvector (live на сцене):

```http
### Блок 2: загрузить документы + metadata чанков в pgvector
POST http://localhost:8080/api/admin/load
```

RAG-вопрос про ретрит:

```http
### Блок 2: RAG-вопрос про ретрит (порог по умолчанию — высокий)
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Во сколько йога?"
}
```

Вопрос не про ретрит — контекст не попадает:

```http
### Блок 2: вопрос не про ретрит — высокий порог, контекст не попадает
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Что такое SOLID?"
}
```

---

### Блок 2b. Настройка точности поиска (Spring AI + application.yml)

Понизить порог similarity (демо «freud»):

```http
### Блок 2b: понизить порог similarity (демо «freud»)
PUT http://localhost:8080/api/admin/search-config
Content-Type: application/json

{
  "similarityThreshold": 0.35,
  "topK": 8
}
```

Тот же off-topic вопрос — слабый контекст проходит:

```http
### Блок 2b: тот же off-topic вопрос — низкий порог пропускает слабый контекст
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Что такое SOLID?"
}
```

Вернуть строгий порог:

```http
### Блок 2b: вернуть строгий порог
PUT http://localhost:8080/api/admin/search-config
Content-Type: application/json

{
  "similarityThreshold": 0.78,
  "topK": 4
}
```

Off-topic снова — честный отказ:

```http
### Блок 2b: off-topic снова — честный отказ без галлюцинаций
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Что такое SOLID?"
}
```

---

### Блок 3. Жизненный цикл запроса (advisors)

Смотреть augmented prompt в логах:

```http
### Блок 3: вопрос про ретрит — смотреть augmented prompt в логах
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Кто ведёт медитацию?"
}
```

Цепочка advisors в логах:

```http
### Блок 3: ещё один запрос — смотреть цепочку advisors в логах
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Где завтрак?"
}
```

---

### Блок 4. Tools — поиск по metadata через ИИ

Каталог допустимых metadata:

```http
### Блок 4: каталог допустимых metadata
GET http://localhost:8080/api/metadata/searchable
```

ИИ выберет `source=speakers`, `topic=yoga`:

```http
### Блок 4: ИИ выберет source=speakers, topic=yoga
POST http://localhost:8080/api/agent/chat
Content-Type: application/json

{
  "message": "Кто ведёт йогу?"
}
```

ИИ выберет `source=schedule`, `topic=yoga`:

```http
### Блок 4: ИИ выберет source=schedule, topic=yoga
POST http://localhost:8080/api/agent/chat
Content-Type: application/json

{
  "message": "Во сколько йога?"
}
```

ИИ выберет `source=location`:

```http
### Блок 4: ИИ выберет source=location
POST http://localhost:8080/api/agent/chat
Content-Type: application/json

{
  "message": "Как добраться?"
}
```

Не про ретрит — tool не вызывается:

```http
### Блок 4: не про ретрит — tool не вызывается
POST http://localhost:8080/api/agent/chat
Content-Type: application/json

{
  "message": "Что такое SOLID?"
}
```

---

### Блок 5. История сообщений (PostgreSQL, по user id)

Первый вопрос пользователя `alice`:

```http
### Блок 5: первый вопрос пользователя alice — сохраняется в spring_ai_chat_memory
POST http://localhost:8080/api/chat
Content-Type: application/json
X-User-Id: alice

{
  "message": "Кто ведёт йогу?"
}
```

Уточнение `alice` — история из Postgres:

```http
### Блок 5: уточнение alice — история из Postgres по X-User-Id
POST http://localhost:8080/api/chat
Content-Type: application/json
X-User-Id: alice

{
  "message": "А во сколько её сессия?"
}
```

`bob` — отдельная история:

```http
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

**Кейс A** — увеличить topK:

```http
### Кейс A: увеличить topK — больше контекста, риск шума
PUT http://localhost:8080/api/admin/search-config
Content-Type: application/json

{
  "similarityThreshold": 0.78,
  "topK": 12
}
```

```http
### Кейс A: запрос с увеличенным topK
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Расскажи про все утренние активности"
}
```

**Кейс B** — уменьшить topK:

```http
### Кейс B: уменьшить topK — точнее, но можно потерять детали
PUT http://localhost:8080/api/admin/search-config
Content-Type: application/json

{
  "similarityThreshold": 0.78,
  "topK": 2
}
```

```http
### Кейс B: запрос с уменьшенным topK
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "message": "Расскажи про все утренние активности"
}
```

**Кейс C** — строгий порог + agent:

```http
### Кейс C: строгий порог + metadata через agent (лучшее качество)
PUT http://localhost:8080/api/admin/search-config
Content-Type: application/json

{
  "similarityThreshold": 0.80,
  "topK": 4
}
```

```http
### Кейс C: вопрос про правила ретрита через agent
POST http://localhost:8080/api/agent/chat
Content-Type: application/json

{
  "message": "Какие правила на ретрите про телефоны?"
}
```
