# NutriSync — Automated Nutrition Consultation Scheduling System

NutriSync is a Java web application for requesting and managing nutrition consultation slots. This repository contains the application, JSON API, local Docker Compose stack, database-backed integration tests, optional Selenium journeys, Jenkins pipeline and Ansible deployment automation.

## Features
- Client registration and login with BCrypt-hashed passwords.
- View future open nutritionist slots.
- Request consultation and track PENDING / CONFIRMED / REJECTED / CANCELLED status.
- Cancellation subject to a 24-hour notice rule.
- Staff slot management and confirmation/rejection of pending requests.
- Transactional slot claiming with a database row lock to protect against duplicate requests.
- JSON API secured with stateless HTTP Basic authentication.
- PostgreSQL persistence, health endpoint, Docker, Jenkins and Ansible deployment files.

> **Demo safety:** Docker Compose activates the demo profile and seeds fixed accounts. Use only on a local/private learning environment. For deployment, disable demo seeding, use unique secrets, HTTPS, backups and managed credentials. This is a scheduling demo, not a medical-record system.

## 1. Requirements
For the easiest launch:
- Docker Desktop or Docker Engine with the Docker Compose plugin.
- Git.
- For Maven/tests directly: JDK 17 and Maven 3.9+.
- For Selenium tests: Docker Compose and permission to pull the Selenium Chrome image.

## 2. Run locally with Docker Compose (recommended)

Clone the repo. After the code PR is merged to main, the standard clone will contain the app; before merge, switch to the working branch shown below.

~~~bash
git clone https://github.com/Dhruv-D-Bhrasadiya/NutriSync-.git
cd NutriSync-
# Only before the PR is merged:
git switch docs/devops-documentation-baseline
cp .env.example .env
~~~

Edit `.env` and replace `POSTGRES_PASSWORD` with a unique random local password (Linux/macOS: generate with `openssl rand -hex 24`). Do not commit `.env`.

~~~bash
docker compose up --build -d
docker compose ps
docker compose logs -f app
~~~

Wait for the app to become healthy, then open **http://localhost:8080**. The app port is bound to loopback and PostgreSQL is not published on the host.

Health endpoint: **http://localhost:8080/actuator/health**

### Local demo accounts

| Role | Email | Password |
|---|---|---|
| Client | `client@nutrisync.local` | `NutriSyncDemo!2026` |
| Nutritionist | `nutritionist@nutrisync.local` | `NutriSyncDemo!2026` |
| Administrator | `admin@nutrisync.local` | `NutriSyncAdmin!2026` |

These known credentials exist only because Compose enables the demo seeder. Do not expose this demo stack publicly or reuse these passwords.

### Stop, inspect and reset

~~~bash
docker compose logs --tail=200 app
docker compose ps
docker compose down
~~~

To delete local PostgreSQL data, demo users and bookings as well, run `docker compose down -v`. **This is destructive.** Regular `docker compose down` preserves the named DB volume.

## 3. Run with Maven without Docker

Start PostgreSQL, create a database/user, and export settings first.

Linux/macOS:
~~~bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/nutrisync'
export SPRING_DATASOURCE_USERNAME='nutrisync'
export SPRING_DATASOURCE_PASSWORD='your-local-database-password'
export APP_TIME_ZONE='Asia/Kolkata'
mvn spring-boot:run
~~~

PowerShell:
~~~powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/nutrisync'
$env:SPRING_DATASOURCE_USERNAME = 'nutrisync'
$env:SPRING_DATASOURCE_PASSWORD = 'your-local-database-password'
$env:APP_TIME_ZONE = 'Asia/Kolkata'
mvn spring-boot:run
~~~

Demo users are seeded only when both `SPRING_PROFILES_ACTIVE=demo` and `APP_SEED_DEMO_DATA=true` are set. The default profile does not create known demo credentials.

## 4. Run tests

The standard tests use an H2 in-memory database and do not need external PostgreSQL.

~~~bash
mvn -B clean test
mvn -B clean verify
~~~

Reports are generated under `target/surefire-reports/`. Tests cover password hashing, duplicate registration, booking creation, duplicate slot-claim rejection and slot release after staff rejection.

## 5. Selenium browser journeys

Start the app, database and optional browser service:

~~~bash
docker compose --profile e2e up --build -d
docker compose --profile e2e ps
~~~

Then from the project root:

~~~bash
mvn -B -Pselenium-e2e verify \
  -De2e.enabled=true \
  -De2e.baseUrl=http://app:8080 \
  -De2e.remoteUrl=http://localhost:4444
~~~

