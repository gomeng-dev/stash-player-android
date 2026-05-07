# Development

This document is for people building Stash Android Player from source.

## Build From Source

Install JDK 17 or newer and Android SDK 35, then run:

```bash
./gradlew --no-daemon :app:assembleDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/
```

## Signed Release Build

To build a signed release APK, provide these environment variables:

```bash
export ANDROID_SIGNING_KEYSTORE_BASE64="<base64 encoded keystore>"
export ANDROID_SIGNING_STORE_PASSWORD="<store password>"
export ANDROID_SIGNING_KEY_ALIAS="<key alias>"
export ANDROID_SIGNING_KEY_PASSWORD="<key password>"
./gradlew --no-daemon :app:assembleRelease
```

Do not commit keystores, signing credentials, APKs, AABs, or build outputs.

## Tech Stack

- Kotlin
- Jetpack Compose and Material 3
- AndroidX Navigation
- Media3/ExoPlayer
- Coil
- Room
- DataStore
- Retrofit, OkHttp, and Moshi

## Validation

Useful local checks:

```bash
./gradlew --no-daemon :app:testDebugUnitTest
./gradlew --no-daemon :app:assembleDebug
./gradlew --no-daemon :app:lintDebug
git diff --check
```
