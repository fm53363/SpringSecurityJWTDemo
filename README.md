# Spring Boot 3 + Spring Security JWT Demo

A demo project showcasing stateless JWT authentication, role-based authorization (RBAC), and method-level access control
in **Spring Boot 3** and **Spring Security 6**.


---

## What's Inside

- **Stateless JWT Auth**: Custom `JwtAuthFilter` intercepts incoming HTTP requests, validates HMAC-SHA256 tokens (via
  JJWT), extracts the user identity, and registers the authentication in `SecurityContextHolder`.
- **Method-Level Security (`@PreAuthorize`)**:
    - Role-based restrictions (e.g. `hasRole('ADMIN')` for managing users).
    - Resource ownership checks (e.g. `#id == principal.id` so regular users can only read or delete their own
      profiles).
- **Error & Exception Handling**:
    - `AuthExceptionHandler` handles filter-level security errors: `401 Unauthorized` (AuthenticationEntryPoint) and
      `403 Forbidden` (AccessDeniedHandler) returning standardized JSON bodies.
    - `GlobalExceptionHandler` (`@RestControllerAdvice`) handles validation failures, duplicate username conflicts,
      missing entities, and SpEL authorization denials (`AuthorizationDeniedException`).
- **MDC Request Tracing**: Every request gets a unique `requestId` in the logging MDC for traceability across logs.
- **Data Transfer & Validation**: MapStruct maps between JPA entities and request/response records. Jakarta Bean
  Validation validates incoming payloads.
- **Database & Tooling**: File-based H2 database (`./data/devdb`), embedded H2 console, and interactive Swagger UI /
  OpenAPI 3.0 documentation.

---

## Tech Stack

