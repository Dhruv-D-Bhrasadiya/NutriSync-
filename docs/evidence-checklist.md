# NutriSync — Final Evidence Checklist

This checklist distinguishes committed implementation from work that must still be demonstrated on a real Jenkins controller or target host. Replace TBD with actual build IDs, screenshots, and deployment details. Never invent evidence.

## Task 1 — problem and MVP
- [x] Problem, stakeholder list, constraints and 15-item product scope documented.
- [ ] Validate pain points with users, or record explicitly that no interviews were conducted.
- Evidence: [Problem statement](01-problem-definition-and-mvp.md)

## Task 2 — Agile planning and DevOps workflow
- [x] User stories, acceptance criteria, sprint plan, Definition of Done and Mermaid workflows documented.
- [x] Create a GitHub Issues backlog for MVP items P01–P15 and the environment-dependent assignment tasks; backlog tracking issue: [#33](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/33).
- [ ] Create/populate a GitHub Project board (not verifiable with the connected repository file/issue tools).
- Evidence: [Agile plan](02-agile-plan-and-devops-workflow.md)

## Task 3 — requirements, architecture and local setup
- [x] SRS summary, architecture, data model, API contract and diagrams committed.
- [x] Application health and the app/PostgreSQL stack verified by the successful CI run.
- Evidence: [CI run](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/actions/runs/38679031958)

## Task 4 — GitHub repository setup
- [x] README, .gitignore, issue templates, PR template and branch policy committed.
- [x] Java/Maven application, tests, Docker/Compose, Jenkinsfile, GitHub Actions and Ansible playbook committed.
- [x] Create/populate GitHub Issues for P01–P15 and DevOps/evidence follow-up tasks; see [#33](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues/33).
- [ ] Populate a GitHub Project board (requires Projects access not available through the current connector actions).
- Evidence: [Implementation PR #2](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/pull/2)

## Task 5 — feature branch and PR collaboration
- [x] Working feature branch and booking workflow were committed.
- [x] PR #2 reviewed through self-review and merged; no independent reviewer is claimed.
- [x] Merge commit: `6b7fa6924ee3d53af99b5fa7c75aa4e90cf87bca`.
- Evidence: [Merged implementation PR](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/pull/2)

## Task 6 — MVP acceptance and release baseline
- [ ] Run the complete P01–P15 manual acceptance matrix. Automated tests cover core registration, slot, booking, ownership and cancellation flows, but full manual sign-off is still outstanding.
- [ ] Demonstrate and record the deliberate Git merge-conflict exercise.
- [ ] Create a release tag only after final acceptance checks and release review.
- Evidence: [MVP acceptance matrix](06-mvp-acceptance-and-release.md)

## Task 7 — Jenkins installation and CI job
- [ ] Install and secure a real Jenkins controller/agent.
- [ ] Configure SCM and a webhook or polling trigger.
- [ ] Capture a real Jenkins build log, test report and archived Maven artifact.
- Job/build URL: TBD
- Guide: [Jenkins CI setup](07-jenkins-ci-setup.md)

## Task 8 — Jenkins pipeline and deployment
- [x] Jenkinsfile committed with checkout, build/test, Docker, parameterized deployment and health-check logic.
- [ ] Run the Jenkins pipeline on a configured Multibranch Pipeline job.
- [ ] Verify the Jenkins deployment parameter and deployed application URL.
- Build/URL: TBD
- Guide: [Pipeline and deployment](08-jenkins-pipeline-and-deployment.md)

## Task 9 — Selenium test design and local/CI execution
- [x] Five browser journeys with assertions and synthetic data isolation implemented.
- [x] The Selenium suite passed in the linked CI run.
- [x] CI artifact upload is configured and a downloadable report artifact was verified on the latest successful main run: [run #38706341153](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/actions/runs/38706341153) (artifact ID `11959435410`, expires 2026-10-29).
- [ ] Save a local test report/screenshot set for the assignment submission.
- Evidence: [Latest successful main CI run](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/actions/runs/38706341153)

## Task 10 — continuous testing in Jenkins
- [x] Jenkinsfile places Selenium verification before deployment, so failed tests cannot reach the deploy stage.
- [ ] Execute and capture a real Jenkins failed-test run and prove deployment was skipped.
- [ ] Capture the corrective commit and successful Jenkins rerun.
- Evidence: TBD
- Guide: [Continuous testing](10-continuous-testing-in-jenkins.md)

## Task 11 — Docker image and container lifecycle
- [x] Dockerfile builds successfully in CI; Compose stack starts and passes health checks.
- [ ] Record a versioned release image ID/digest and inspect output.
- [ ] Demonstrate logs, inspect, port mapping, restart, stop/start and removal commands.
- [ ] Explicitly verify persistent database records survive application-container replacement.
- Evidence: [Successful CI run](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/actions/runs/38679031958)

## Task 12 — Jenkins-to-Docker continuous deployment
- [x] Jenkinsfile contains versioned image tagging, optional registry push and a gated deployment path.
- [ ] Configure registry credentials and prove an image was pushed.
- [ ] Run end-to-end Jenkins commit-to-container deployment and capture health check.
- Registry/build URL: TBD
- Guide: [Jenkins-Docker CD](12-jenkins-docker-cd.md)

## Task 13 — Ansible configuration management
- [x] Sanitized inventory example, Ansible playbook and Nginx template committed.
- [ ] Run ping, syntax check and check mode against the target host.
- [ ] Capture the first real Ansible execution log.
- Evidence: TBD
- Guide: [Ansible configuration](13-ansible-configuration-management.md)

## Task 14 — provisioning, idempotency and rollback
- [ ] Provision a clean disposable host using Ansible.
- [ ] Inspect the second run and demonstrate idempotency.
- [ ] Capture host health and a synthetic application smoke test.
- [ ] Demonstrate rollback to a previous known-good image/release.
- Evidence: TBD
- Guide: [Provisioning and rollback](14-provisioning-reliability-and-rollback.md)

## Final review
- [x] README links point to the committed documentation and project files.
- [x] No real credentials, private keys, .env files or health records were intentionally committed; fixed demo credentials are clearly restricted to local learning.
- [x] Confirm latest main-branch CI is green and a test-report artifact is available: [run #38706341153](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/actions/runs/38706341153).
- [ ] Save final submission evidence and record the release tag after acceptance.

Do not mark environment-dependent tasks complete until they have run. Keep logs/screenshots sanitized and follow the instructor's evidence-file requirements.
