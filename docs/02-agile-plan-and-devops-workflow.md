# Task 2 — Agile Planning and DevOps Workflow

## Working agreement
Use two lightweight sprints. Track product work in issues, use short-lived branches, require PR review/self-review, and merge into develop. Keep main release-ready. Solo work may use disclosed self-review but must not invent another person's approval.

## User stories and acceptance criteria
| ID | Story | Acceptance |
|---|---|---|
| US-01 | Client registers | Valid details accepted; email unique; password hashed |
| US-02 | Client signs in/out | Valid login establishes session; logout invalidates it |
| US-03 | Client browses slots | Future open slots shown and sorted |
| US-04 | Staff manage availability | Authorized staff create future slots; overlaps rejected |
| US-05 | Client requests slot | Booking starts PENDING; taken slot rejected |
| US-06 | Client tracks status | Own bookings visible after refresh; other bookings hidden |
| US-07 | Staff decide request | Authorized staff confirm/reject pending booking |
| US-08 | Client cancels | Ownership and cancellation policy enforced |
| US-09 | Operator checks health | Health endpoint reports service state |
| US-10 | Developer uses CI | Build/tests run after changes; reports published |
| US-11 | Operator deploys repeatably | Versioned image starts with config and health check |
| US-12 | Operator recovers service | Provisioning is idempotent and rollback is verified |

The frozen product backlog is P01–P15 in Task 1. Track DevOps setup separately from product features.

## Sprint plan
**Sprint 1 — product slice and Git collaboration**
- Create project skeleton, branch policy and issue backlog.
- Implement registration/authentication and availability.
- Implement booking request and safe slot-claim logic.
- Demonstrate feature branch, PR, review and merge.
- Exit when the core booking request persists and is tested.

**Sprint 2 — lifecycle, testing and release**
- Implement confirm/reject/list/status/cancel.
- Finish validation, PostgreSQL persistence and health endpoint.
- Add Selenium journeys and Maven verification.
- Configure Jenkins CI/pipeline, image build and deployment.
- Configure Ansible, idempotency, health and rollback.
- Exit when evidence has real results and remaining gaps are explicit.

Suggested duration is one week per sprint, adjusted to the course deadline.

## Kanban
Columns: Backlog → Ready → In Progress → Review → Done, plus Blocked. Ready means acceptance criteria/dependencies are clear. Limit in-progress tasks to two. Done means merged, tested and documented—not just edited locally.

## Definition of Ready
A task has user outcome, acceptance criteria, bounded scope, known dependencies, test approach and no unresolved product decision.

## Definition of Done
- Acceptance criteria verified.
- Unit/integration and relevant Selenium tests pass.
- Clean Maven verification succeeds.
- Code and secrets reviewed.
- PR description/review checklist complete; feedback addressed.
- Docs/config updated.
- Change merged to develop; release changes reviewed into main.
- CI result and evidence captured; failures are not silently ignored.

## DevOps lifecycle
1. Plan issue and acceptance criteria.
2. Code on feature branch with small commits and local checks.
3. Review PR, address feedback and merge.
4. Jenkins checkout and Maven build.
5. Unit/integration and Selenium tests.
6. Package versioned JAR and Docker image.
7. Deploy only after gates pass.
8. Monitor health and logs.
9. Learn from failures and update backlog.

See diagrams.md for the pipeline, architecture, booking sequence and state diagrams.
