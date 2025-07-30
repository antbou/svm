# Menu API

**Menu API** provides a RESTful interface for accessing weekly menus
from [sv-restaurant.ch](https://www.sv-restaurant.ch).
Since the site is dynamically rendered and backed by a non-public Firebase Firestore API, this project simulates a real
browser to extract menu data in a reliable way.

The extracted data can be used by external tools like [sv-menu](https://github.com/antbou/sv-menu) to display menus in a
terminal or custom frontends.

---

## Why this approach?

The `sv-restaurant.ch` platform is built using Angular and communicates with a backend via Firebase Firestore. However:

* The APIs are not public or officially documented.
* Menu data is streamed using Firestore’s `Listen` protocol over HTTP, encoded in binary (Protocol Buffers).
* Responses are not human-readable and require complex decoding logic and session-specific tokens.

Here’s a typical Firestore stream call:

```
https://firestore.googleapis.com/google.firestore.v1.Firestore/Listen/channel?...&SID=...&RID=...
```

Which returns raw chunks like:

```
9
[1,53,7]
```

Due to these constraints, direct scraping or HTTP-based API consumption is impractical. Instead, this project simulates
a full browser session to render the page and extract menus as a real user would.

---

## Features

* REST API for weekly menu retrieval
* Role-based access control (`ADMIN` role for administrative endpoints)
* Scheduled scraping via headless browser
* Swagger/OpenAPI integration for easy API exploration

---

## Technologies Used

* **Java 21**
* **Spring Boot**
* **Maven**
* **Swagger (OpenAPI)**

---

## Getting Started

### Prerequisites

* Java 21+
* Maven

### Installation

1. Clone the repository:

   ```bash
   git clone git@github.com:antbou/svm.git
   cd svm
   ```

2. Edit configuration (e.g., credentials, schedule) in:

   ```
   src/main/resources/application.properties
   ```

3. Build and run the application:

   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### API Documentation

Swagger UI is available at:

* `http://localhost:8080/swagger-ui.html`
  or
* `http://localhost:8080/swagger-ui/index.html`

---

## Running with Docker

This project includes a `Makefile` to simplify Docker-based operations.

### Prerequisites

* Docker
* Docker Compose
* GNU Make

### Available Commands

Run the following from the project root:

```bash
make build     # Build Docker images
make up        # Start all services defined in docker-compose.yml
make down      # Stop services
make logs      # View logs from all services
make clean     # Stop and remove containers, volumes, and orphans
make shell     # Open a shell inside the main service container
```

The Docker configuration is located in the `docker/` directory.

---

## Authentication

* All endpoints under `/v1/api/admin/**` require HTTP Basic Auth with a user assigned the `ADMIN` role.
* Other endpoints may be publicly accessible, depending on configuration.

---

## Project Structure

* `src/main/java/sv/menu/svm/` – Application source code
* `src/main/resources/` – Configuration files and templates
* `src/test/java/sv/menu/svm/` – Unit and integration tests

---

## License

This project is licensed under the [MIT License](LICENSE).