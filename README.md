# NutriSync — Automated Nutrition Consultation Scheduling System

A Java/Maven DevOps learning project for a small consultation-booking MVP and a documented path through Git collaboration, CI, browser testing, containerization, deployment and provisioning.

**Status:** This commit establishes documentation. The repository began with only an initial README and .gitignore. Application code, Jenkins jobs, a live deployment, Docker registry image and Ansible run remain execution tasks; this README does not claim they already exist.

## Product goal
Clients register, view available nutrition-consultation slots, request a booking, check its status and cancel an eligible booking. Nutritionists or a demo administrator manage availability and confirm or reject requests.

## Technology baseline
- Java 17, Spring Boot 3.x, Spring MVC/Thymeleaf, Spring Security
- Maven Wrapper, Spring Data JPA, PostgreSQL
- Executable JAR with embedded Tomcat, Nginx reverse proxy
- Selenium WebDriver 4 and JUnit 5
- Jenkins Pipeline as Code, Docker and Ansible
- Local development on Linux, Windows + WSL2, or macOS

Nginx is the reverse proxy. Spring Boot's executable JAR includes embedded Tomcat, so a separately installed Tomcat is not required. Pin actual dependency and base-image versions when implementation begins.

## Assignment guides
| Task | Guide |
|---|---|
| 1 — problem, stakeholders, scope, 15-task MVP | [Problem definition and MVP](docs/01-problem-definition-and-mvp.md) |
| 2 — user stories, backlog, sprint, DoD, lifecycle | [Agile plan](docs/02-agile-plan-and-devops-workflow.md) |
| 3 — SRS, architecture, data model, APIs, setup | [SRS and architecture](docs/03-srs-architecture-and-api.md) |
| 4 — repository and GitHub conventions | [Git/GitHub workflow](docs/04-git-github-workflow.md) |
| 5 — feature branch, PR and merge | [Feature development playbook](docs/05-feature-development-playbook.md) |
| 6 — MVP acceptance, conflict exercise, release | [MVP and release](docs/06-mvp-acceptance-and-release.md) |
| 7 — Jenkins installation and CI job | [Jenkins CI setup](docs/07-jenkins-ci-setup.md) |
| 8 — Jenkinsfile and deployment | [Pipeline and deployment](docs/08-jenkins-pipeline-and-deployment.md) |
| 9 — Selenium plan and local execution | [Selenium test plan](docs/09-selenium-test-plan.md) |
| 10 — tests in Jenkins and deployment gate | [Continuous testing](docs/10-continuous-testing-in-jenkins.md) |
| 11 — Docker image/container lifecycle | [Docker lifecycle](docs/11-docker-container-lifecycle.md) |
| 12 — Jenkins-to-Docker deployment | [Docker continuous deployment](docs/12-jenkins-docker-cd.md) |
| 13 — Ansible configuration management | [Ansible guide](docs/13-ansible-configuration-management.md) |
| 14 — provisioning, idempotency, health, rollback | [Reliability and rollback](docs/14-provisioning-reliability-and-rollback.md) |
| Diagrams | [Mermaid diagrams](docs/diagrams.md) |
| Submission evidence | [Evidence checklist](docs/evidence-checklist.md) |

## Product journey
1. Client registers and signs in.
2. They view future open consultation slots.
3. They request a slot; status starts as PENDING.
4. A nutritionist/admin confirms or rejects.
5. The client tracks status and may cancel under the policy.
6. State changes persist after refresh/restart.

## Target layout
~~~text
NutriSync-/
├── .github/ISSUE_TEMPLATE/
├── .github/pull_request_template.md
├── .github/branching-and-commit-policy.md
├── docs/                       # assignment guides and Mermaid diagrams
├── src/main/java/              # application code to be implemented
├── src/test/java/
├── ansible/                    # provisioning to be implemented
├── pom.xml                     # to be implemented
├── Dockerfile                  # to be implemented
└── Jenkinsfile                 # to be implemented
~~~

## Security and evidence
Use synthetic demo accounts only. Do not store diagnoses, prescriptions, health records or payment data. Never commit passwords, tokens, private keys, real environment files or registry credentials. Use Jenkins Credentials, Ansible Vault or environment variables. Capture real command output and screenshots; mark a task complete only after it has actually run.

## License
A license has not been selected. Add one once reuse terms have been decided.
