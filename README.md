# Project Shield Service

A Spring Boot backend service for the Project Shield portfolio management and capacity planning application.

## Prerequisites

- Java 21 or higher
- PostgreSQL 15 or higher
- Gradle 9.x (wrapper included)

## Quick Start

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE projectshield;
```

### 2. Configure Environment

Set environment variables or use defaults:

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

### 3. Run the Application

```bash
./gradlew bootRun
```

The service starts at `http://localhost:8080`

### 4. Access Swagger UI

Open your browser and navigate to:

```
http://localhost:8080/swagger-ui.html
```

Or access the OpenAPI specification directly:

```
http://localhost:8080/v3/api-docs
```

## Build

### Build the Project

```bash
./gradlew build
```

### Build Without Tests

```bash
./gradlew build -x test
```

### Clean Build

```bash
./gradlew clean build
```

## Testing

### Run All Tests

```bash
./gradlew test
```

### Run Tests with Coverage Report

```bash
./gradlew test jacocoTestReport
```

### Run Full Check (Tests + Coverage Verification)

```bash
./gradlew check
```

### Run a Specific Test Class

```bash
./gradlew test --tests "com.vicras.projectshield.service.CapacityServiceTest"
```

### Run Tests Matching a Pattern

```bash
./gradlew test --tests "*ServiceTest"
```

### Run Only Unit Tests

```bash
./gradlew test --tests "com.vicras.projectshield.service.*"
```

### Run Only Integration Tests (REST Assured)

```bash
./gradlew test --tests "com.vicras.projectshield.integration.*"
```

## Test Types

### Unit Tests (Mockito)

Located in `src/test/java/com/vicras/projectshield/service/` and `src/test/java/com/vicras/projectshield/controller/`

- Use **JUnit 5** and **Mockito** for mocking
- Test service layer business logic in isolation
- Use **@WebMvcTest** for controller slice tests with MockMvc

### Integration Tests (REST Assured)

Located in `src/test/java/com/vicras/projectshield/integration/`

- Use **REST Assured** for fluent API testing
- Run against full Spring Boot context with **@SpringBootTest**
- Test complete request/response cycles
- Use H2 in-memory database

Example REST Assured test:

```java
@Test
void createTeam_withValidData_returnsCreated() {
    given()
            .contentType(ContentType.JSON)
            .body("""
                {"name": "Platform Team", "description": "Core platform"}
                """)
    .when()
            .post("/teams")
    .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Platform Team"));
}
```

## Code Coverage Reports

Coverage reports are generated using JaCoCo.

### Generate Coverage Report

```bash
./gradlew test jacocoTestReport
```

### Report Locations

| Format | Location |
|--------|----------|
| HTML | `build/reports/jacoco/test/html/index.html` |
| XML | `build/reports/jacoco/test/jacocoTestReport.xml` |

### View HTML Report

```bash
open build/reports/jacoco/test/html/index.html
```

### Coverage Thresholds

The build enforces minimum coverage thresholds:

- **Overall**: 40% instruction coverage
- **Service classes**: 50% line coverage

Coverage verification runs automatically with `./gradlew check`.

### Excluded from Coverage

The following packages are excluded from coverage metrics:
- `entity/**` - JPA entities (getters/setters)
- `dto/**` - Data transfer objects
- `config/**` - Configuration classes
- `ProjectShieldApplication` - Main application class

## Test Reports

### Unit Test Report

After running tests, view the HTML report:

```bash
open build/reports/tests/test/index.html
```

### Test Report Location

| Format | Location |
|--------|----------|
| HTML | `build/reports/tests/test/index.html` |
| XML | `build/test-results/test/*.xml` |

## API Documentation (Swagger/OpenAPI)

Interactive API documentation is available via Swagger UI.

### Access Swagger UI

```
http://localhost:8080/swagger-ui.html
```

### OpenAPI Specification

```
http://localhost:8080/v3/api-docs        # JSON format
http://localhost:8080/v3/api-docs.yaml   # YAML format
```

### Features

- **Interactive testing**: Execute API calls directly from the browser
- **Request/Response schemas**: View detailed JSON schemas for all DTOs
- **Try it out**: Test endpoints with sample data
- **API grouping**: Endpoints organized by tags (Phases, Projects, Staff, Teams, Capacity)

### Download OpenAPI Spec

```bash
curl http://localhost:8080/v3/api-docs -o openapi.json
```

## API Endpoints

### Health Check

```bash
curl http://localhost:8080/health
```

### Phases

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/phases` | List all phases |
| GET | `/api/phases/{id}` | Get phase with projects |
| POST | `/api/phases` | Create phase |
| PUT | `/api/phases/{id}` | Update phase |
| DELETE | `/api/phases/{id}` | Delete phase |

### Projects

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/phases/{phaseId}/projects` | List projects in phase |
| GET | `/api/projects/{id}` | Get project by ID |
| POST | `/api/phases/{phaseId}/projects` | Create project |
| PUT | `/api/projects/{id}` | Update project |
| PUT | `/api/projects/{id}/stack-rank` | Update priority |
| DELETE | `/api/projects/{id}` | Delete project |

