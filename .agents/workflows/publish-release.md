---
name: publish-release
description: Ship a version of the Android app — bump, build both APKs, PR (squash + delete branch), then GitHub Release with 2 APK assets.
---

# Workflow: publish-release

Preconditions: working tree reviewed (`git status`), previous release known via `gh release list`.

1. **Bump versions** — `app/build.gradle.kts` (`versionCode` +1, `versionName`) and `companion-codeoss/build.gradle.kts` (`versionCode` +1, same `versionName`).
2. **Build** — `.\gradlew.bat :app:assembleRelease :companion-codeoss:assembleRelease` (~12 min; R8 kotlin-metadata warnings are OK).
3. **Stage APKs at repo root** — copy to `vastavikLearning-vX.Y.Z.apk` and `vastavik-codeoss-extension.apk` (both git-ignored).
4. **Write notes** — `release_notes_vX.Y.Z.md` at repo root, following the exact structure of the previous release body (`gh release view <prev-tag> --json body --jq .body`), then `git add -A && git commit`.
5. **PR flow** — `git checkout -b release/vX.Y.Z` → `git push -u origin` → `gh pr create --title "<type>(<scope>): <summary>, vX.Y.Z" --body "<what changed + verification>"` → `gh pr merge --squash --delete-branch` (squash-merges `main` and deletes the branch in one step).
6. **Release** — `gh release create vX.Y.Z --target main --title "vX.Y.Z - <Short Feature Title>" --notes-file release_notes_vX.Y.Z.md vastavikLearning-vX.Y.Z.apk vastavik-codeoss-extension.apk`.
7. **Backend repo** — if endpoints were touched, commit/push `vastavikLearning-backend-app` (Render auto-deploys) and list required env vars in Upgrade Notes.
