# ProjectShield Service

## Commands
- Build: `./gradlew build`
- Test: `./gradlew test`
- Run (local): `./gradlew bootRun --args='--spring.profiles.active=local'`
- Run (prod): `./gradlew bootRun --args='--spring.profiles.active=prod'`
- Clean: `./gradlew clean`
- Migrate DB: `./gradlew flywayMigrate`
- Migration info: `./gradlew flywayInfo`

## Configuration
- `application.properties` - Base/shared configuration
- `application-local.properties` - Local development (debug logging, local DB)
- `application-prod.properties` - Production (env vars for DB, connection pooling)

## Architecture
- Java 21, Gradle (Groovy), Spring Boot
- Database: PostgreSQL with Hibernate ORM
- Auth: Clerk (JWT via Spring Security OAuth2 Resource Server)
- Observability: OpenTelemetry, Sentry

## Environment Variables
- `PS_DB_USERNAME` - Database username
- `PS_DB_PASSWORD` - Database password
- `PS_DB_URL` - Database URL (prod only)
- `CLERK_ISSUER_URI` - Clerk issuer URL (e.g., https://your-app.clerk.accounts.dev)

## Structure
- `src/main/java/` - Application code
- `src/main/resources/` - Configuration files
- `src/main/resources/db/migration/` - Flyway migrations (V{YYYYMMDD}_{HHMM}__description.sql)
- `src/test/java/` - Tests

## Conventions
- Use constructor injection (not @Autowired on fields)
- DTOs in `dto/` package, entities in `entity/`
- Controllers return ResponseEntity with appropriate status codes