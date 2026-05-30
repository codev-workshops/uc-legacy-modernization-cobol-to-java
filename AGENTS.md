# AGENTS.md — CardDemo COBOL-to-Java Migration

## Project Overview

This repo contains the legacy COBOL CardDemo credit card system (`app/`) and its ongoing
Java 17 / Spring Boot 3.x migration (`carddemo-*` modules).

## Java Project Structure

```
carddemo-parent (pom.xml)        — Multi-module parent, JaCoCo 80%, surefire/failsafe
├── carddemo-common              — JPA entities, repositories, codecs, utilities
├── carddemo-batch               — Spring Batch jobs (CardDataPrinterJob, InterestCalculationJob, StatementGenerationJob, etc.)
├── carddemo-online              — Auth, user management, Spring Security + JWT
└── carddemo-migration           — CLI data loader: ASCII/EBCDIC → DB
```

## Build Commands

```bash
# Full build with coverage check
mvn clean verify -B

# Run only carddemo-common tests
mvn test -pl carddemo-common

# Run only carddemo-online tests
mvn test -pl carddemo-online

# Run test-harness (standalone, not part of parent reactor)
cd test-harness && mvn test

# Run only carddemo-batch tests
mvn verify -pl carddemo-batch -am

# Run migration against local ASCII data
mvn spring-boot:run -pl carddemo-migration -Dspring-boot.run.arguments=app/data/ASCII
```

## Profiles

- **dev** (default): H2 in-memory, `ddl-auto=create-drop`, Flyway disabled
- **prod**: PostgreSQL, `ddl-auto=validate`, Flyway enabled (`db/migration/`)

## Key Conventions

- Entities derived from COBOL copybooks in `app/cpy/` — do not rename fields without
  checking the copybook PIC clauses.
- Codecs (`ZonedDecimalCodec`, `PackedDecimalCodec`) in `carddemo-common` are ports of
  `test-harness/src/main/java/com/carddemo/harness/codec/`.
- `DateFormatUtil` replaces the `COBDATFT` assembler; `WaitUtil` replaces `MVSWAIT`.
- JaCoCo 80% minimum line coverage enforced on `carddemo-common`, `carddemo-batch`, and `carddemo-online`.
- Do NOT modify files under `app/` or `test-harness/` — those are the legacy COBOL source
  and its validation harness.

## Auth & Security (carddemo-online)

- JWT-based stateless authentication via `JwtUtil` / `JwtAuthenticationFilter`.
- Roles: `ADMIN` (userType `A`) and `USER` (userType `U`) — mapped from COBOL
  `CSUSR01Y.cpy` SEC-USR-TYPE and `COCOM01Y.cpy` CDEMO-USRTYP-ADMIN/USER.
- `POST /api/auth/login` is public; `/api/users/**` requires `ROLE_ADMIN`.
- JWT secret configured via `carddemo.jwt.secret` property (env var `CARDDEMO_JWT_SECRET`).
- COMMAREA session state (`CDEMO-USER-ID`, `CDEMO-USER-TYPE`) is replaced by JWT claims
  (`sub` = userId, `userType` claim).

## CI

GitHub Actions workflow (`.github/workflows/ci.yml`):
1. `mvn clean verify` — builds all modules + JaCoCo check
2. `cd test-harness && mvn test` — runs the 64-test validation harness
