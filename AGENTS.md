# AGENTS.md — CardDemo Java Modernization

## Project Overview
This repository contains both the original COBOL mainframe CardDemo application and its Java (Spring Boot) modernization under `java-app/`.

## Java Application (`java-app/`)

### Tech Stack
- Java 17
- Spring Boot 3.2.5
- Spring Data JPA + H2 (in-memory)
- Maven

### Build & Run
```bash
cd java-app
mvn clean compile       # compile
mvn test                # run all tests (17 tests)
mvn spring-boot:run     # start app on port 8080
```

### Project Structure
```
java-app/
├── pom.xml
└── src/
    ├── main/java/com/carddemo/
    │   ├── CardDemoApplication.java    # Entry point
    │   ├── config/DataLoader.java      # Loads COBOL data files at startup
    │   ├── controller/                 # REST controllers
    │   ├── model/                      # JPA entities (from COBOL copybooks)
    │   ├── repository/                 # Spring Data JPA repositories
    │   └── service/                    # Business logic
    ├── main/resources/
    │   ├── application.properties
    │   └── data/                       # ASCII sample data files
    └── test/java/com/carddemo/        # Unit + integration tests
```

### API Endpoints
- `GET /api/accounts` — List all accounts
- `GET /api/accounts/{id}` — Get account by ID
- `GET /api/cards` — List all cards
- `GET /api/cards/{cardNumber}` — Get card by number
- `GET /api/customers` — List all customers
- `GET /api/customers/search?lastName=...` — Search customers
- `GET /api/transactions` — List all transactions
- `POST /api/auth/login` — Authenticate (body: `{"userId":"ADMIN001","password":"PASSWORD"}`)
- `GET /api/auth/users` — List all users

### COBOL-to-Java Entity Mapping
| COBOL Copybook | Java Entity         | DB Table           |
|---------------|--------------------|--------------------|
| CVACT01Y      | Account            | accounts           |
| CVACT02Y      | Card               | cards              |
| CVCUS01Y      | Customer           | customers          |
| CVTRA05Y      | Transaction        | transactions       |
| CVTRA03Y      | TransactionType    | transaction_types  |
| CVACT03Y      | CardCrossReference | card_xref          |
| CSUSR01Y      | UserSecurity       | user_security      |

### Conventions
- Entity fields map directly from COBOL copybook PIC clauses
- COBOL signed decimal (PIC S9(n)V99) → Java BigDecimal
- COBOL dates (PIC X(10), YYYY-MM-DD) → Java LocalDate
- Fixed-width data files are parsed by `DataLoader` at startup
