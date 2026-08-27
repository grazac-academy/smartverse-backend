# SmartVert Backend

SmartVert Backend is a high-performance Java Spring Boot service designed to calculate solar panel counts, battery capacities, and inverter rating configurations based on appliance running loads and backup requirements.

---

## Technical Stack

- **Java Version:** 21+ (configured with Java 25 in `pom.xml`)
- **Framework:** Spring Boot 3.x / 4.x (`spring-boot-starter-parent` v4.1.0 in `pom.xml`)
- **Database:** PostgreSQL
- **Migration Engine:** Flyway (auto-initializes schema and seeds initial appliance metadata on startup)
- **Build Tool:** Maven

---

## Core API Design & Response Wrapping

All REST controller responses conform to a unified wrapper structure (`ApiResponse<T>`):

```json
{
  "message": "Calculation completed successfully",
  "data": {
    ...
  }
}
```

If an error or validation exception occurs, the payload resolves as a failure:

```json
{
  "message": "Validation failed: items[0].quantity: must be greater than 0"
}
```

---

## Project Structure

```
smartverse-backend/
├── .env                             # Environment configuration (git-ignored)
├── .env.example                     # Sample environment configuration template
├── docker-compose.yml               # PostgreSQL service definition
├── pom.xml                          # Maven build dependencies
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/smartvert/smartvert/
│   │   │       ├── SmartVertApplication.java   # Application main entrypoint
│   │   │       ├── config/                      # Security, CORS, and OpenAPI configurations
│   │   │       ├── controller/                  # Public and administrative REST endpoints
│   │   │       ├── exception/                   # Global RestAdvice exception mapping
│   │   │       ├── mapper/                      # Entity/DTO MapStruct-like conversions
│   │   │       ├── model/                       # Data entities, DTO inputs & outputs
│   │   │       ├── repository/                  # JPA database adapters
│   │   │       └── service/                     # Sizing engine logic and catalog management
│   │   └── resources/
│   │       ├── db/migration/                    # Flyway database schemas and seeds
│   │       └── application.yml                  # Spring Boot properties
│   └── test/                                    # Unit tests and MockMvc integration suite
```

---

## Environment Configuration

Copy `.env.example` to `.env` in the root folder and configure your PostgreSQL database parameters:

```env
DB_USER=db_user
DB_PASSWORD=db_password
DB_NAME=db_name
DB_PORT=db_port
```

---

## Getting Started

### Prerequisites

- JDK 21+ (configured for Java 25)
- Maven 3.9+
- PostgreSQL or Docker (optional)

### Running Locally

1. **Configure Environment Variables:**
   ```bash
   cp .env.example .env
   ```

2. **Start Database (Optional Docker Setup):**
   ```bash
   docker-compose up -d
   ```

3. **Start Spring Boot Application:**
   ```bash
   mvn spring-boot:run
   ```

4. **Access Swagger UI documentation:**
   Open [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) in your browser.

---

## REST Endpoints Guide

### Public Catalog Endpoints

#### 1. Retrieve Appliance Categories
* **Endpoint:** `GET /api/v1/categories`
* **Curl Command:**
  ```bash
  curl -X GET http://localhost:8080/api/v1/categories
  ```

#### 2. Retrieve Catalog Appliances
* **Endpoint:** `GET /api/v1/appliances`
* **Query Parameters:** `categoryId` (Optional UUID filter)
* **Curl Command:**
  ```bash
  curl -X GET "http://localhost:8080/api/v1/appliances?categoryId=<CATEGORY_UUID>"
  ```

---

### Sizing Engine Calculation

#### Perform Load Calculation
* **Endpoint:** `POST /api/v1/calculations`
* **Headers:** `Content-Type: application/json`
* **Payload Structure:**
  ```json
  {
    "usageMode": "BACKUP",
    "backupHours": 6.00,
    "items": [
      {
        "applianceId": "73d44b67-ce7d-466a-883f-9762d71bbe30",
        "quantity": 2,
        "wattage": 150.00,
        "hoursPerDay": 8.00
      }
    ]
  }
  ```
* **Curl Command:**
  ```bash
  curl -X POST http://localhost:8080/api/v1/calculations \
    -H "Content-Type: application/json" \
    -d '{"usageMode":"BACKUP","backupHours":6.00,"items":[{"applianceId":"73d44b67-ce7d-466a-883f-9762d71bbe30","quantity":2,"wattage":150.00,"hoursPerDay":8.00}]}'
  ```

---

### Administrative Management (HTTP Basic Authentication Required)

All administrative actions under `/api/v1/admin/**` require basic authentication credentials. The default configured admin user is:
- **Username:** `admin`
- **Password:** Configured via application variables (or check startup logs for development credentials).

#### Create a New Catalog Appliance
* **Endpoint:** `POST /api/v1/admin/appliances`
* **Headers:** `Authorization: Basic <credentials>`
* **Curl Command:**
  ```bash
  curl -X POST http://localhost:8080/api/v1/admin/appliances \
    -u admin:<password> \
    -H "Content-Type: application/json" \
    -d '{
      "categoryId": "79d44b67-ce7d-466a-883f-9762d71bbe30",
      "code": "microwave",
      "name": "Microwave Oven",
      "defaultWattage": 1200.0,
      "minWattage": 800.0,
      "maxWattage": 1500.0,
      "defaultVoltage": 220,
      "surgeApplicable": true,
      "surgeMultiplier": 2.0,
      "heavyLoad": true,
      "active": true
    }'
  ```

---

## Verification and Testing

### Executing Tests Offline
Verify sizing computations and endpoint security policies using the integrated JUnit test suite:

```bash
mvn clean test -o
```
