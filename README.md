# Books API

A small Spring Boot REST application with CRUD operations for books. It uses an in-memory H2 database, so data is reset when the application stops.
Book changes also produce events through Apache Kafka.

## Requirements

- Java 17 or newer
- Maven 3.6.3 or newer
- A running Kafka broker at `localhost:9092` (or set `KAFKA_BOOTSTRAP_SERVERS`)

## Run

```sh
mvn spring-boot:run
```

The API listens on `http://localhost:8080`.

The project uses an alternate Maven Central endpoint through `.mvn/maven.config` because the default endpoint may time out on this network. Maven applies this setting automatically when run from the project folder.

## Kafka events

Start a Kafka broker before running the application. With Docker installed, you can use Apache Kafka's local quickstart command in a separate terminal:

```sh
docker run -p 9092:9092 apache/kafka:4.3.1
```

The application creates a one-partition `book-events` topic. Each successful POST, PUT, or DELETE sends a JSON event to that topic with the book ID as its key. The event has `action` (`CREATED`, `UPDATED`, or `DELETED`), `id`, `title`, and `author` fields. The `@KafkaListener` in `BookEventListener` consumes these events and writes them to the application log.

For example, after creating a book, look for a log line like:

```text
Received book event: BookEvent[action=CREATED, id=1, title=Clean Code, author=Robert C. Martin]
```

Kafka sends are asynchronous. This sample logs delivery errors; it does not guarantee that a database change and its event are committed together.
Batch updates start publishing events only after the database transaction commits.

## Endpoints

| Method | Path | Action |
| --- | --- | --- |
| GET | `/api/books` | List books |
| GET | `/api/books/{id}` | Get one book |
| POST | `/api/books` | Create a book |
| PUT | `/api/books/{id}` | Replace a book |
| PUT | `/api/books/batch` | Update several books in one transaction |
| DELETE | `/api/books/{id}` | Delete a book |

Create a book:

```sh
curl -i -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin"}'
```

Both `title` and `author` are required and cannot be blank. Creating a book returns `201 Created` and a `Location` header. Missing IDs return `404 Not Found`; deleting a book returns `204 No Content`.

To see an all-or-nothing batch, first create two books and use their IDs in this request:

```sh
curl -i -X PUT http://localhost:8080/api/books/batch \
  -H "Content-Type: application/json" \
  -d '{"updates":[{"id":1,"title":"Revised One","author":"Author One"},{"id":2,"title":"Revised Two","author":"Author Two"}]}'
```

`BookBatchService.updateBatch` uses `@Transactional`. If any ID is missing, the API returns `404` and rolls back updates to earlier books in the same request. An empty batch or invalid book fields return `400` before any update begins.

## Test

```sh
mvn test
```

The tests mock the event publisher and do not require a Kafka broker.
