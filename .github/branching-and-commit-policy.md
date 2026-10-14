# Branching and Commit Policy

## Branches
- main: release-ready; merge via PR.
- develop: integration branch during the assignment.
- feature/short-description: product feature.
- bugfix/short-description: ordinary defect.
- hotfix/short-description: urgent release fix.
- docs/short-description: documentation.
- ci/short-description: pipeline work.
- test/short-description: test-only work.

Use lowercase kebab-case.

## Commit messages
Format: type(scope): imperative summary. Example: feat(booking): reject duplicate slot requests. Keep commits focused and review staged content before committing.

## Pull requests
Target develop for normal work and main for a reviewed release. Include purpose, related issue, tests actually run, risks and evidence. Address feedback before merge. For solo work, disclose self-review rather than fabricating a second reviewer.

## Release
Merge an accepted release into main, rerun verification, tag the tested commit and retain the previous known-good tag/image for recovery.

## Security
Keep .env files, credentials, tokens, private keys, sessions, database dumps and real personal/clinical data out of Git. If a secret is committed, rotate/revoke it; deleting a line later does not erase Git history.
