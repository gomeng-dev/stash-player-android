# Security Policy

## Supported versions

Security fixes are handled on the latest public release line and the `main` branch. If you are using an older APK, please update to the latest release before reporting an issue unless the vulnerability only exists in the latest build.

## Reporting a vulnerability

Please do not report security vulnerabilities in public issues.

Use GitHub's private vulnerability reporting or security advisory flow for this repository when available. If that is not available, open a minimal public issue asking for a private contact path, but do not include exploit details, credentials, private server URLs, screenshots, cookies, API keys, or personal library data.

When reporting, include only what is needed to reproduce safely:

- App version and Android version.
- Stash server version if relevant.
- A concise description of the affected feature.
- Safe reproduction steps using dummy data or redacted logs.
- Whether the issue affects authentication, credential storage, media URL handling, local data, or network transport.

## Privacy-sensitive data

This project is a client for personal Stash servers. Never attach:

- private media,
- real library screenshots,
- server URLs that identify your deployment,
- API keys, cookies, passwords, or session material,
- signing keys or keystores,
- unredacted logs containing filenames, paths, or headers.

## Scope

Security reports that are useful for this project include:

- credential leakage or unsafe auth header forwarding,
- insecure storage of server credentials or session data,
- unsafe URL rewriting or thumbnail/media loading behavior,
- privacy leaks in logs, screenshots, backups, or crash output,
- vulnerabilities in exported Android components,
- dependency issues that are reachable in the app.

Reports about a user's own Stash server configuration, reverse proxy setup, or network exposure may be outside this app's scope unless the app contributes to the risk.
