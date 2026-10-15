# NutriSync Assignment Backlog

This is the canonical tracker for the 14-task DevOps assignment and the 15-item MVP. All items start open until implementation and acceptance are backed by evidence.

## Product MVP (P01–P15)
- [x] [P01 — Registration](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/3) — implementation merged and test coverage exists
- [x] [P02 — Authentication/logout](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/4) — implementation merged and Selenium login/logout journey exists
- [x] [P03 — Availability browsing](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/5) — implementation merged and Selenium journey exists
- [x] [P04 — Staff availability management](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/6) — implementation merged and service tests exist
- [x] [P05 — Booking request](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/7) — implementation merged and integration tests exist
- [ ] [P06 — Concurrent double-booking prevention](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/8) — sequential conflict tests exist; simultaneous concurrency test is a remaining verification gap
- [x] [P07 — Confirm booking](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/9) — implementation merged and API/service tests exist
- [x] [P08 — Reject booking](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/10) — implementation merged and slot release tested
- [x] [P09 — Client booking list](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/11) — implementation merged and ownership tests exist
- [x] [P10 — Booking access/status](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/12) — implementation merged and cross-client access test exists
- [x] [P11 — Cancellation](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/13) — implementation merged and lifecycle tests exist
- [x] [P12 — Safe validation/errors](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/14) — implementation merged and error mapping exists
- [ ] [P13 — PostgreSQL durability](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/15) — PostgreSQL Compose/CI exists; explicit persistence-through-app-container-replacement evidence remains
- [x] [P14 — Health endpoint](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/16) — implemented and used by health checks
- [ ] [P15 — Release artifact](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/17) — Maven artifact/image build exists; inspect a versioned release artifact and record its ID/digest

## Assignment tasks / external evidence
- [ ] [Task 1 — validate user pain points](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/31)
- [ ] [Tasks 2–4 — Agile workflow, SRS, GitHub setup](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/33)
- [x] [Task 5 — feature branch/PR cycle](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/pull/2) — implementation PR #2 merged after verification
- [ ] [Task 6 — manual acceptance/conflict exercise/release](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/32)
- [ ] [Tasks 7–10 — real Jenkins execution and failed-test gate evidence](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/29)
- [ ] [Tasks 11–12 — Docker lifecycle and registry/deployment evidence](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/32)
- [ ] [Tasks 13–14 — Ansible provisioning, idempotency and rollback](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/30)

## CI evidence
- Latest verified successful baseline prior to current documentation updates: [Run #38706341153](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/actions/runs/38706341153).
- A report artifact was available on that run (artifact ID `11959435410`, expiration `2026-10-29`).
- CI is rerunning after the recent README/evidence-checklist commits. Update the checklist only after inspecting the new run result.

## Rules
- Open issue means acceptance is not fully signed off; do not infer that code does not exist.
- Do not mark Jenkins, remote host provisioning, registry push, manual interviews or release-tag checks complete without real evidence.
- Use synthetic test data only; do not put passwords, personal health information, or private host data in evidence.


## Backlog hygiene
The original product issues (#3–#17) are the canonical P01–P15 tracker. Some exploratory duplicate issues were closed as duplicates during cleanup; use the links above plus open operational issues #29–#33 and do not reopen the duplicates unless they need a distinct scope.
