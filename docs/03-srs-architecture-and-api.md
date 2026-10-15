# Task 3 — SRS Summary, Architecture, Data Model and API

## Product and roles
NutriSync is a browser-based nutrition consultation scheduler. Clients register, browse future slots, request bookings, track status and cancel eligible bookings. Nutritionists manage slots and confirm/reject requests; an admin supports demo operations. Roles are CLIENT, NUTRITIONIST and ADMIN. Enforce role and record ownership on the server.

## Functional requirements
| ID | Requirement |
|---|---|
| FR-01 | Register user with unique normalized email and hashed password |
| FR-02 | Authenticate and logout |
| FR-03 | List future open slots with start/end and nutritionist |
| FR-04 | Authorized staff create/edit unused future slots |
| FR-05 | Client requests a slot; initial status PENDING |
| FR-06 | Concurrent claims cannot double-book a slot |
| FR-07 | Authorized staff confirm/reject pending requests |
| FR-08 | Client sees only own bookings/details |
| FR-09 | Eligible owner/staff cancellation supported |
| FR-10 | State and slot availability persist consistently |
| FR-11 | Safe actionable validation errors |
| FR-12 | Health endpoint supports deployment checks |
| FR-13 | Maven build/tests run in CI |

## Business rules
- Store appointment instants as UTC and render a consistent timezone.
- Only future slots can be added/booked; starts_at must precede ends_at.
- New booking status is PENDING.
- Transitions: PENDING → CONFIRMED, REJECTED or CANCELLED; CONFIRMED → CANCELLED or COMPLETED. Terminal states do not reopen in MVP.
- Only authorized staff confirm/reject. Clients can see/cancel only their own bookings.
- Proposed cancellation policy: clients may cancel PENDING/CONFIRMED bookings at least 24 hours before start.
- PENDING and CONFIRMED bookings occupy the slot; REJECTED/CANCELLED release it if no other active claim exists.

## Non-functional requirements
Security uses Spring Security, BCrypt or equivalent, ownership checks, parameterized persistence and CSRF protection for session-based forms. Reliability requires transactions and database constraints. Use clear accessible controls, controller/service/repository separation, isolated test data, health endpoint and structured logs without secrets. Store configuration in environment. Target common local-demo interactions under two seconds for a small synthetic dataset; measure before claiming achieved.

## Technology choices
| Layer | Choice |
|---|---|
| Language | Java 17 |
| Framework/UI | Spring Boot 3.x, Spring MVC, Thymeleaf |
| Security | Spring Security, session login |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Build | Maven installation |
| Runtime | Executable JAR with embedded Tomcat |
| Reverse proxy | Nginx |
| UI tests | Selenium 4, JUnit 5 |
| CI/CD | Jenkins Pipeline |
| Container/provisioning | Docker / Ansible |

Pin actual versions in the implementation. The choice is a baseline, not a completed build.

## Logical architecture
Browser → Nginx → Spring Boot application (embedded Tomcat) → controller → service → Spring Data JPA → PostgreSQL. Jenkins checks out Git, builds/tests/packages source and deploys after gates. Ansible configures the host; runtime health checks verify deployment.

## Data model
**users:** id, name, normalized unique email, password_hash, role, created_at, updated_at.

**availability_slots:** id, nutritionist_id (users FK), starts_at, ends_at, status, created_at.

**bookings:** id, client_id (users FK), slot_id (availability_slots FK), status, created_at and updated_at. Avoid clinical notes.

Add indexes for email, slot start, client booking history and slot lookup. Use transactions and a database-enforced slot claim. A simple “check then insert” without locking can race. Use one source of truth for slot occupancy.

## Proposed API
All routes under /api/v1. Browser MVC pages may wrap equivalent operations.

The browser UI uses session-based form login at `/login` and logout at `/logout`. The JSON API is stateless HTTP Basic; it does not expose separate JSON login/logout endpoints. All API routes below are prefixed with `/api/v1`.

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Public | Register a client |
| GET | `/api/v1/slots` | Public | List future open slots |
| POST | `/api/v1/slots` | NUTRITIONIST/ADMIN | Create availability with ISO-8601 `startsAt`/`endsAt` instants |
| POST | `/api/v1/bookings` | CLIENT | Request a slot by `slotId` |
| GET | `/api/v1/bookings/mine` | CLIENT | List own bookings |
| GET | `/api/v1/bookings/{bookingId}` | Owner/assigned nutritionist/ADMIN | Fetch authorized booking |
| PATCH | `/api/v1/bookings/{bookingId}/confirm` | Assigned NUTRITIONIST/ADMIN | Confirm a pending request |
| PATCH | `/api/v1/bookings/{bookingId}/reject` | Assigned NUTRITIONIST/ADMIN | Reject and release slot |
| PATCH | `/api/v1/bookings/{bookingId}/cancel` | Booking owner or authorized staff | Cancel according to policy |
| GET | `/actuator/health` | Runtime/monitor | Health check |

Successful registration/creation returns 201; invalid input returns 400, unauthenticated API requests return 401, unauthorized actions return 403, missing or non-owned records return 404, and unavailable slots/state conflicts return 409. The API disables CSRF only for its stateless HTTP Basic route group; the browser forms retain Spring Security CSRF protection. Check ownership before returning a booking to a caller.

## Local setup
1. Install JDK 17, Git and Docker.
2. Clone repo and create a feature branch.
3. Install Maven 3.9+; create pom.xml, source and tests.
4. Start PostgreSQL locally or through a development Compose setup.
5. Set SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME and SPRING_DATASOURCE_PASSWORD outside Git.
6. Run mvn clean verify (Windows: mvn clean verify).
7. Start with mvn spring-boot:run; use synthetic users and slots.
8. Save actual output and screenshots in the evidence checklist.

Lock remaining details during implementation: ID type, slot occupancy model, app port, slot-list access and demo timezone.
