# Task 1 — Problem Definition and MVP Scope

## Problem statement
Clients seeking nutrition guidance may coordinate availability through calls, messages or spreadsheets. This can lead to repeated back-and-forth, stale availability, duplicate slot claims, missed requests and uncertainty about whether an appointment is confirmed. NutriSync proposes one web workflow to view open slots, request a booking, track status and cancel eligible bookings.

This is an educational scheduling system, not a clinical decision-support or medical-record product.

## Stakeholders
| Stakeholder | Need |
|---|---|
| Client | View times, request a consultation, know status and cancel |
| Nutritionist | Publish availability and confirm/reject requests |
| Admin/operator | Support demo setup, monitor service and troubleshoot |
| Developer | Small scope, testable behavior and reproducible setup |
| Instructor/reviewer | Requirements, Git evidence, tests and operational evidence |
| DevOps operator | Repeatable builds, safe secrets, health and rollback |
| Platform owners | GitHub, Jenkins, registry and host access configured safely |

## Pain points to validate
These are hypotheses, not researched findings: availability may be fragmented; status may be unclear; two users may compete for one slot; cancellation may not release availability; deployment may be inconsistent. No user interviews were conducted for the 2026-10-16 evidence run, so these hypotheses remain unvalidated. Do not infer participant experiences or invent interview outcomes. If interviews are conducted later, record only anonymized, actual responses.

## Objectives and success criteria
| Objective | Target |
|---|---|
| Registration | Valid account saved; duplicate email rejected; password hashed |
| Availability | Future open slots visible in chronological order |
| Booking integrity | No slot receives more than one active booking, including concurrent attempts |
| Lifecycle | PENDING, CONFIRMED, REJECTED, CANCELLED and COMPLETED visible |
| Cancellation | Policy enforced and slot occupancy updated consistently |
| Automated tests | Five critical Selenium journeys with reports |
| CI | Clean Maven verification from fresh checkout |
| Deployment | Build/test failures block deployment |
| Runtime | Health check succeeds after deployment |
| Recovery | Prior known-good release can be restored |

These are intended targets, not achieved measurements.

## Constraints and assumptions
- Freeze MVP at 15 product backlog items.
- Use Java 17, Maven, Spring Boot, PostgreSQL, Selenium, Jenkins, Docker, Nginx and Ansible.
- A local machine or disposable Ubuntu VM/WSL2 is enough; cloud hosting is optional.
- Store timestamps in UTC and render a consistent timezone.
- A request is pending until staff decides.
- Do not store sensitive medical records. Payments, video calls and clinical recommendations are out of scope.

## Frozen MVP — 15 product tasks
| ID | Item | Acceptance |
|---|---|---|
| P01 | Client registration | Valid account persisted; duplicate email rejected; password hashed |
| P02 | Login/logout | Login works; invalid login handled safely; logout invalidates session |
| P03 | Browse availability | Future open slots sorted by start time |
| P04 | Manage availability | Authorized staff add future slots; overlaps rejected |
| P05 | Request booking | PENDING booking linked to client and slot |
| P06 | Prevent double booking | Concurrent/duplicate active claims rejected safely |
| P07 | Confirm booking | Authorized staff confirm pending request |
| P08 | Reject booking | Authorized staff reject pending request |
| P09 | My bookings | Client sees own list |
| P10 | Booking detail/status | Owner or authorized staff sees state |
| P11 | Cancel booking | Ownership/policy checked; slot released consistently |
| P12 | Validation/errors | Clear safe errors; no exposed stack traces |
| P13 | Persistence | Core data survives app restart in PostgreSQL |
| P14 | Health endpoint | Runtime and configured database readiness can be checked |
| P15 | Release package | Maven creates versioned runnable artifact/image |

## Explicitly out of scope
Payments, video calls, diet plans, food tracking, prescriptions, clinical records, AI recommendations, SMS/email, calendar integration, mobile apps, multi-clinic tenancy and advanced analytics.

## Risks and mitigation
- Double booking: database constraint/locking plus transaction, not UI hiding alone.
- Unauthorized changes: server-side role and ownership checks.
- Credential leak: hashed passwords and externalized secrets.
- Environment drift: pin versions; use repeatable Maven/Docker/Ansible commands.
- False claims: use actual logs, screenshots and links in the evidence checklist.

## Sign-off
Confirm the 15 items are achievable within the assignment time. Track new feature requests separately as post-MVP work.