- **Java 17**
- **Spring Boot 3.5.16** (Spring Web, Spring Security, Spring Data JPA, Validation)
- **JJWT 0.11.5** (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- **H2 Database** (File-based storage in `./data/devdb`)
- **MapStruct 1.5.0.Final** & **Lombok**
- **SpringDoc OpenAPI 2.8.17** (Swagger UI)
- **Testing**: JUnit 5 (Jupiter), Spring Boot Test, MockMvc, AssertJ, Spring Security Test

---

## How Security Works Here

### Request Flow

1. The client sends an HTTP request with an `Authorization: Bearer <token>` header.
2. `JwtAuthFilter` extracts the token, checks the signature and expiration against `JwtService`, loads the `UserDetails`
   from `CustomUserDetailsService`, and populates the `SecurityContext`.
3. If no token is provided or the token is invalid:
    - Public endpoints (`/api/auth/**`, `/swagger-ui/**`, `/h2-console/**`) proceed normally.
    - Protected endpoints trigger `AuthExceptionHandler` and return `401 Unauthorized`.
4. For protected endpoints, Spring Security evaluates URL rules first, followed by `@PreAuthorize` SpEL expressions on
   the controller methods. If unauthorized, `403 Forbidden` is returned.

### Roles and Permissions

- `ROLE_USER`: Can access `/api/demo` and view or delete their own profile (`/api/users/{id}` where `{id}` matches the
  logged-in user's ID).
- `ROLE_ADMIN`: Full access to all endpoints, including listing all users, creating users with assigned roles, editing
  user profiles, and deleting any user.

---

## API Endpoints

### Authentication (`/api/auth`)

- `POST /api/auth/register` - Register a new user (defaults to `ROLE_USER`). Returns a JWT.
- `POST /api/auth/login` - Authenticate with username and password. Returns a JWT.

### Demo (`/api/demo`)

- `GET /api/demo` - Simple greeting endpoint protected by authentication (`ROLE_USER` or `ROLE_ADMIN`).

### Users (`/api/users`)

- `GET /api/users` - List all users (`ADMIN` only).
- `GET /api/users/{id}` - Get user by ID (`ADMIN` or owner `#id == principal.id`).
- `POST /api/users` - Create a user with a specific role (`ADMIN` only).
- `PATCH /api/users/{id}` - Update a user's username, password, or role (`ADMIN` only).
- `DELETE /api/users/{id}` - Delete a user (`ADMIN` or owner `#id == principal.id`).

### Documentation & Database Console

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI Docs: `http://localhost:8080/v3/api-docs`
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/devdb`)

---

## Integration Tests & Testing Strategy

The project relies on a comprehensive integration test suite located in
`src/test/java/com/fer/hr/springsecurityjwtdemo/controller/UserControllerIntegrationTest.java`.

All tests run against a full Spring application context with MockMvc, verifying the entire request lifecycle—from HTTP
headers and filter chains down to database state and transaction rollbacks.

### Test Architecture & Setup

- **`@SpringBootTest` & `@AutoConfigureMockMvc`**: Boots the application context and injects `MockMvc` to perform real
  HTTP requests against the endpoints.
- **`@Transactional`**: Rolls back database mutations after every single test method to guarantee strict test isolation
  without manual cleanup.
- **`@ActiveProfiles("test")`**: Activates test configuration.
- **`TestUserFactory`**: A dedicated helper utility in `testUtil` that seeds test users directly into `UserRepository`
  with properly BCrypt-hashed passwords. It also includes bulk creation (`createUsers(count, role)`) for testing list
  endpoints.
- **Dynamic JWT Generation**: Instead of hardcoding mock strings or mocking security filters, tests generate real signed
  JWT tokens for created users using `jwtService.generateToken(user)`. This ensures that `JwtAuthFilter` and
  `CustomUserDetailsService` are tested end-to-end.

### Test Suites Covered

The tests are organized into nested classes using JUnit 5 `@Nested` and `@DisplayName` annotations, covering happy
paths, edge cases, and security boundaries:

#### 1. `GET /api/users/{id}`

- **Admin Access (`200 OK`)**: Verifies an admin user can fetch another user's details.
- **Self Access (`200 OK`)**: Verifies a regular user can fetch their own details (`#id == principal.id`).
- **Entity Not Found (`404 Not Found`)**: Verifies admin receives a 404 when querying a non-existent ID.
- **Access Denied (`403 Forbidden`)**: Verifies a regular user is blocked from viewing another user's profile.
- **Unauthenticated (`401 Unauthorized`)**: Verifies requests without a token are rejected immediately.

#### 2. `GET /api/users`

- **Admin Fetch All (`200 OK`)**: Seeds multiple users via `TestUserFactory` and verifies that the admin receives the
  complete list with matching size and DB record count.
- **Forbidden for Regular User (`403 Forbidden`)**: Ensures regular users cannot list all users.
- **Unauthenticated (`401 Unauthorized`)**: Ensures unauthenticated requests are blocked.

#### 3. `POST /api/users`

- **Admin Creates User (`201 Created`)**: Confirms an admin can create a user, validates the returned payload and
  `Location` header, and verifies the user is saved in the database.
- **Username Conflict (`409 Conflict`)**: Ensures attempting to create a user with an already taken username returns
  409.
- **Forbidden for Regular User (`403 Forbidden`)**: Asserts that regular users cannot create new accounts via this admin
  endpoint and verifies no entity was saved.
- **Unauthenticated (`401 Unauthorized`)**: Verifies unauthenticated POST requests are rejected.

#### 4. `PATCH /api/users/{id}`

- **Admin Updates User (`200 OK`)**: Verifies updating a user's username and role persists correctly in the database and
  clears the old username.
- **Username Conflict on Update (`409 Conflict`)**: Verifies an update fails when trying to rename a user to a username
  that already exists, ensuring database integrity.
- **Forbidden for Regular User (`403 Forbidden`)**: Confirms non-admin users cannot update accounts and database records
  remain untouched.
- **Unauthenticated (`401 Unauthorized`)**: Ensures unauthenticated PATCH requests are rejected.

#### 5. `DELETE /api/users/{id}`

- **Admin Deletes User (`204 No Content`)**: Verifies an admin can delete any user and checks the database is updated.
- **Self Deletion (`204 No Content`)**: Verifies a user can delete their own account.
- **Non-Existent User (`404 Not Found`)**: Verifies deleting an invalid ID returns 404.
- **Forbidden Deletion of Other User (`403 Forbidden`)**: Confirms a regular user cannot delete someone else's account.
- **Unauthenticated (`401 Unauthorized`)**: Ensures unauthenticated DELETE requests are rejected.

---

## Getting Started

### Prerequisites

- **JDK 17+**
- Maven wrapper is included (`./mvnw` or `.\mvnw.cmd`)

### 1. Set the JWT Secret

The application requires a 256-bit secret string for HMAC-SHA256 signing. Export it in your terminal:

**Linux / macOS:**

```bash
export JWT_SECRET="my-super-secret-key-that-is-at-least-256-bits-long-for-jwt-signing"
```

**Windows (PowerShell):**

```powershell
$env:JWT_SECRET="my-super-secret-key-that-is-at-least-256-bits-long-for-jwt-signing"
```

**Windows (CMD):**

```cmd
set JWT_SECRET=my-super-secret-key-that-is-at-least-256-bits-long-for-jwt-signing
```

### 2. Run the Tests

To execute all 21 integration tests:

```bash
# Linux / macOS
./mvnw clean test

# Windows
.\mvnw.cmd clean test
```

### 3. Run the Application

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

The app will start on `http://localhost:8080`.

---

## Quick API Test Walkthrough (cURL)

### 1. Register a new user

```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alex",
    "password": "Password123!",
    "confirmPassword": "Password123!"
  }'
```

Response:

```json
{
  "token": "<JWT_TOKEN>"
}
```

### 2. Log in

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alex",
    "password": "Password123!"
  }'
```

### 3. Call the protected demo endpoint

```bash
curl -i -X GET http://localhost:8080/api/demo \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

Response: `hello`

### 4. Fetch own user profile

```bash
curl -i -X GET http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 5. Try fetching another user profile as regular user (Expect 403)

```bash
curl -i -X GET http://localhost:8080/api/users/2 \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

Response:

```json
{
  "status": 403,
  "message": "Access Denied: You do not have permission to access this resource",
  "timestamp": "2026-10-17T11:46:00Z"
}
```