The remote Chrome browser resolves the Compose service name `app`; Maven connects to Selenium through `localhost:4444`. The tests require the demo users/seeder because they exercise the browser workflow. Failure screenshots are saved under `target/selenium-failures/`.

When finished:

~~~bash
docker compose --profile e2e down
~~~

The database volume remains unless removed explicitly with `docker compose down -v`.

## 6. JSON API

Routes use `/api/v1`. Registration and reading open slots are public; protected routes use stateless HTTP Basic authentication, so send credentials on each protected request.

Get available slots:
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

Request a booking (replace slot ID with a future ID from the slots response):
~~~bash
curl --fail-with-body -u 'client@nutrisync.local:NutriSyncDemo!2026' \
  -H 'Content-Type: application/json' -X POST http://localhost:8080/api/v1/bookings \
  -d '{"slotId":1}'
~~~

Staff transitions are `PATCH /api/v1/bookings/{id}/confirm`, `/reject` and `/cancel`. Slot creation is `POST /api/v1/slots` with ISO instants, for example `{"startsAt":"2030-01-02T09:00:00Z","endsAt":"2030-01-02T09:45:00Z"}`; the authenticated nutritionist/admin becomes the owner.

## 7. Jenkins CI/CD

The root `Jenkinsfile` defines checkout, Maven verification, Docker image build, optional Selenium journeys via Compose, optional Docker Hub push and a Compose deployment stage on main.

Configure Jenkins Global Tools:
- JDK name `JDK17` (JDK 17).
- Maven name `Maven_3_9` (Maven 3.9+).
- Docker Engine/Compose available to the build agent.
- For image publishing, add username/password credential ID `nutrisync-dockerhub`; use a Docker Hub access token as the password.

The Docker Hub push parameter is disabled by default. Configure the credential before enabling it. The deployment example uses the agent host's local Compose stack for a controlled classroom demonstration, not production.

## 8. Ansible provisioning

See [Ansible deployment notes](ansible/README.md). Install Ansible and the Docker collection:

~~~bash
ansible-galaxy collection install -r ansible/collections/requirements.yml
cp ansible/inventory/dev.example.ini ansible/inventory/dev.ini
~~~

Edit ignored `ansible/inventory/dev.ini` for a disposable Ubuntu 22.04+ host. Export `NUTRISYNC_IMAGE` to an image tag the target can pull, and `NUTRISYNC_DB_PASSWORD` to a unique secret of at least 20 characters on the Ansible control node. Do not commit either value.

~~~bash
ansible -i ansible/inventory/dev.ini app -m ping
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml --syntax-check
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml --check --diff
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml
ansible-playbook -i ansible/inventory/dev.ini ansible/playbooks/site.yml
~~~

Inspect the second run for unintended changes. Roll back by setting `NUTRISYNC_IMAGE` to a previous known-good tag and rerunning the playbook. This is a learning-host runbook, not a deployment for sensitive health data.

## 9. Assignment documentation
- [Task 1 — problem and 15-task MVP](docs/01-problem-definition-and-mvp.md)
- [Task 2 — Agile and DevOps workflow](docs/02-agile-plan-and-devops-workflow.md)
- [Task 3 — SRS and architecture](docs/03-srs-architecture-and-api.md)
- [Task 4 — Git/GitHub workflow](docs/04-git-github-workflow.md)
- [Task 5 — feature development](docs/05-feature-development-playbook.md)
- [Task 6 — MVP acceptance and release](docs/06-mvp-acceptance-and-release.md)
- [Task 7 — Jenkins CI](docs/07-jenkins-ci-setup.md)
- [Task 8 — pipeline/deployment](docs/08-jenkins-pipeline-and-deployment.md)
- [Task 9 — Selenium plan](docs/09-selenium-test-plan.md)
- [Task 10 — continuous testing](docs/10-continuous-testing-in-jenkins.md)
- [Task 11 — Docker lifecycle](docs/11-docker-container-lifecycle.md)
- [Task 12 — Jenkins-Docker CD](docs/12-jenkins-docker-cd.md)
- [Task 13 — Ansible](docs/13-ansible-configuration-management.md)
- [Task 14 — provisioning/recovery](docs/14-provisioning-reliability-and-rollback.md)
- [Mermaid diagrams](docs/diagrams.md)
- [Evidence checklist](docs/evidence-checklist.md)

## Production gaps
This remains an educational MVP. Compose intentionally seeds fixed demo accounts; never use that profile in production. Email verification, password reset, rate limiting, audit trail, reminders, calendar integration, TLS termination, external secrets, production backups, monitoring, database migrations and compliance review are not implemented. Add them before exposing a real service publicly.

## License
No license has been selected yet.
