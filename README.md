# Zorvyn — Finance Dashboard Backend

A Spring Boot 3.1 backend for a role-based finance dashboard system.  
Built with **Java 17**, **MongoDB Atlas**, **Spring Security + JWT**, and **Bean Validation**.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Roles & Access Control](#roles--access-control)
- [API Reference](#api-reference)
- [Setup & Run](#setup--run)
- [Default Users](#default-users)
- [Design Decisions & Tradeoffs](#design-decisions--tradeoffs)
- [Validation & Error Handling](#validation--error-handling)
- [Data Model](#data-model)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                     Client (Postman / Frontend)         │
└───────────────────────────┬─────────────────────────────┘
                            │ HTTP + JWT Bearer Token
┌───────────────────────────▼─────────────────────────────┐
│                  JwtAuthenticationFilter                 │
│          (intercepts every request, validates JWT)       │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│                     Controllers                         │
│   AuthController · UserController                       │
│   FinancialRecordController · DashboardController       │
└───────────────────────────┬─────────────────────────────┘
                            │ @PreAuthorize (RBAC)
┌───────────────────────────▼─────────────────────────────┐
│                      Services                           │
│   UserService · FinancialRecordService                  │
│   DashboardService                                      │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│                    Repositories                         │
│   UserRepository · FinancialRecordRepository            │
│              (Spring Data MongoDB)                      │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│                  MongoDB Atlas                          │
│               Database: zorvyn_finance                  │
└─────────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Layer          | Technology                          |
|----------------|-------------------------------------|
| Language       | Java 17                             |
| Framework      | Spring Boot 3.1.5                   |
| Security       | Spring Security + JWT (jjwt 0.11.5) |
| Database       | MongoDB Atlas (Spring Data MongoDB) |
| Validation     | Jakarta Bean Validation             |
| Build          | Maven                               |
| Code Reduction | Lombok                              |

---

## Project Structure

```
src/main/java/com/aditya/zorvyn/
├── ZorvynApplication.java          # Entry point
├── config/
│   ├── DataSeeder.java             # Seeds default users on startup
│   └── GlobalExceptionHandler.java # Centralized error handling
├── controller/
│   ├── AuthController.java         # Login & registration
│   ├── UserController.java         # User CRUD (Admin only)
│   ├── FinancialRecordController.java  # Record CRUD
│   └── DashboardController.java    # Analytics endpoints
├── dto/
│   └── DashboardSummaryResponse.java   # Response DTO
├── model/
│   ├── User.java                   # User document
│   ├── Role.java                   # VIEWER | ANALYST | ADMIN
│   └── FinancialRecord.java        # Financial entry document
├── repository/
│   ├── UserRepository.java         # User data access
│   └── FinancialRecordRepository.java  # Record data access
├── security/
│   ├── SecurityConfig.java         # HTTP security + filter chain
│   ├── JwtUtil.java                # Token generation & validation
│   ├── JwtAuthenticationFilter.java    # Per-request JWT filter
│   └── CustomUserDetailsService.java   # Loads users for auth
└── service/
    ├── UserService.java            # User business logic
    ├── FinancialRecordService.java  # Record business logic
    └── DashboardService.java       # Aggregation & analytics
```

---

## Roles & Access Control

| Action                        | VIEWER | ANALYST | ADMIN |
|-------------------------------|:------:|:-------:|:-----:|
| View dashboard summary        |   ✅   |   ✅    |  ✅   |
| View category totals          |   ✅   |   ✅    |  ✅   |
| View recent activity          |   ✅   |   ✅    |  ✅   |
| View monthly trends           |   ✅   |   ✅    |  ✅   |
| List financial records        |   ❌   |   ✅    |  ✅   |
| View single record            |   ❌   |   ✅    |  ✅   |
| Create financial record       |   ❌   |   ❌    |  ✅   |
| Update financial record       |   ❌   |   ❌    |  ✅   |
| Delete financial record       |   ❌   |   ❌    |  ✅   |
| Manage users                  |   ❌   |   ❌    |  ✅   |

Access control is enforced at two levels:
1. **URL-level** — via `SecurityConfig` `requestMatchers()` rules
2. **Method-level** — via `@PreAuthorize` annotations on controller methods

---

## API Reference

### Authentication (Public)

| Method | Endpoint             | Description                                  |
|--------|----------------------|----------------------------------------------|
| POST   | `/api/auth/login`    | Login with username/password, returns JWT     |
| POST   | `/api/auth/register` | Self-register as VIEWER (public registration) |

**Login Request:**
```json
{ "username": "admin", "password": "admin123" }
```
**Login Response:**
```json
{ "token": "eyJhb...", "role": "ADMIN", "username": "admin" }
```

### User Management (Admin Only)

| Method | Endpoint                      | Description         |
|--------|-------------------------------|---------------------|
| POST   | `/api/users`                  | Create a user       |
| GET    | `/api/users`                  | List all users      |
| PUT    | `/api/users/{id}`             | Update user details |
| DELETE | `/api/users/{id}`             | Delete a user       |
| POST   | `/api/users/{id}/deactivate`  | Deactivate a user   |

### Financial Records (Analyst: Read / Admin: Full)

| Method | Endpoint              | Description                          |
|--------|-----------------------|--------------------------------------|
| POST   | `/api/records`        | Create a record (Admin)              |
| GET    | `/api/records`        | List records with optional filters   |
| GET    | `/api/records/{id}`   | Get a single record                  |
| PUT    | `/api/records/{id}`   | Update a record (Admin)              |
| DELETE | `/api/records/{id}`   | Soft-delete a record (Admin)         |

**Query Parameters for GET `/api/records`:**
- `type` — Filter by `INCOME` or `EXPENSE`
- `category` — Filter by category name
- `startDate` / `endDate` — Filter by date range (ISO format: `2024-01-01`)

### Dashboard (All Authenticated Roles)

| Method | Endpoint                 | Description                            |
|--------|--------------------------|----------------------------------------|
| GET    | `/api/dashboard/summary` | Full summary (income, expenses, etc.)  |
| GET    | `/api/dashboard/categories` | Category-wise totals                |
| GET    | `/api/dashboard/recent?limit=10` | Recent transactions             |
| GET    | `/api/dashboard/trends?months=6` | Monthly aggregated trends       |

---

## Setup & Run

### Prerequisites

- Java 17+
- Maven 3.8+ (or use the included `mvnw` wrapper)
- A MongoDB Atlas cluster (or local MongoDB instance)

### Steps

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd zorvyn
   ```

2. **Configure the database**  
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.data.mongodb.uri=mongodb+srv://<username>:<password>@<cluster-url>
   spring.data.mongodb.database=zorvyn_finance
   ```

3. **Configure JWT secret**  
   Update the JWT secret in `application.properties` (use a strong, random 256-bit key):
   ```properties
   app.jwt.secret=<your-256-bit-hex-secret>
   app.jwt.expiration=86400000   # 24 hours in milliseconds
   ```

4. **Build and run**
   ```bash
   ./mvnw spring-boot:run
   ```
   The server starts on `http://localhost:8080`.

5. **Test with Postman/curl**
   ```bash
   # Login as admin
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'

   # Use returned token for authenticated requests
   curl http://localhost:8080/api/dashboard/summary \
     -H "Authorization: Bearer <token>"
   ```

---

## Default Users

On first startup (when the database is empty), the `DataSeeder` automatically creates three users:

| Username  | Password    | Role    |
|-----------|-------------|---------|
| admin     | admin123    | ADMIN   |
| analyst   | analyst123  | ANALYST |
| viewer    | viewer123   | VIEWER  |

These are for **testing/demo purposes only**. Remove or change them before any real deployment.

---

## Design Decisions & Tradeoffs

### Why MongoDB?
- Flexible schema suits financial records where categories and metadata can evolve
- MongoDB Atlas provides a free-tier cloud database — zero infrastructure setup
- Spring Data MongoDB offers a clean repository abstraction

### Why JWT (stateless)?
- No server-side session storage needed — scales horizontally
- Each request is self-contained after authentication
- Token expiry (24h) limits damage from compromised tokens

### Soft Delete
- Records are never physically deleted; the `deleted` flag is set to `true`
- This preserves audit trails and allows recovery
- All read queries filter by `deleted=false`

### Method-Level vs Class-Level Security
- Used **method-level** `@PreAuthorize` on `FinancialRecordController` for granular control (Analysts can read, only Admins can write)
- Used **class-level** `@PreAuthorize` on `UserController` (Admin-only for all user management)
- Used **class-level** `@PreAuthorize` on `DashboardController` (all roles can view)

### Password Security
- Passwords are hashed with BCrypt (via `BCryptPasswordEncoder`)
- Raw passwords are never stored or returned in API responses

### Centralized Error Handling
- `GlobalExceptionHandler` using `@RestControllerAdvice` provides consistent JSON error responses
- Handles validation errors, access denied, and generic exceptions
- Returns structured error objects with appropriate HTTP status codes

---

## Validation & Error Handling

All request DTOs use Jakarta Bean Validation annotations:

| Annotation      | Usage                           |
|-----------------|---------------------------------|
| `@NotBlank`     | Required string fields          |
| `@NotNull`      | Required non-string fields      |
| `@Email`        | Email format validation         |
| `@Size`         | Length constraints               |
| `@DecimalMin`   | Minimum financial amounts       |

**Example validation error response (400):**
```json
{
  "error": "Validation failed",
  "fieldErrors": {
    "username": "Username is required",
    "amount": "Amount must be greater than 0"
  }
}
```

**Access denied response (403):**
```json
{ "error": "Access denied" }
```

**Invalid login response (401):**
```json
{ "error": "Invalid username or password" }
```

---

## Data Model

### User
```
{
  "id": "string (auto-generated)",
  "username": "string (unique, 3-50 chars)",
  "email": "string (unique, valid email)",
  "password": "string (BCrypt hashed)",
  "role": "VIEWER | ANALYST | ADMIN",
  "active": true,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

### FinancialRecord
```
{
  "id": "string (auto-generated)",
  "amount": 1500.00,
  "type": "INCOME | EXPENSE",
  "category": "string (e.g. Salary, Rent, Groceries)",
  "date": "2024-01-15",
  "notes": "string (max 500 chars, optional)",
  "userId": "string (reference to user)",
  "deleted": false
}
```

### Role (Enum)
```
VIEWER | ANALYST | ADMIN
```