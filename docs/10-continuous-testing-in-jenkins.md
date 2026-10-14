# Task 10 — Continuous Testing in Jenkins

## Pipeline gate
Recommended sequence:
1. Checkout.
2. Maven unit/integration verification.
3. Start/connect to isolated test environment.
4. Execute Selenium suite.
5. Publish JUnit XML even when tests fail.
6. Archive failure screenshots and diagnostics.
7. Package the artifact/image.
8. Deploy only if required gates are green.

Failed tests must return non-zero status. Do not ignore test exit codes or put deployment in an unconditional post action.

## Browser agent
Install a compatible browser and OS libraries or use a dedicated browser test container. Pin browser/Selenium compatibility. Ensure app URL is reachable from the agent. Remember that localhost inside a container refers to that container, not automatically the host.

## Reports and artifacts
Configure JUnit publishing for real XML paths, commonly target/surefire-reports/*.xml or target/failsafe-reports/*.xml. Archive real screenshot paths only after confirming where the test code writes files. Publish reports/screenshots even on failure.

## Controlled defect exercise
Use a temporary test branch and harmless reversible defect, such as a known display-label mismatch. Do not weaken security or alter real data.
1. Commit the controlled defect.
2. Run Jenkins; capture failed assertion and build status.
3. Verify deployment was skipped.
4. Fix the behavior and add/retain a regression assertion.
5. Commit fix and rerun.
6. Capture green report, fix SHA and build URL.
7. Keep the broken version out of the release branch.

## Conceptual Jenkins stage
~~~groovy
stage('Verify') {
  steps {
    sh './mvnw -B clean verify'
  }
  post {
    always {
      junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
      archiveArtifacts allowEmptyArchive: true,
        artifacts: 'target/selenium-failures/**/*.png'
    }
  }
}
stage('Deploy') {
  when { branch 'main' }
  steps {
    // Invoke the project's approved deployment procedure.
    sh './scripts/deploy.sh'
  }
}
~~~

Adapt paths and conditions to the actual pipeline. The deployment script shown is an example, not an existing file.

## Acceptance
- Selenium actually runs in Jenkins.
- JUnit reports visible in Jenkins UI.
- Deliberate test failure fails pipeline and skips deployment.
- Fix commit and green rerun recorded.
- Failure screenshot comes from the real run and contains no credentials.
