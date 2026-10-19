**Prerequisite (user):** in GitHub → VerbeClair → Settings → Secrets and variables → Actions, add the repository secret `FRENCH_VERBS_DATASET_TOKEN` with the same token as `frenchVerbsDataset.githubToken` in `local.properties`.

## 1. Workflow

- [ ] 1.1 Create `.github/workflows/ci.yml` with `name: CI`, the triggers and concurrency of design D2, and `permissions: contents: read`. Verify: `actionlint` (or the GitHub workflow editor) reports no errors.
- [ ] 1.2 Add the `build` job (D1, D3, D5, D7): `macos-15`, checkout, setup-java zulu 21, setup-gradle (read-only cache on PRs), `~/.konan` cache, a step exporting `DEVELOPER_DIR` from `xcode-select -p` and printing the Xcode version, `./gradlew build --stacktrace` with `FRENCH_VERBS_DATASET_TOKEN` from secrets, and test reports uploaded on failure. Verify: the job runs green on the PR for this change.
- [ ] 1.3 Add the `android-device-tests` job (D1, D4, D5, D7): `ubuntu-latest`, checkout, setup-java zulu 21, setup-gradle, KVM udev rule, AVD snapshot cache, `reactivecircus/android-emulator-runner` (API 34, google_apis, x86_64, animations off, 600 s boot timeout) running `./gradlew connectedAndroidDeviceTest --stacktrace`, and the `androidTests` reports uploaded on failure. Verify: the job runs green on the PR and the log lists the instrumented tests of `:core:designsystem` and `:shared`.
- [x] 1.4 Pin every action to a full commit SHA with its version tag in a comment (D6). Verify: `grep -E 'uses: .+@[0-9a-f]{40}' .github/workflows/ci.yml` matches every `uses:` line.

## 2. Behaviour checks

- [ ] 2.1 Check cancel-in-progress. Verify: pushing a second commit to the PR while CI runs cancels the first run, and the new commit gets a fresh run.
- [ ] 2.2 Check that a failing test fails the right check. Verify: on a throwaway commit, a failing `commonTest` assertion in `:core:designsystem` turns both checks red, and the reports artifact is uploaded. Then drop the commit (never merged).
- [ ] 2.3 Check that secrets are not exposed. Verify: the workflow logs contain no token value (only `***` where the variable is printed), and no step writes `local.properties`.
- [ ] 2.4 Check the second run time. Verify: on a re-run, the Gradle cache, `~/.konan` and the AVD snapshot are restored. Record the run times of both jobs in the PR description.

## 3. Repository settings (user, after merge)

- [ ] 3.1 After the first green run on `dev`, mark `CI / Build (Gradle, all modules)` and `CI / Android device tests` as required status checks in the branch protection of `dev` and `main`. Verify: a test PR shows both as "Required", and the merge button stays disabled until they pass.

## 4. Done gate

- [ ] 4.1 Run `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew build` and `./gradlew connectedAndroidDeviceTest` locally (no app code changes, so this confirms nothing regressed). Verify: both succeed locally and both CI jobs are green on the PR into `dev`, with the outputs recorded.
