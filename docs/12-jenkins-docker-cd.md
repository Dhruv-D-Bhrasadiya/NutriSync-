# Task 12 — Jenkins-to-Docker Continuous Deployment

## Goal
A tested commit creates a versioned Docker image, publishes it to Docker Hub or a local registry and deploys a fresh container. Every deployment must be traceable to a source commit.

## Versioning
Use an immutable release tag or short Git commit SHA, for example DOCKERHUB_USER/nutrisync:abc1234. Keep latest optional; never rely on it as the sole rollback reference. Record commit SHA, Jenkins build number, image tag/digest, test status and environment.

## Pipeline stages
1. Checkout exact commit.
2. Run ./mvnw -B clean verify.
3. Build the image from the tested tree.
4. Scan dependencies/image if available and document limitations.
5. Authenticate through Jenkins Credentials.
6. Push versioned image and record digest.
7. Deploy only from an approved branch/tag after tests pass.
8. Start fresh container with target config and persistent DB connection.
9. Wait for health readiness; fail if not healthy.
10. Retain the previous known-good image and deployment metadata.

## Credentials and configuration
Create a least-privilege registry token and store it in Jenkins Credentials. Bind it only in the push stage. Store deployment SSH access and trusted host keys securely. Never print environment variables or enable shell tracing around secrets.

Make registry repository, environment, app port and target group configurable with allowlisted values. Keep environment-specific settings outside the image.

## Deployment strategy
For a single host, pull the new image and start a candidate on the configured network. Wait for health checks, switch Nginx/upstream if using blue-green deployment, then retire the old container. A stop-and-recreate approach is simpler but causes downtime; document the trade-off if chosen.

Do not delete the old version until the new version is healthy and rollback has been verified. Image rollback does not reverse a destructive database migration.

## Acceptance
- Versioned image built from a tested commit.
- Registry push succeeds and image is visible.
- Tag/digest and commit recorded without secret leakage.
- Failed tests skip deployment.
- Pipeline starts a fresh container automatically.
- Health check passes; failed health preserves known-good version.
