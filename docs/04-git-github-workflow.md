# Task 4 — Git and GitHub Repository Initialization

Repository: https://github.com/Dhruv-D-Bhrasadiya/NutriSync-

## Branch model
- main: release-ready only.
- develop: integration branch for active work.
- feature/short-description: product feature.
- bugfix/short-description: ordinary defect.
- hotfix/short-description: urgent release repair.
- docs/short-description: documentation.
- ci/short-description: pipeline work.
- test/short-description: test-only work.

Use lowercase kebab-case.

## Target structure
~~~text
.github/
  ISSUE_TEMPLATE/bug_report.md
  ISSUE_TEMPLATE/feature_request.md
  pull_request_template.md
  branching-and-commit-policy.md
docs/
src/main/java/
src/main/resources/
src/test/java/
ansible/
Dockerfile
Jenkinsfile
pom.xml
~~~

Application/infrastructure paths are target locations, not claims that those files currently exist.

## Local setup
~~~bash
git clone https://github.com/Dhruv-D-Bhrasadiya/NutriSync-.git
cd NutriSync-
git switch -c docs/project-foundation
git status
~~~

Create develop only if it does not exist remotely:
~~~bash
git switch main
git pull --ff-only origin main
git switch -c develop
git push -u origin develop
~~~

If develop already exists, fetch and switch to it instead of recreating it.

## Commit messages
Use type(scope): imperative summary. Examples:
- docs(mvp): define problem and acceptance scope
- feat(auth): add client registration validation
- test(booking): reject a second active request for a slot
- ci(jenkins): publish Maven test reports
- fix(slots): prevent overlapping availability

Keep commits focused. Review staged files before committing.

## PR workflow
1. Sync with develop and create a branch.
2. Implement one bounded change and run relevant tests.
3. Run git diff --check, git status and inspect the complete diff.
4. Push branch and open PR into develop.
5. Explain purpose, verification, risks and real evidence.
6. Review changed files and acceptance criteria; address comments.
7. Merge only after required checks pass.
8. Delete stale feature branches and update local develop.
9. Release by reviewed PR from develop to main and tag a tested commit.

## Issues and board
Use included feature and bug templates. Each product issue should reference P01–P15 and include acceptance criteria, dependencies, test plan and expected evidence. Suggested labels: type:feature, type:bug, type:docs, area:app, area:ci-cd, area:testing, area:infra, priority:p1, priority:p2, blocked. Create 15 product issues from Task 1 and separate DevOps task issues. Suggested board columns: Backlog, Ready, In Progress, Review, Blocked and Done.

## Hygiene
Ignore build output, logs, IDE metadata, local environment files and secrets. Do not commit .env, private keys, tokens, database dumps or personal/clinical data. Save sanitized evidence and reference CI URLs where possible. Add a license only after choosing reuse terms.
