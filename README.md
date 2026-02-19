# 💰 Penny — Mini Expense Manager

A full-stack expense tracking application built with **Java Spring Boot** (microservice) + **React TypeScript** (frontend).

---

## Tech Stack

| Layer      | Technology                                         |
|------------|----------------------------------------------------|
| Backend    | Java 21, Spring Boot 3.2, Spring Data JPA, OpenCSV |
| Database   | PostgreSQL (prod) · H2 in-memory (dev)             |
| Frontend   | React 18, TypeScript, Vite, Tailwind CSS, Recharts |
| API Docs   | Springdoc OpenAPI (Swagger UI at `/swagger-ui.html`) |

---

## Project Structure

```
penny-expense/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/penny/expense/
│       src/main/java/com/penny/expense/
│       ├── ExpenseApplication.java
│       ├── controller/    ExpenseController.java
│       ├── service/
│       │   ├── ExpenseService.java                 ← Thin orchestrator (SOLID core)
│       │   ├── CategorizationService.java          ← @Configuration: registers strategy bean
│       │   ├── AnomalyDetectionService.java        ← @Configuration: registers strategy bean
│       │   └── strategy/                           ← OCP/DIP: all interfaces + impls
│       │       ├── CategorizationStrategy.java              ← Interface (OCP/ISP)
│       │       ├── KeywordCategorizationStrategy.java       ← Impl (OCP)
│       │       ├── AnomalyDetectionStrategy.java            ← Interface (OCP/ISP)
│       │       ├── MeanMultiplierAnomalyStrategy.java       ← Impl (OCP)
│       │       ├── ExpenseFileParser.java                   ← Interface (SRP/OCP)
│       │       ├── CsvExpenseParser.java                    ← Impl: CSV parsing (SRP)
│       │       ├── DashboardAssembler.java                  ← Interface (SRP)
│       │       └── DefaultDashboardAssembler.java           ← Impl: dashboard assembly (SRP)
│       ├── mapper/
│       │   └── ExpenseMapper.java                  ← Entity ↔ DTO conversion (SRP)
│       ├── exception/
│       │   ├── ExpenseNotFoundException.java       ← Domain exception → 404
│       │   └── InvalidExpenseException.java        ← Domain exception → 400
│       ├── repository/    ExpenseRepository.java
│       ├── model/         Expense.java
│       ├── dto/           ExpenseRequest / ExpenseResponse / DashboardResponse / CsvUploadResult
│       └── config/        CorsConfig.java · GlobalExceptionHandler.java
│
└── frontend/
    ├── src/
    │   ├── api/        client.ts      (Axios typed API client)
    │   ├── hooks/      useData.ts     (useFetch, useExpenses, useDashboard)
    │   ├── pages/      Dashboard.tsx · ExpenseList.tsx · AddExpense.tsx · UploadCsv.tsx
    │   ├── components/ ui.tsx         (CategoryPill, StatCard, Button, Card …)
    │   ├── types/      index.ts
    │   └── App.tsx
    └── vite.config.ts
```

---

## Setup Instructions

### Prerequisites
- Java 21+
- Maven 3.8+
- Node 18+ / npm 9+
- PostgreSQL 14+ *(only for prod profile — dev uses H2)*

---

### 1 · Backend

```bash
cd backend

# Dev mode (H2 in-memory — zero config, auto-creates schema)
mvn spring-boot:run

# ── or ──

# Prod mode (PostgreSQL)
# Create DB first:
#   psql -c "CREATE DATABASE pennydb; CREATE USER penny WITH PASSWORD 'penny'; GRANT ALL ON DATABASE pennydb TO penny;"

DB_USERNAME=penny DB_PASSWORD=penny \
  mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

| URL | Description |
|-----|-------------|
| `http://localhost:8080/api/expenses` | REST API |
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/h2-console` | H2 Console (dev only) |

---

### 2 · Frontend

```bash
cd frontend
npm install
npm run dev         # http://localhost:5173
```

Vite proxies `/api → http://localhost:8080` so no CORS issues in dev.

---

### 3 · Build for Production

```bash
# Backend fat JAR
cd backend && mvn clean package -DskipTests
java -jar target/expense-service-1.0.0.jar --spring.profiles.active=prod

# Frontend static build
cd frontend && npm run build   # outputs to dist/
```

---

## REST API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/expenses` | List all expenses |
| `GET` | `/api/expenses/{id}` | Get expense by ID |
| `POST` | `/api/expenses` | Add expense (auto-categorized) |
| `DELETE` | `/api/expenses/{id}` | Delete + recalculate anomalies |
| `POST` | `/api/expenses/upload-csv` | Bulk import from CSV |
| `GET` | `/api/expenses/dashboard` | Dashboard summary |
| `GET` | `/api/expenses/categories` | Vendor→category rules map |

### Sample `POST /api/expenses` body
```json
{
  "date": "2024-01-15",
  "amount": 450.00,
  "vendorName": "Swiggy",
  "description": "Team lunch"
}
```

### CSV Format
```
date,amount,vendor_name,description
2024-01-15,450.00,Swiggy,Lunch order
2024-01-16,75000.00,Amazon,Laptop
```
The `description` column is optional. Flexible header aliases accepted (`vendor`, `merchant`, `desc`, `notes`).

---

## Design Note

### Rule-Based Categorization
`CategorizationService` maintains a `LinkedHashMap<String, String>` of ~65 keyword → category entries. Order matters: `"uber eats"` appears before `"uber"` so the more-specific match wins. Categorization is a simple O(k) substring scan (`vendor.toLowerCase().contains(keyword)`). The same rules are mirrored in the React frontend (TypeScript array) for instant preview before the API round-trip.

**Trade-off:** Keyword matching is brittle for ambiguous vendors. In production this would be a user-editable rules table in the DB, or an ML classifier trained on transaction history.

### Anomaly Detection
`AnomalyDetectionService.recalculateForCategory(category)` is called after every write (insert, delete). It:
1. Loads all expenses for the affected category
2. Computes `mean = SUM(amount) / COUNT`
3. Marks `is_anomaly = amount > mean * 3`
4. Bulk-updates all rows via `@Modifying` JPQL

The flag is **persisted** on the entity (not a view-time computation) so the dashboard query is a simple `WHERE is_anomaly = true`. The tradeoff is write amplification: a single insert triggers a category rescan. At scale, a running-average maintained in a `category_stats` table would reduce this to O(1).

### Data Model
Single `expenses` table — intentionally flat/denormalized. `category` is stored as a `VARCHAR` (not a FK) to avoid joins on the hot read path and to allow rule changes without cascading updates. Three DB indexes: `category` (anomaly recalc queries), `date` (time-range filters), `is_anomaly` (dashboard).

### DB Dual-Profile Strategy
`application-dev.properties` wires H2 in-memory so the app starts with zero setup. `application-prod.properties` wires PostgreSQL with env-var credentials. The only schema difference is the date formatting function in JPQL: H2 uses `FORMATDATETIME`, PostgreSQL uses `TO_CHAR` — `ExpenseService.getDashboard()` tries H2 first and falls back to the PG query.

### Assumptions
1. Single currency (INR ₹)
2. No authentication / multi-tenancy
3. Anomaly threshold (3×) is hardcoded — configurable via an `@Value` property in production
4. CSV date parsing supports `yyyy-MM-dd`, `dd/MM/yyyy`, `MM/dd/yyyy`, `dd-MM-yyyy`
5. `spring.jpa.hibernate.ddl-auto=update` handles schema creation — no Flyway/Liquibase migration (acceptable for an assignment, not production)
