# Contributing to Stash Android Player

Thanks for helping improve Stash Android Player. This project is a native Android client for self-hosted Stash servers, so contributions should preserve user privacy, avoid leaking library data, and keep the phone experience fast and comfortable.

## Before you start

- Check the README and existing issues to avoid duplicate work.
- For larger UI, playback, networking, or storage changes, open an issue first so the scope can be discussed.
- Keep screenshots, logs, fixtures, and examples free of private media, real server URLs, API keys, cookies, filenames, and personal library data.

## Development setup

See [DEVELOPMENT.md](DEVELOPMENT.md) for the supported local build and validation commands.

Quick debug build:

```bash
./gradlew --no-daemon :app:assembleDebug
```

Useful checks before opening a pull request:

```bash
./gradlew --no-daemon :app:testDebugUnitTest
./gradlew --no-daemon :app:lintDebug
./gradlew --no-daemon :app:assembleDebug
git diff --check
```

## Contribution guidelines

### Privacy and security

- Do not commit API keys, cookies, passwords, signing keys, keystores, APKs, AABs, or generated build outputs.
- Redact server addresses, auth headers, session material, and private filenames from logs and screenshots.
- Use synthetic fixtures or public demo media when tests need example data.
- Keep authentication changes same-origin scoped; do not attach Stash credentials to arbitrary media URLs.

### Android UX

- Prefer native Android patterns over copying desktop Stash screens directly.
- Keep one-handed use and touch target size in mind.
- Avoid adding dead controls for features that are not implemented yet.
- Make text accessible and localizable where practical.

### Code style

- Keep changes focused and easy to review.
- Add or update tests for model, parser, persistence, and request-building behavior.
- Prefer small helper functions for reusable logic instead of duplicating behavior across screens.
- Avoid broad refactors in the same PR as a feature or bug fix unless the refactor is necessary.

## Pull requests

A good pull request includes:

- A short summary of what changed.
- Validation commands that were run, or a clear note if a check was not run.
- Screenshots or screen recordings for UI changes, using safe demo data only.
- Notes about privacy, auth, or migration impact when relevant.

By contributing, you agree that your contribution will be licensed under the repository's [MIT License](LICENSE).