### Staff

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/staff` | List all staff |
| GET | `/api/staff/{id}` | Get staff member |
| POST | `/api/staff` | Create staff member |
| PUT | `/api/staff/{id}` | Update staff |
| DELETE | `/api/staff/{id}` | Delete staff |

### Teams

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/teams` | List teams with members |
| GET | `/api/teams/{id}` | Get team by ID |
| POST | `/api/teams` | Create team |
| PUT | `/api/teams/{id}` | Update team |
| DELETE | `/api/teams/{id}` | Delete team |

### Capacity (Read-only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/capacity/strategic?phaseId={id}` | Calculate capacity allocation |
| GET | `/api/capacity/gaps?phaseId={id}` | Calculate staffing gaps |

## Example API Calls

### Create a Team

```bash
curl -X POST http://localhost:8080/api/teams \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Platform Team",
    "description": "Core platform development"
  }'
```

### Create a Phase

```bash
curl -X POST http://localhost:8080/api/phases \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Q1 2025",
    "description": "First quarter planning",
    "startDate": "2025-01-01",
    "endDate": "2025-03-31",
    "type": "quarter"
  }'
```

### Create a Staff Member

```bash
curl -X POST http://localhost:8080/api/staff \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "role": "Backend Developer",
    "seniority": "senior",
    "hoursPerDay": 8,
    "domains": ["Claims Processing", "Payment & Billing"],
    "skills": ["Java", "Spring Boot", "PostgreSQL"]
  }'
```

### Create a Project

```bash
curl -X POST http://localhost:8080/api/phases/{phaseId}/projects \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Claims Modernization",
    "description": "Modernize claims processing system",
    "justification": "Reduce processing time by 50%",
    "sponsor": "Jane Smith",
    "estimatedBudget": 500000,
    "startDate": "2025-01-15",
    "endDate": "2025-03-15",
    "status": "strategic",
    "ratings": {
      "strategicAlignment": 5,
      "financialBenefit": 4,
      "riskProfile": 3,
      "feasibility": 4,
      "regulatoryCompliance": 5
    },
    "staffingNeeds": [
      {"role": "Backend Developer", "count": 2, "durationWeeks": 8},
      {"role": "Frontend Developer", "count": 1, "durationWeeks": 6}
    ]
  }'
```

## Project Structure

```
src/
├── main/
│   ├── java/com/vicras/projectshield/
│   │   ├── ProjectShieldApplication.java
│   │   ├── config/
│   │   │   └── WebConfig.java
│   │   ├── controller/
│   │   │   ├── CapacityController.java
│   │   │   ├── HomeController.java
│   │   │   ├── PhaseController.java
│   │   │   ├── StaffController.java
│   │   │   ├── ProjectController.java
│   │   │   └── TeamController.java
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   └── resources/
│       ├── application.properties
│       └── db/migration/
│           ├── V20260131_0000__init.sql
│           └── V20260131_0001__create_schema.sql
└── test/
    ├── java/com/vicras/projectshield/
    │   ├── controller/
    │   └── service/
    └── resources/
        └── application-test.properties
```

## Configuration

### Application Properties

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | Server port |
| `spring.datasource.url` | jdbc:postgresql://localhost:5432/projectshield | Database URL |
| `spring.datasource.username` | ${DB_USERNAME:postgres} | Database username |
| `spring.datasource.password` | ${DB_PASSWORD:postgres} | Database password |
| `spring.jpa.hibernate.ddl-auto` | validate | Hibernate DDL mode |
| `spring.flyway.enabled` | true | Enable Flyway migrations |

### Test Configuration

Tests use H2 in-memory database with PostgreSQL compatibility mode. See `src/test/resources/application-test.properties`.

## Database Migrations

Migrations are managed by Flyway and located in `src/main/resources/db/migration/`.

Migration naming convention: `V{YYYYMMDD}_{NNNN}__description.sql`

### Check Migration Status

```bash
./gradlew flywayInfo
```

## Troubleshooting

### Database Connection Issues

1. Verify PostgreSQL is running
2. Check database exists: `psql -l | grep projectshield`
3. Verify credentials in environment variables

### Test Failures

1. Run with debug output: `./gradlew test --debug`
2. Check test report: `open build/reports/tests/test/index.html`

### Coverage Verification Failures

If `./gradlew check` fails due to coverage:

1. View coverage report to identify gaps
2. Add tests for uncovered code
3. Or adjust thresholds in `build.gradle`

## License

Proprietary - All rights reserved.
