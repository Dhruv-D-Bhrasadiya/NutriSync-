# NutriSync — Automated Nutrition Consultation Scheduling System

NutriSync is a Java web application for requesting and managing nutrition consultation slots. The repository includes the application, JSON API, Docker Compose stack, database-backed tests, optional Selenium journeys, Jenkins pipeline and Ansible deployment automation.

## Features
- Client registration and login with BCrypt-hashed passwords.
- Future availability display and staff slot management.
- Booking request, status tracking, confirmation/rejection and cancellation.
- 24-hour client cancellation notice policy.
- Transactional slot claiming with database row locking.
- Stateless HTTP Basic JSON API, health endpoint, PostgreSQL persistence and deployment files.

> **Demo safety:** Compose activates a local demo profile and seeds fixed accounts. Use only in a local/private learning environment. Never expose this demo stack publicly or use it for actual health information.

## Requirements
- Docker Desktop or Docker Engine + Docker Compose plugin.
- Git.
- For running Maven/tests directly: JDK 17 and Maven 3.9+.
- Selenium tests use a Docker-hosted Chrome browser.

## 1. Quick start (recommended: Docker Compose)

~~~bash
git clone https://github.com/Dhruv-D-Bhrasadiya/NutriSync-.git
cd NutriSync-
cp .env.example .env
~~~

Edit `.env`: replace the placeholder `POSTGRES_PASSWORD` with a unique random secret (Linux/macOS: `openssl rand -hex 24`). Do not commit it.

~~~bash
docker compose up --build -d
docker compose ps
docker compose logs -f app
~~~

When the app reports healthy, open **http://localhost:8080**. Port 8080 is published to loopback only; PostgreSQL is not exposed directly on the host.

Health endpoint: http://localhost:8080/actuator/health

### Local demo accounts

All accounts below are seeded only for a local/private demo environment. They share sample passwords for convenience and must never be used in a public deployment.

| Role | Name | Email | Password |
|---|---|---|---|
| Client | Demo Client | `client@nutrisync.local` | `NutriSyncDemo!2026` |
| Nutritionist | Dr. Aanya Mehta | `aanya.mehta@nutrisync.local` | `NutriSyncDemo!2026` |
| Nutritionist | Dr. Rohan Kulkarni | `rohan.kulkarni@nutrisync.local` | `NutriSyncDemo!2026` |
| Nutritionist | Dr. Isha Desai | `isha.desai@nutrisync.local` | `NutriSyncDemo!2026` |
| Administrator | Demo Administrator | `admin@nutrisync.local` | `NutriSyncAdmin!2026` |

Each nutritionist has a separate sample availability calendar. The signed-in nutritionist may publish availability and confirm/reject only requests for slots owned by their own account. The administrator has a read-only monitoring dashboard at `/admin/monitoring` and cannot publish availability as a nutritionist.

These fixed credentials are available only when both the `demo` profile and `APP_SEED_DEMO_DATA=true` are enabled. Compose enables them for local learning. Do not use seeded accounts or passwords in a public deployment.


### Stop/reset

~~~bash
docker compose logs --tail=200 app
docker compose down
~~~

To also delete all local DB data and demo bookings, run `docker compose down -v`. **That command is destructive.** Regular `down` preserves the database volume.

## 2. Run with Maven (without the app container)

This uses the Compose PostgreSQL service and runs Spring Boot directly on your host. First create .env as described in Quick start and set a unique POSTGRES_PASSWORD; then start only the database:

~~~bash
docker compose up -d db
docker compose ps db
~~~

Linux/macOS (from the repository root):
~~~bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/nutrisync'
export SPRING_DATASOURCE_USERNAME='nutrisync'
# Use the same local-only password configured as POSTGRES_PASSWORD in .env.
read -s -p 'Local PostgreSQL password: ' SPRING_DATASOURCE_PASSWORD
echo
export SPRING_DATASOURCE_PASSWORD
export APP_TIME_ZONE='Asia/Kolkata'
# Optional: seed demo accounts/slots on a private development DB only.
export SPRING_PROFILES_ACTIVE=demo
export APP_SEED_DEMO_DATA=true
mvn spring-boot:run
# When finished, clear the secret from this shell.
unset SPRING_DATASOURCE_PASSWORD
~~~

