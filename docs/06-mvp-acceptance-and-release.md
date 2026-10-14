# Task 6 — MVP Completion, Git Collaboration and Release Baseline

## MVP verification matrix
Complete and test P01–P15 from Task 1. Use synthetic client and nutritionist accounts.

| Area | Required check |
|---|---|
| Registration/auth | Valid registration; duplicate email rejected; login/logout work |
| Availability | Future open slots sorted; unauthorized slot creation denied |
| Booking | Request starts PENDING; unavailable slot returns conflict |
| Concurrency | Two attempts for one slot produce at most one active booking |
| Decision | Authorized staff confirm/reject pending request |
| Ownership | Client sees own bookings only; guessed ID does not expose another user |
| Cancellation | Eligible booking cancels; restricted cancellation rejected; slot release consistent |
| Persistence | Data survives app restart in PostgreSQL |
| Validation | Readable errors; no stack traces/secrets exposed |
| Health/build | Health route succeeds; clean Maven verification passes |

## Conflict creation and resolution exercise
Use a lab-only documentation file/branch so application code is not damaged.

~~~bash
git switch develop
git pull --ff-only origin develop
git switch -c lab/conflict-a
# Edit a harmless line in docs/git-conflict-lab.md
git add docs/git-conflict-lab.md
git commit -m "docs(lab): add first conflict variant"
git push -u origin lab/conflict-a
git switch develop
git switch -c lab/conflict-b
# Change the same line to a different value
git add docs/git-conflict-lab.md
git commit -m "docs(lab): add second conflict variant"
git push -u origin lab/conflict-b
~~~

Merge the first branch, then merge/rebase the second onto the updated develop to trigger a conflict. Preserve intended content and remove every conflict marker.

~~~bash
git status
git diff --check
git add docs/git-conflict-lab.md
git commit -m "docs(lab): resolve merge conflict"
git log --oneline --graph --decorate -10
~~~

Capture real before/after evidence and resolution commit. Delete or label the lab file once evidence is saved.

## Release candidate tag
Only tag a commit that passed verification.

~~~bash
git switch main
git pull --ff-only origin main
git tag -a v0.1.0 -m "NutriSync MVP release candidate"
git push origin v0.1.0
git show --stat --oneline v0.1.0
~~~

If features/tests are incomplete, use a pre-release tag and list limitations. Never tag an untested commit just to obtain evidence.

## Release checklist
- [ ] P01–P15 pass or limitations are recorded.
- [ ] No secrets or real clinical data.
- [ ] Maven and Selenium verification pass.
- [ ] PR and conflict-resolution evidence visible.
- [ ] Tag points at tested commit.
- [ ] Docs match actual behavior.
- [ ] Previous known-good tag/image retained.
- [ ] Evidence checklist has actual results.
