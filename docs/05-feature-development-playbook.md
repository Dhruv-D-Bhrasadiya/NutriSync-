# Task 5 — Feature Branch, Pull Request and Merge Practice

## Goal
Demonstrate a complete collaboration cycle for one vertical slice: the client views open slots and requests one. If the application skeleton is not ready, create it as a separate reviewed change first.

## Create branch
~~~bash
git switch develop
git pull --ff-only origin develop
git switch -c feature/booking-request
~~~

## Implement the vertical slice
- Booking entity/DTO and repository.
- Transactional service validation for user, future slot, availability and safe slot claiming.
- Controller/API route and UI success/error feedback.
- PostgreSQL persistence.
- Tests for valid request, invalid slot and occupied slot.
- Updated SRS/API docs if behavior changes.

Avoid a UI-only button with no persistence. Role/ownership checks and double-booking prevention belong server/database side.

## Git evidence
~~~bash
git status
git diff --check
git add src docs
git commit -m "feat(booking): add consultation booking request"
git log --oneline --decorate -5
git push -u origin feature/booking-request
~~~

Adjust paths to actual files and inspect staged content first.

## PR content
Include the problem, changes, exact test commands and results, concurrency risks, and real report/screenshot URLs. Use the PR template. Review the diff for authorization gaps, null handling, error paths, secrets and unrelated files.

## Merge and verify
After review and passing checks:
~~~bash
git switch develop
git pull --ff-only origin develop
git log --oneline --decorate -10
~~~

Verify the merged feature on develop, rerun relevant tests, and record branch, PR URL, commit SHA, test output and screenshots in evidence-checklist.md.

## Evidence required
- Remote feature branch and meaningful commit.
- PR description and truthful review/self-review record.
- CI/test result and merge status.
- Actual UI/integration test showing the feature works.

Do not fabricate screenshots or claim approval from someone who did not review.
