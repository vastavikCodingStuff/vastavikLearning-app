---
trigger: always_on
description: PR and GitHub Release conventions for this Android app (branching, squash merge, version bump, release notes format, APK assets).
---

## PR & release rules (vastavikLearning-app)

Rules:
- Never commit directly to `main`. Create a short-lived branch (e.g. `release/vX.Y.Z` or `fix/<topic>`), push it, open a PR with `gh pr create`, then **squash-merge** it (`gh pr merge --squash --delete-branch`) so `main` stays one commit per feature with title `<type>(<scope>): <summary>, vX.Y.Z (#N)` — matching `git log --oneline`.
- After squash-merge, delete the branch (the `--delete-branch` flag does it); do not keep merged branches around.
- Every user-facing release bumps **both** modules in lockstep: `app/build.gradle.kts` (`versionCode` +1, `versionName X.Y.Z`) and `companion-codeoss/build.gradle.kts` (`versionCode` +1, same `versionName X.Y.Z`).
- Release notes live only on GitHub Releases, never in git: write `release_notes_vX.Y.Z.md` at the repo root (git-ignored via `RELEASE_NOTES_*.md`) and pass it to `gh release create --notes-file`.
- Release title format is exactly `vX.Y.Z - <Short Feature Title>` (see `gh release list`).
- Release body format (match the previous 2-3 releases, e.g. `gh release view v1.0.61`):
  1. `# Vastavik Learning vX.Y.Z - Release Notes` + welcome paragraph,
  2. `---`, `## What's New and Improved` with numbered `### N.` subsections and bold lead-ins,
  3. `---`, `## Release Assets` markdown table (both APKs + sizes),
  4. `---`, `## Upgrade Notes` bullets,
  5. footer `*Built with love by the Vastavik Learning Team*`.
- Exactly two APK assets per release, copied from build outputs to the repo root before upload:
  - `vastavikLearning-vX.Y.Z.apk` ← `app/build/outputs/apk/release/app-release.apk`
  - `vastavik-codeoss-extension.apk` ← `companion-codeoss/build/outputs/apk/release/companion-codeoss-release.apk`
- Root `*.apk` files are git-ignored (`apk/` too) — build them fresh each release, upload with `gh release create ... <apk1> <apk2>`, never commit them. The only tracked APKs are the legacy in-app-update assets under `app/src/main/assets/`.
- Verify before releasing: `.\gradlew.bat :app:assembleDebug` (or `assembleRelease`) must pass; release builds run R8 (`isMinifyEnabled = true`) — R8 "parsing kotlin metadata" warnings are non-fatal.
- Backend changes live in the sibling repo `vastavikLearning-backend-app`; check `git status` there too and push before announcing a feature that needs new endpoints.
