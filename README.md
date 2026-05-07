# Stash Android Player

Stash Android Player is a native Android client for
[Stash](https://github.com/stashapp/stash), focused on watching and organizing
your self-hosted Stash library from a phone or tablet.

The app is built for a direct connection to your own Stash server. It does not
include a hosted backend service, cloud sync, or third-party recommendation
server.

## What It Does

- Connects to your Stash server with an API key or a login session.
- Browses and searches your scene library with a mobile-first interface.
- Plays scenes with Media3/ExoPlayer.
- Provides fullscreen player controls inspired by MX Player-style gestures.
- Supports tap controls, double-tap seeking, horizontal seek, side
  brightness/volume controls, long-press speed hold, and lock mode.
- Keeps local favorites, Watch Later, and playback queue lists on the device.
- Shows similar videos using the Stash Hybrid Recommendations Engine plugin
  when available, with Stash GraphQL recommendations as a fallback.
- Uses a dark, thumbnail-focused Jetpack Compose UI.

## Install

Download the latest APK from
[GitHub Releases](https://github.com/gomeng-dev/stash-player-android/releases).

Android may ask you to allow installs from your browser or file manager before
installing a downloaded APK.

## Requirements

- Android 10 or newer.
- A reachable Stash server.
- Stash API access, or a Stash login session, when your server requires
  authentication.

## Privacy

Your Stash server URL, API key, and session data stay on your device. The app
does not send your library data to a hosted service controlled by this project.

Treat your Stash credentials as private. Avoid sharing screenshots, logs, or
screen recordings that expose your server URL, API key, or session cookies.

## Help And Updates

- Releases: https://github.com/gomeng-dev/stash-player-android/releases
- Issues: https://github.com/gomeng-dev/stash-player-android/issues

## Development

Build instructions, stack details, and signing notes are in
[DEVELOPMENT.md](DEVELOPMENT.md).

## License

MIT. See [LICENSE](LICENSE).