PowerShell (replace the value with the same password from your local .env):
~~~powershell
docker compose up -d db
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/nutrisync'
$env:SPRING_DATASOURCE_USERNAME = 'nutrisync'
$securePassword = Read-Host 'Local PostgreSQL password' -AsSecureString
$env:SPRING_DATASOURCE_PASSWORD = [System.Net.NetworkCredential]::new('', $securePassword).Password
$env:APP_TIME_ZONE = 'Asia/Kolkata'
# Optional and private/local only:
$env:SPRING_PROFILES_ACTIVE = 'demo'
$env:APP_SEED_DEMO_DATA = 'true'
mvn spring-boot:run
# Remove it from the shell session afterward.
Remove-Item Env:SPRING_DATASOURCE_PASSWORD
~~~

To stop the database container without deleting its data, run `docker compose stop db`. To remove the container while preserving its volume, run `docker compose down`. Do not use `docker compose down -v` unless you intentionally want to delete the local database volume.

Demo accounts and sample slots are seeded only when `SPRING_PROFILES_ACTIVE=demo` and `APP_SEED_DEMO_DATA=true`. The default runtime configuration does not create fixed credentials. Use those settings only on a private/local learning environment and disable them for an internet-facing deployment.

## 3. Run automated tests

Standard tests use an H2 in-memory database and do not need external PostgreSQL:

~~~bash
mvn -B clean test
mvn -B clean verify
~~~

Reports: `target/surefire-reports/`. Tests cover password hashing, duplicate-email registration, booking creation, duplicate slot-claim rejection, slot release after staff rejection, API response serialization, booking lifecycle and ownership checks. The GitHub Actions workflow also starts an isolated PostgreSQL + app + Selenium Chrome Compose stack and runs the five browser journeys on each qualifying push/PR. It archives Maven Surefire/Failsafe reports and any Selenium failure screenshots as workflow artifacts.

## 4. Run Selenium journeys

Start Compose including its optional Selenium Chrome service:

~~~bash
docker compose --profile e2e up --build -d --wait
docker compose --profile e2e ps
~~~

Run from the repository root:

~~~bash
mvn -B -Pselenium-e2e verify \
  -De2e.enabled=true \
  -De2e.baseUrl=http://nutrisync-web:8080 \
  -De2e.remoteUrl=http://localhost:4444
~~~

The remote browser resolves the Compose network alias `nutrisync-web` (chosen to avoid browsers' automatic HTTPS upgrades for the hostname `app`); Maven connects to Selenium on localhost port 4444. These browser tests use the demo data profile. Failure screenshots are saved under `target/selenium-failures/`.

When finished, `docker compose --profile e2e down` removes the containers but preserves the named database volume.

## 5. JSON API

API routes use `/api/v1`. Registration and listing open slots are public. Protected operations use stateless HTTP Basic authentication, so pass credentials with each request.

List future slots:
~~~bash
curl --fail http://localhost:8080/api/v1/slots
~~~

Register a client:
~~~bash
curl --fail-with-body -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"API Demo Client","email":"api.client@example.test","password":"LongUniquePass!2026"}'
~~~

List your bookings:
~~~bash
curl --fail -u 'client@nutrisync.local:NutriSyncDemo!2026' \
  http://localhost:8080/api/v1/bookings/mine
~~~

Request a booking (replace ID with a future slot ID from the response):
~~~bash
curl --fail-with-body -u 'client@nutrisync.local:NutriSyncDemo!2026' \
  -H 'Content-Type: application/json' -X POST http://localhost:8080/api/v1/bookings \
  -d '{"slotId":1}'
~~~

Staff can use `PATCH /api/v1/bookings/{id}/confirm`, `/reject` and `/cancel`. Staff can create slots with `POST /api/v1/slots` and ISO instant values such as `{"startsAt":"2030-01-02T09:00:00Z","endsAt":"2030-01-02T09:45:00Z"}`. Authorization and booking ownership are checked server-side.

## 6. Jenkins CI/CD

The root `Jenkinsfile` checks out source, runs Maven verification, builds a Docker image, optionally runs Selenium, optionally publishes to Docker Hub, then deploys from main. Use a **Multibranch Pipeline** job so branch-specific stages and PR builds receive the correct `BRANCH_NAME`. The Selenium stack uses a build-specific Compose project and removes its temporary database volume on cleanup; the persistent `nutrisync` deployment stack is kept separate.

Configure Jenkins Global Tools with JDK name `JDK17` (JDK 17) and Maven name `Maven_3_9` (Maven 3.9+). Docker and Compose must be usable by the build agent.

