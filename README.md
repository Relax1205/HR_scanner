# HR Scanner

HR Scanner - веб-платформа для первичного анализа резюме кандидатов под выбранную вакансию. Система принимает PDF/DOC/DOCX-файл, извлекает из него текст, отправляет его в LLM через OpenRouter, получает структурированный JSON с оценкой соответствия кандидата вакансии и сохраняет результат в PostgreSQL.

Проект собран как связка из Spring Boot приложения, Node.js сервиса анализа резюме и PostgreSQL. Для локального и серверного запуска предусмотрены Docker Compose, Dockerfile'ы, Kubernetes-манифесты, Render-конфигурация и GitHub Actions pipeline.

## Содержание

- [Возможности](#возможности)
- [Архитектура](#архитектура)
- [Технологический стек](#технологический-стек)
- [Структура проекта](#структура-проекта)
- [Требования](#требования)
- [Переменные окружения](#переменные-окружения)
- [Быстрый запуск через Docker Compose](#быстрый-запуск-через-docker-compose)
- [Локальная разработка без Docker Compose](#локальная-разработка-без-docker-compose)
- [Сборка и тесты](#сборка-и-тесты)
- [HTTP-маршруты и API](#http-маршруты-и-api)
- [Модель данных](#модель-данных)
- [CI/CD](#cicd)
- [Kubernetes](#kubernetes)
- [Render](#render)
- [Production checklist](#production-checklist)
- [Troubleshooting](#troubleshooting)

## Возможности

- Регистрация и вход пользователей через Spring Security.
- Защищенный веб-интерфейс на Thymeleaf.
- Выбор целевой роли: аналитик данных, менеджер в ИИ, технический аналитик в ИИ, инженер данных.
- Загрузка резюме в форматах PDF, DOC и DOCX.
- Извлечение текста из документов на Node.js.
- Анализ резюме через OpenRouter API.
- Получение результата в строгом JSON-формате.
- Сохранение кандидатов, профессии, процента соответствия и навыков в PostgreSQL.
- История обработанных резюме с возможностью удаления записей.
- Контейнеризация backend/frontend сервисов.
- CI/CD pipeline для тестирования, сборки Docker-образов и проверки деплоя в Kubernetes.

## Архитектура

```text
Browser
  |
  | Spring Security / Thymeleaf UI
  v
Spring Boot backend :8080
  |
  | JPA / Hibernate
  v
PostgreSQL :5432

Browser
  |
  | multipart/form-data: resume + job
  v
Node.js analyzer :3001
  |
  | OpenRouter Chat Completions API
  v
LLM model

Node.js analyzer
  |
  | writes /data/data.json
  v
Shared Docker volume
  |
  | Spring reads /data/data.json
  v
PostgreSQL
```

Основной пользовательский сценарий:

1. Пользователь регистрируется или входит в систему.
2. Открывает `/go`, выбирает вакансию и загружает резюме.
3. Браузер отправляет файл на Node.js сервис `POST /api/analyze`.
4. Node.js сервис извлекает текст из PDF/DOC/DOCX.
5. Текст резюме и выбранная вакансия отправляются в OpenRouter.
6. LLM возвращает JSON с именем кандидата, вакансией, процентом соответствия, навыками, опытом и образованием.
7. Node.js сервис сохраняет JSON в `/data/data.json`.
8. Браузер вызывает Spring endpoint `/go/update-json`.
9. Spring Boot backend читает JSON из общего тома и сохраняет кандидата в PostgreSQL.
10. Запись появляется в истории анализа.

## Технологический стек

| Слой | Технологии |
| --- | --- |
| Backend | Java 21, Spring Boot 3.5.3, Spring MVC, Spring Security, Spring Data JPA, Hibernate |
| UI | Thymeleaf, HTML, CSS, JavaScript |
| Analyzer service | Node.js 20, Express 5, express-fileupload, pdf-parse, mammoth |
| AI integration | OpenRouter Chat Completions API, модель `deepseek/deepseek-chat-v3.1:free` |
| Database | PostgreSQL |
| Tests | JUnit, Spring Boot Test, Spring Security Test, H2 для тестового профиля |
| Build | Maven Wrapper, npm |
| Containers | Docker, Docker Compose |
| Orchestration | Kubernetes manifests |
| CI/CD | GitHub Actions, GitHub Container Registry |
| Cloud config | Render Blueprint |

## Структура проекта

```text
.
├── .github/workflows/main.yml          # CI/CD pipeline
├── k8s/                                # Kubernetes Deployment/Service manifests
├── src/
│   ├── main/
│   │   ├── java/ru/hackathon/springcourse/
│   │   │   ├── config/                 # Spring Security and password encoder config
│   │   │   ├── controllers/            # MVC controllers
│   │   │   ├── dao/                    # Persistence layer through EntityManager
│   │   │   ├── models/                 # JPA entities
│   │   │   ├── services/               # Business services
│   │   │   └── validator/              # Registration validation
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/                # UI styles
│   │       │   ├── js/                 # Node analyzer and browser scripts
│   │       │   └── json_results/       # Local JSON sample/result path
│   │       ├── templates/              # Thymeleaf pages
│   │       └── application.properties  # Spring application config
│   └── test/
│       ├── java/                       # Spring Boot tests
│       └── resources/                  # Test datasource config
├── docker-compose.yml
├── Dockerfile.backend
├── Dockerfile.frontend
├── pom.xml
├── render.yaml
├── mvnw
└── mvnw.cmd
```

## Требования

Для Docker-запуска:

- Docker Engine или Docker Desktop.
- Docker Compose v2.
- OpenRouter API key.

Для локальной разработки без полного Docker Compose:

- Java 21.
- Maven Wrapper из репозитория (`mvnw` / `mvnw.cmd`).
- Node.js 20+ и npm.
- PostgreSQL 14+ или контейнер PostgreSQL из `docker-compose.yml`.
- OpenRouter API key.

Порты по умолчанию:

| Сервис | Порт |
| --- | --- |
| Spring Boot backend / веб-интерфейс | `8080` |
| Node.js analyzer | `3001` |
| PostgreSQL | `5432` |

## Переменные окружения

| Переменная | Где используется | Обязательна | Описание |
| --- | --- | --- | --- |
| `OPENROUTER_API_KEY` | Node.js analyzer, Docker Compose, Render | Да | API-ключ OpenRouter. Должен передаваться через env/secret, не через git. |
| `SPRING_DATASOURCE_URL` | Spring Boot backend | Да для prod/docker | JDBC URL PostgreSQL, например `jdbc:postgresql://db:5432/hackbd`. |
| `SPRING_DATASOURCE_USERNAME` | Spring Boot backend | Да для prod/docker | Пользователь PostgreSQL. |
| `SPRING_DATASOURCE_PASSWORD` | Spring Boot backend | Да для prod/docker | Пароль PostgreSQL. |
| `PORT` | Node.js analyzer | Нет | Порт Node.js сервиса. По умолчанию `3001`. |

Пример `.env` в корне проекта для Docker Compose:

```env
OPENROUTER_API_KEY=sk-or-v1-your-real-key
```

Файлы `.env` уже исключены через `.gitignore`. Не коммитьте реальные ключи, пароли и токены.

## Быстрый запуск через Docker Compose

1. Создайте `.env` в корне проекта:

```env
OPENROUTER_API_KEY=sk-or-v1-your-real-key
```

2. Соберите и запустите сервисы:

```powershell
docker compose up -d --build
```

3. Проверьте контейнеры:

```powershell
docker compose ps
```

4. Откройте приложение:

```text
http://localhost:8080
```

5. Зарегистрируйте пользователя на `/register`, войдите через `/login`, затем перейдите в рабочий интерфейс `/go`.

6. Проверьте Node.js analyzer отдельно:

```text
http://localhost:3001
```

Остановка:

```powershell
docker compose down
```

Остановка с удалением данных PostgreSQL и общего JSON-тома:

```powershell
docker compose down -v
```

## Локальная разработка без Docker Compose

### 1. Запустить PostgreSQL

Можно поднять только базу из compose:

```powershell
docker compose up -d db
```

По умолчанию compose создает:

```text
database: hackbd
user: postgres
password: root
host: localhost
port: 5432
```

### 2. Запустить Node.js analyzer

```powershell
cd src\main\resources\static\js
npm install
$env:OPENROUTER_API_KEY="sk-or-v1-your-real-key"
node server.js
```

Analyzer будет доступен на:

```text
http://localhost:3001
```

### 3. Запустить Spring Boot backend

В отдельном терминале из корня проекта:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/hackbd"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="root"
.\mvnw.cmd spring-boot:run
```

Приложение будет доступно на:

```text
http://localhost:8080
```

Для Linux/macOS:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/hackbd"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="root"
./mvnw spring-boot:run
```

## Сборка и тесты

Backend tests:

```powershell
.\mvnw.cmd test
```

Backend package:

```powershell
.\mvnw.cmd clean package
```

Backend package без тестов:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Node.js dependencies:

```powershell
cd src\main\resources\static\js
npm install
```

Docker build:

```powershell
docker compose build
```

## HTTP-маршруты и API

### Web routes

| Method | Route | Назначение | Auth |
| --- | --- | --- | --- |
| `GET` | `/login` | Страница входа | Public |
| `POST` | `/process_login` | Spring Security login processing | Public |
| `GET` | `/register` | Страница регистрации | Public |
| `POST` | `/register` | Создание пользователя | Public |
| `POST` | `/logout` | Выход | Authenticated |
| `GET` | `/go` | Главная страница анализа и история | Authenticated |
| `GET` | `/go?job=analyst` | Фильтрация истории по вакансии | Authenticated |
| `DELETE` | `/go/{id}` | Удаление кандидата из истории | Authenticated |
| `GET` | `/go/update` | Чтение локального JSON-результата | Authenticated |
| `POST` | `/go/update` | Сохранение результата из `/data/data.json` после upload form | Authenticated |
| `POST` | `/go/update-json` | AJAX-сохранение результата из `/data/data.json` в БД | Authenticated |

### Analyzer API

#### `GET /`

Health-проверка Node.js сервиса.

Response:

```text
HR Scanner backend is running!
```

#### `POST /api/analyze`

Анализ резюме.

Content type:

```text
multipart/form-data
```

Form fields:

| Field | Type | Required | Описание |
| --- | --- | --- | --- |
| `resume` | file | Да | PDF, DOC или DOCX файл резюме. |
| `job` | string | Нет | Код вакансии. По умолчанию `analyst`. |

Ограничения:

- максимальный размер файла: `10 MB`;
- поддерживаемые форматы: PDF, DOC, DOCX;
- в LLM отправляются первые `2000` символов извлеченного текста.

Пример успешного JSON-ответа:

```json
{
  "name": "Иван Иванов",
  "job": "analyst",
  "persent": 82,
  "skills": [
    {
      "skill": "SQL"
    },
    {
      "skill": "Python"
    }
  ],
  "experience": "3 года в аналитике данных",
  "education": "Высшее техническое"
}
```

Важно: поле `persent` используется в коде именно с таким написанием. При изменении схемы нужно синхронно обновить Java model, JSON parsing, frontend rendering и prompt.

## Модель данных

### `Users`

| Поле | Тип | Описание |
| --- | --- | --- |
| `id` | integer | Primary key. |
| `email` | string | Логин пользователя. |
| `password` | string | BCrypt hash пароля. |
| `role` | string | Роль пользователя. По умолчанию `USER`. |

### `People`

| Поле | Тип | Описание |
| --- | --- | --- |
| `id` | integer | Primary key. |
| `name` | string | Имя кандидата. |
| `job` | string | Код вакансии. |
| `persent` | integer | Процент соответствия вакансии. |
| `skills` | `List<Skills>` | Навыки кандидата. |

### `Skills`

| Поле | Тип | Описание |
| --- | --- | --- |
| `id` | integer | Primary key. |
| `skill` | string | Название навыка. |
| `people` | relation | Many-to-one связь с кандидатом. |

DDL управляется Hibernate через:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Для production окружений рекомендуется заменить `ddl-auto=update` на миграции через Flyway или Liquibase.

## CI/CD

Pipeline находится в `.github/workflows/main.yml` и запускается при push в ветки:

- `main`
- `master`

Этапы:

1. Checkout репозитория.
2. Установка JDK 21.
3. Установка Node.js 20.
4. Запуск backend-тестов: `mvn test`.
5. Сборка backend: `mvn package -DskipTests`.
6. Установка frontend/analyzer зависимостей.
7. Опциональная frontend-сборка.
8. Login в GitHub Container Registry.
9. Сборка и публикация backend Docker image.
10. Сборка и публикация frontend Docker image.
11. Поднятие временного kind-кластера.
12. Загрузка образов в kind.
13. Применение `k8s/` манифестов.
14. Проверка Kubernetes pods/services.

Образы публикуются в GHCR с тегом commit SHA:

```text
ghcr.io/<owner>/<repo>/backend:<sha>
ghcr.io/<owner>/<repo>/frontend:<sha>
```

## Kubernetes

Манифесты находятся в `k8s/`:

```text
k8s/backend-deployment.yaml
k8s/backend-service.yaml
k8s/frontend-deployment.yaml
k8s/frontend-service.yaml
```

Базовое применение:

```powershell
kubectl apply -f k8s/
```

Обновление образов:

```powershell
kubectl set image deployment/backend backend=ghcr.io/<owner>/<repo>/backend:<sha>
kubectl set image deployment/frontend frontend=ghcr.io/<owner>/<repo>/frontend:<sha>
```

Проверка:

```powershell
kubectl get pods
kubectl get services
kubectl logs deployment/backend
kubectl logs deployment/frontend
```

Перед production-деплоем Kubernetes-манифесты нужно дополнить:

- PostgreSQL или внешним managed database подключением;
- `Secret` для `OPENROUTER_API_KEY`;
- `Secret` для database credentials;
- env-переменными `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`;
- shared volume для `/data/data.json` между backend и analyzer, либо заменить файловую интеграцию прямым HTTP/API или очередью;
- readiness/liveness probes;
- resource requests/limits;
- ingress/TLS;
- rolling update strategy;
- namespace и network policies.

## Render

Файл `render.yaml` описывает:

- PostgreSQL database `postgres-db`;
- backend web service из GHCR image;
- frontend/analyzer web service из GHCR image;
- секретную переменную `OPENROUTER_API_KEY`.

Перед деплоем через Render:

1. Убедитесь, что GHCR images доступны Render.
2. Укажите `OPENROUTER_API_KEY` в Render Dashboard.
3. Проверьте формат `SPRING_DATASOURCE_URL`: Spring ожидает JDBC URL вида `jdbc:postgresql://host:port/db`.
4. Настройте production credentials для PostgreSQL.
5. Проверьте, что backend и analyzer имеют согласованный способ обмена результатом анализа. В Docker Compose это общий volume `/data`; в cloud-окружении такой обмен должен быть явно спроектирован.

## Production checklist

Перед эксплуатацией в production необходимо закрыть следующие пункты:

- Вынести все секреты из `application.properties`, `.env`, Dockerfile'ов и YAML-манифестов в secret manager окружения.
- Ротировать OpenRouter API key, если он когда-либо попадал в git history.
- Заменить `spring.jpa.hibernate.ddl-auto=update` на управляемые миграции.
- Добавить профили конфигурации: `local`, `test`, `prod`.
- Отключить подробные SQL/TRACE логи в production.
- Ограничить CORS для Node.js analyzer конкретным origin'ом Spring-приложения.
- Убрать прямую привязку frontend fetch к `http://localhost:3001` для production; использовать env-based base URL или reverse proxy.
- Добавить централизованное логирование и request correlation id.
- Добавить валидацию размера, расширения и MIME-типа файла на backend boundary.
- Добавить антивирусную/контентную проверку загружаемых файлов, если система используется с внешними пользователями.
- Заменить файловый обмен через `/data/data.json` на более надежный контракт: REST callback, message queue, object storage или прямое сохранение результата через backend API.
- Добавить обработку конкурентных загрузок: текущий `/data/data.json` является общей точкой записи и может быть перезаписан параллельными запросами.
- Добавить indexes и constraints на таблицы `users`, `people`, `skills`.
- Сделать `email` уникальным на уровне БД.
- Добавить мониторинг, health checks и алерты.
- Настроить HTTPS, secure cookies, CSRF policy и security headers.
- Ввести rate limiting для `/api/analyze`.
- Добавить тесты для регистрации, авторизации, сохранения кандидатов и анализа ошибок Node.js сервиса.

## Troubleshooting

### `OPENROUTER_API_KEY не найден`

Проверьте, что переменная передана именно в Node.js сервис:

```powershell
docker compose logs frontend
```

Для локального запуска:

```powershell
$env:OPENROUTER_API_KEY="sk-or-v1-your-real-key"
node server.js
```

### Файл анализируется, но кандидат не появляется в истории

Проверьте:

- доступен ли Spring endpoint `/go/update-json`;
- есть ли общий Docker volume `json_data`;
- создался ли файл `/data/data.json` в контейнере analyzer;
- может ли backend прочитать `/data/data.json`;
- нет ли ошибок CSRF в browser devtools или Spring logs.

### Ошибка подключения к PostgreSQL

Проверьте env-переменные backend:

```powershell
docker compose logs backend
docker compose logs db
```

Для локального запуска проверьте JDBC URL:

```text
jdbc:postgresql://localhost:5432/hackbd
```

### `DeepSeek вернул невалидный JSON`

LLM может вернуть текст не по контракту. В коде уже есть очистка markdown-оберток, но для production рекомендуется:

- включить повтор запроса;
- валидировать JSON schema;
- логировать raw-response без персональных данных;
- возвращать пользователю понятную ошибку;
- при необходимости сменить модель или усилить prompt.

### `Failed to extract text`

Возможные причины:

- неподдерживаемый формат файла;
- битый PDF/DOC/DOCX;
- файл больше 10 MB;
- документ содержит сканы без текстового слоя;
- нет прав на запись во временную директорию `src/main/resources/static/js/temp`.

## Лицензия

Лицензия в репозитории не указана. Перед публичным распространением добавьте `LICENSE` и явно зафиксируйте условия использования.

