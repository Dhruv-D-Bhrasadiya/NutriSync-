# Task 8 — Pipeline as Code and Server Deployment

## Target topology
GitHub → Jenkins agent → Maven build/test → versioned JAR/image → Linux host. Nginx serves browser traffic and proxies to Spring Boot. The executable JAR includes embedded Tomcat.

## Jenkinsfile stages
- Checkout exact commit.
- Verify/build using ./mvnw -B clean verify.
- Package executable JAR; use actual artifact name after pom.xml exists.
- Publish JUnit reports and archive artifacts.
- Deploy only from approved branch/tag after tests pass.
- Poll health endpoint with retry/timeout; fail if readiness is not reached.
- Publish outcome without printing secrets.

## Environment parameterization
At least one value must be configurable, such as DEPLOY_ENV (dev/staging), APP_PORT, APP_BASE_URL or IMAGE_TAG. Validate values with an allowlist. Do not allow parameters to become arbitrary shell commands or paths. Keep passwords, SSH keys and registry tokens in Jenkins Credentials.

## Deployment options
For the first pass, deploy the executable JAR as a systemd service on a disposable Ubuntu VM. For Tasks 11–12 evolve deployment to Docker. Alternatively deploy a container from the start, but still archive the Maven artifact for build evidence.

For a JAR deployment, create a non-root service user, versioned release directory, protected environment file, systemd unit and Nginx reverse proxy. Keep the old release until the candidate passes health checks. For Docker, use private database networking and persistent PostgreSQL storage; expose only intended app traffic through Nginx.

## Implementation requirements
- Declarative Pipeline with defined agent, timeout and build retention.
- Shell commands propagate failures.
- Reports publish even after tests fail.
- Artifact is archived only after successful verification.
- Deployment is restricted to approved branch/tag.
- Credentials are injected only in stages that need them.
- Health check is bounded; workspace cleanup does not delete host rollback files.

This guide does not claim that a Jenkinsfile or deployment script already exists; implement them against the actual application.

## Verification
1. Confirm process/container is active.
2. Inspect logs for startup errors.
3. Call health endpoint on the host.
4. Access Nginx URL from the expected client network.
5. If application is ready, register a synthetic client and create a test booking.
6. Confirm failed tests skip deploy.
7. Record artifact tag, source SHA, environment, health output and screenshot.

## Done criteria
A checked-in Jenkinsfile, successful real pipeline run, parameter evidence and an app reachable at its actual URL. Never invent a URL or screenshot.
