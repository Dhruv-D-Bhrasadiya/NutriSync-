# Task 14 — Provisioning, Idempotency and Rollback

## Goal
Provision a clean host, deploy NutriSync, prove rerunning Ansible makes no unintended changes, check health and restore a previous known-good release in a safe lab.

## Baseline capture
Record OS/version, Ansible version, sanitized target alias, source commit, artifact/image version, app port, Nginx route, health endpoint, active service/container and previous known-good image digest/tag. Use disposable data only.

## Clean provisioning
1. Create/reset a disposable VM from a clean OS image.
2. Configure SSH, Python and privilege escalation.
3. Run inventory validation and Ansible ping.
4. Run syntax check and check mode.
5. Apply the playbook.
6. Deploy the approved artifact/image.
7. Verify application and Nginx service/container state.
8. Call health endpoint from host and intended client network.
9. Run a synthetic registration/booking smoke test if app/database exist.
10. Save logs, exit status and screenshots.

## Idempotency proof
Run the same playbook twice:
~~~bash
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml
~~~

Inspect the second-run summary. Already-correct resources should show zero unintended changes. Investigate any changing task; do not assume idempotency.

## Health and reliability checks
- App/container is active.
- Health endpoint passes; DB readiness is checked if configured.
- Nginx serves expected route/status.
- Logs show no repeated crash/datasource error.
- Synthetic workflow succeeds.
- Restart does not erase PostgreSQL records.
- Invalid configuration fails visibly and safely.

## Rollback exercise
1. Deploy version A and record tag/digest plus health result.
2. Deploy candidate version B in test environment, optionally with a controlled startup/health failure.
3. Confirm B's health gate fails and A remains available.
4. Restore A's image/artifact and compatible configuration.
5. Switch traffic/restart through documented command/playbook.
6. Verify health and repeat smoke test.
7. Record versions, commands, outputs and data-integrity observations.

Avoid destructive database migrations. An image rollback does not undo a schema migration; use backward-compatible migrations or restore a disposable database snapshot.

## Decision table
| Symptom | Response |
|---|---|
| Build/test fails before deploy | Keep current version |
| New image fails health | Switch back to known-good image |
| Nginx config invalid | Restore last known-good config, test and reload |
| Database unavailable | Gate app as unhealthy; inspect network/credentials safely |
| Partial Ansible failure | Inspect failed task, fix and rerun |
| Possible data corruption | Stop risky writes and use a verified recovery procedure |

## Definition of Done
Clean host provisioned, first-run logs captured, second-run idempotency inspected, health/smoke checks pass, rollback demonstrated and docs/evidence match the actual run.
