# Stash Android Player

Native Android media player for [Stash](https://github.com/stashapp/stash),
built with Kotlin, Jetpack Compose, and Media3/ExoPlayer.

## Features

- Connect to a self-hosted Stash server with URL and API key support.
- Browse, search, queue, and play Stash scenes from a native Android interface.
- Media3/ExoPlayer playback with fullscreen-first player controls.
- MX Player-style gesture direction, including tap controls, seeking, side
  controls, long-press speed hold, lock mode, and watch-page playback.
- Local queue, watch-later style state, favorites, and playback progress.
- Dark thumbnail-first UI built with Jetpack Compose and Material 3.

## Install

Download the latest APK from
[GitHub Releases](https://github.com/gomeng-dev/stash-player-android/releases).

Android may ask you to allow installs from your browser or file manager before
installing a downloaded APK.

## Requirements

- Android 10 or newer.
- A reachable Stash server.
- Stash API access when your server requires authentication.

The app connects directly to the Stash server you configure. Server URL and API
key settings are stored locally on the device.

Android application ID: `gomeng.dev.stashplayer`.

## Build From Source

Install JDK 17 or newer and Android SDK 35, then run:

```bash
./gradlew --no-daemon :app:assembleDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/
```

To build a signed release APK, provide these environment variables:

```bash
export ANDROID_SIGNING_KEYSTORE_BASE64="<base64 encoded keystore>"
export ANDROID_SIGNING_STORE_PASSWORD="<store password>"
export ANDROID_SIGNING_KEY_ALIAS="<key alias>"
export ANDROID_SIGNING_KEY_PASSWORD="<key password>"
./gradlew --no-daemon :app:assembleRelease
```

## Tech Stack

- Kotlin
- Jetpack Compose and Material 3
- AndroidX Navigation
- Media3/ExoPlayer
- Coil
- Room
- DataStore
- Retrofit, OkHttp, and Moshi

## Privacy And Security

This app is a client for your own Stash server. It does not include a hosted
backend service. Treat your Stash URL and API key as private credentials, and
avoid sharing logs or screenshots that expose them.

## License

MIT. See [LICENSE](LICENSE).
