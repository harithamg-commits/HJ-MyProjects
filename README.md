# Books API

A small Spring Boot REST application with CRUD operations for books. It uses an in-memory H2 database, so data is reset when the application stops.

## Requirements

- Java 17 or newer
- Maven 3.6.3 or newer

## Run

```sh
mvn spring-boot:run
```

The API listens on `http://localhost:8080`.

The project uses an alternate Maven Central endpoint through `.mvn/maven.config` because the default endpoint may time out on this network. Maven applies this setting automatically when run from the project folder.

## Endpoints

| Method | Path | Action |
| --- | --- | --- |
| GET | `/api/books` | List books |
| GET | `/api/books/{id}` | Get one book |
| POST | `/api/books` | Create a book |
| PUT | `/api/books/{id}` | Replace a book |
| DELETE | `/api/books/{id}` | Delete a book |

Create a book:

```sh
curl -i -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin"}'
```

Both `title` and `author` are required and cannot be blank. Creating a book returns `201 Created` and a `Location` header. Missing IDs return `404 Not Found`; deleting a book returns `204 No Content`.

## Test

```sh
mvn test
```