For deployments on main, configure these Jenkins **Secret text** credentials: `nutrisync-postgres-password` (persistent database password), `nutrisync-bootstrap-staff-name`, `nutrisync-bootstrap-staff-email`, and `nutrisync-bootstrap-staff-password` (initial nutritionist account; use a 20–72 character unique password). The main deployment runs the `prod` profile with demo seeding disabled and uses the opt-in staff bootstrap only when the account is absent. The bootstrap does not reset an existing password. For image publishing, add a username/password credential with ID `nutrisync-dockerhub`, using a Docker Hub access token as the password. Set `PUSH_IMAGE` only after registry credentials/repository are configured. `APP_HOST_PORT` is parameterized (1024–65535) and bound to loopback; this Jenkinsfile deploys on the Jenkins agent's Docker host, not a remote production server. Use the Ansible playbook for a separate target host. This is a learning setup, not production hardening.

## 7. Ansible provisioning

Install Ansible and the required collection, then copy the sample inventory:

~~~bash
ansible-galaxy collection install -r ansible/collections/requirements.yml
cp ansible/inventory/dev.example.ini ansible/inventory/dev.ini
~~~

Edit the ignored `dev.ini` for a disposable Ubuntu 22.04+ host. Export `NUTRISYNC_IMAGE` as an image reference the host can pull and `NUTRISYNC_DB_PASSWORD` as a unique 20+ character secret on the control node. Do not commit or print these values.

The Ansible target disables demo accounts. To create the initial nutritionist account, enable the opt-in bootstrap and pass a dedicated account through protected environment variables (recommended only during first provisioning or while resetting a disposable environment):

~~~bash
export NUTRISYNC_BOOTSTRAP_STAFF_ENABLED=true
export NUTRISYNC_BOOTSTRAP_STAFF_NAME='Nutritionist'
export NUTRISYNC_BOOTSTRAP_STAFF_EMAIL='nutritionist@your-demo-domain.example'
read -s -p 'Initial staff password (20–72 characters): ' NUTRISYNC_BOOTSTRAP_STAFF_PASSWORD
echo
# Apply the playbook below. The bootstrap runs only if the account is absent.
# It never resets an existing account's password; clear shell variables after provisioning.
~~~

Use a real email under a domain you control for a real environment. Keep these values in a secrets manager or a short-lived shell session, do not add them to inventory or group_vars, and do not enable the bootstrap in an internet-facing environment unless the onboarding procedure has been reviewed. The bootstrap flag defaults to false.

~~~bash
ansible -i ansible/inventory/dev.ini app -m ping
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml --syntax-check
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml --check --diff
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml
~~~

Inspect the second run for unintended changes. Roll back by setting `NUTRISYNC_IMAGE` to a known-good prior image and rerunning the playbook. Full steps: [ansible/README.md](ansible/README.md).

## Assignment documents
This repository tracks a 14-task Agile/DevOps assignment. The guides describe scope and runbooks; environment-dependent work is only complete once verified evidence exists.

- [Task 1 — problem definition and MVP](docs/01-problem-definition-and-mvp.md)
- [Task 2 — Agile planning and DevOps workflow](docs/02-agile-plan-and-devops-workflow.md)
- [Task 3 — SRS, architecture and API](docs/03-srs-architecture-and-api.md)
- [Task 4 — Git/GitHub workflow](docs/04-git-github-workflow.md)
- [Task 5 — feature development playbook](docs/05-feature-development-playbook.md)
- [Task 6 — MVP acceptance and release](docs/06-mvp-acceptance-and-release.md)
- [Task 7 — Jenkins CI setup](docs/07-jenkins-ci-setup.md)
- [Task 8 — Jenkins pipeline and deployment](docs/08-jenkins-pipeline-and-deployment.md)
- [Task 9 — Selenium test plan](docs/09-selenium-test-plan.md)
- [Task 10 — continuous testing in Jenkins](docs/10-continuous-testing-in-jenkins.md)
- [Task 11 — Docker container lifecycle](docs/11-docker-container-lifecycle.md)
- [Task 12 — Jenkins-to-Docker CD](docs/12-jenkins-docker-cd.md)
- [Task 13 — Ansible configuration management](docs/13-ansible-configuration-management.md)
- [Task 14 — provisioning, reliability and rollback](docs/14-provisioning-reliability-and-rollback.md)
- [Architecture and workflow diagrams](docs/diagrams.md)
- [Final evidence checklist](docs/evidence-checklist.md)
- [Canonical assignment backlog (P01–P15 and Tasks 1–14)](docs/assignment-backlog.md)
- [GitHub issues backlog](https://github.com/Dhruv-D-Bhrasadiya/NutriSync-/issues)

## Important production gaps
This is an educational MVP. Compose intentionally seeds known demo accounts; never use that profile in production. Email verification, password reset, rate limiting, audit trail, reminders, calendar integration, TLS termination, external secrets, production backups, monitoring, database migrations and compliance review are not implemented. Add them before exposing a real service publicly.

## License
No license has been selected yet.
