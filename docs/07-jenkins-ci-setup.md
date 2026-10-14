# Task 7 — Jenkins Installation and Continuous Integration

This is a runbook. Mark the task complete only after Jenkins is installed and the real job has run.

## Install safely
Use a local Linux VM, WSL2-based Linux environment or disposable Ubuntu VM. Keep Jenkins private while learning; never expose an unauthenticated controller publicly. Follow the official Jenkins install instructions for the selected OS and current LTS release because supported Java runtime requirements can change.

1. Install the Java runtime supported by that Jenkins LTS and JDK 17 for the application build.
2. Configure the official Jenkins package repository and install LTS.
3. Enable/start the Jenkins service.
4. Read the initial setup secret locally and complete the wizard.
5. Create a unique administrator account and keep authorization enabled.
6. Install Git, Pipeline, JUnit and artifact-related plugins as required.
7. Check disk space, time and update policy.

Never paste passwords or tokens into repository docs/screenshots.

## Configure tools
Prefer Maven installation so the Maven version is pinned by the project. Ensure the agent can run Git and download dependencies. On Linux, the wrapper must be executable; on Windows, use mvn. Configure JDK/tool paths where needed.

## Create the CI job
Create a Pipeline job named nutrisync-ci:
- Definition: Pipeline script from SCM.
- Repository: https://github.com/Dhruv-D-Bhrasadiya/NutriSync-.git
- Branch: */develop when develop exists; otherwise the active working branch.
- Script path: Jenkinsfile.
- Trigger: GitHub webhook after configuration, or Poll SCM such as H/5 * * * * for a local classroom setup.
- Keep a bounded number of builds/artifacts.

For private repository access, use a least-privilege credential in Jenkins Credentials. Never embed a token in the URL or Jenkinsfile.

## Expected CI stages
1. Checkout.
2. Print non-sensitive tool versions.
3. Run Maven clean verification.
4. Publish test XML reports.
5. Archive versioned JAR after successful packaging.

A non-zero Maven result must fail the pipeline. Do not hide errors with commands that ignore exit status.

## Trigger verification
A webhook needs a secured callback URL reachable from GitHub. A localhost-only Jenkins instance normally cannot receive callbacks; use Poll SCM while local. If a tunnel is allowed, secure it and never expose Jenkins without authentication/TLS.

## Evidence and acceptance
Capture actual job URL/build number, repo/branch config, webhook delivery or polling trigger evidence, successful console excerpt, test report and artifact name. Redact secrets. Task 7 is not complete until the real job runs.

## Troubleshooting
- Missing tools: check java -version and mvn -version on the agent.
- Wrapper permission denied: commit executable mode for mvnw.
- Download failure: verify network/proxy and Maven access.
- No tests found: check test path, naming, dependencies and report path.
- Webhook absent: inspect GitHub delivery history, endpoint, plugin and trigger settings.
