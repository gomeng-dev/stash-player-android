<div align="center">

# Stash Android Player

### Your self-hosted Stash library, made comfortable on Android.

Browse, search, queue, swipe through Shorts, and watch with a gesture-first native player — while your library stays on your own Stash server.

<p>
  <a href="README.ko.md"><img alt="Korean README" src="https://img.shields.io/badge/README-%ED%95%9C%EA%B5%AD%EC%96%B4-7c3aed?style=for-the-badge"></a>
  <a href="README.zh-CN.md"><img alt="Simplified Chinese README" src="https://img.shields.io/badge/README-%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87-7c3aed?style=for-the-badge"></a>
  <a href="README.zh-TW.md"><img alt="Traditional Chinese README" src="https://img.shields.io/badge/README-%E7%B9%81%E9%AB%94%E4%B8%AD%E6%96%87-7c3aed?style=for-the-badge"></a>
  <a href="https://github.com/gomeng-dev/stash-player-android/releases"><img alt="Download APK" src="https://img.shields.io/badge/download-APK-34d399?style=for-the-badge&logo=github&logoColor=white"></a>
  <img alt="Android 10+" src="https://img.shields.io/badge/Android-10%2B-34d399?style=for-the-badge&logo=android&logoColor=white">
  <img alt="Kotlin + Compose" src="https://img.shields.io/badge/Kotlin%20%2B%20Compose-111827?style=for-the-badge&logo=kotlin&logoColor=white">
</p>

</div>

---

## Why use it?

Stash is powerful, but its desktop web UI is not always the easiest way to browse and watch from a phone. **Stash Android Player** gives your self-hosted library a native Android experience:

- **Home** for continuing, opening your local lists, and jumping back into watching.
- **Explore** for search, filters, sort, random/shuffle, saved filters, and bulk actions in one place.
- **Shorts** for a vertical feed of short scenes with local feedback.
- **Queue / Watch Later / Favorites** stored locally on your device.
- **Watch page + Media3 player** with gestures, fullscreen, stream choices, subtitles, PiP, and playlist controls.
- **Recommendations** through the Stash Hybrid Recommendations plugin when available, with Stash fallback data when it is not.

No hosted service is required. The app connects directly to your Stash server and does not upload your library metadata to this project.

## Screenshots

The screenshots below were captured from the real Android app with public demo media. They do not show private media, credentials, server addresses, API keys, or user data.

<p align="center">
  <img src="docs/assets/readme/screenshots/01-home.png" alt="Home screen with continue watching and library shortcuts" width="220">
  <img src="docs/assets/readme/screenshots/02-explore.png" alt="Explore screen with search, filters, and scene cards" width="220">
  <img src="docs/assets/readme/screenshots/03-shorts.png" alt="Shorts vertical feed with feedback controls" width="220">
</p>
<p align="center">
  <img src="docs/assets/readme/screenshots/04-queue.png" alt="Playback queue with Watch Later and local lists" width="220">
  <img src="docs/assets/readme/screenshots/05-watch-page.png" alt="Watch page with metadata, rating, tags, and actions" width="220">
  <img src="docs/assets/readme/screenshots/06-player-controls.png" alt="Player controls with seek bar, speed, lock, and fullscreen" width="220">
</p>

| Screen | What it is for |
| --- | --- |
| **Home** | Continue watching, open local lists, and start playback quickly. |
| **Explore** | Search or browse the whole library with the same filters and bulk tools. |
| **Shorts** | Swipe through short scenes and give local feedback. |
| **Queue** | Manage Up Next, Watch Later, Favorites, and recent playback. |
| **Watch page** | See metadata, rating, tags, actions, and similar videos around the player. |
| **Player controls** | Use seek, speed, stream, fullscreen, lock, previous/next, and aspect controls. |

## Install

1. Open the [latest release](https://github.com/gomeng-dev/stash-player-android/releases) on your Android phone.
2. Download the APK.
3. Open the APK and allow installs from your browser or file manager if Android asks.
4. Launch **Stash Player** and connect it to your Stash server.

> Current public release: **v1.7.0**.

## What you need

- Android 10 or newer.
- A Stash server reachable from your phone.
- One connection method:
  - trusted local server without authentication,
  - Stash API key,
  - Stash username/password login.
- Optional: Stash Hybrid Recommendations Engine plugin for richer similar-video recommendations.

## First connection

On first launch, enter your Stash server address and choose the authentication method that matches your Stash setup.

Common examples:

- `http://192.168.0.10:9999`
- `http://stash.local:9999`
- your own HTTPS reverse-proxy address

Tap **Test connection**. If it succeeds, save the server and the app opens Home.

## Main features

### Home

Home is the fast start screen: continue your latest scene, open Explore or Shorts, jump into Queue / Watch Later / Favorites, and see app update status.

### Explore

Explore combines browsing and searching. Whether you enter a search term or just browse the library, you can use:

- tags, date, duration, rating, media quality, and watched filters,
- local Favorites and saved filters,
- random sort and shuffle again,
- grid/list display,
- multi-select actions such as play selection, add to queue, and delete.

### Shorts

Shorts is a vertical feed for short scenes. It preloads nearby items for smoother swiping and keeps feedback on the device.

Useful gestures:

- tap to play or pause,
- swipe for the next item,
- double-tap to like,
- long-press for temporary 1.5x playback,
- drag the seek row for precise scrubbing,
- mark Not Interested or delete a feed item.

### Watch page and player

The watch page keeps the video, scene actions, metadata, and recommendations together. The player supports fullscreen, portrait watch-page mode, double-tap seek, horizontal scrub, side brightness/volume gestures, long-press speed hold, lock mode, stream selection, subtitles, PiP, orientation controls, and playlist navigation.

### Local lists

These lists are stored on your Android device for phone-side convenience:

- **Queue** — what you want to watch now.
- **Watch Later** — scenes saved for another time.
- **Favorites** — app-local favorite marks.
- **Playback history** — local resume/history signals for Home and Queue.

## Recommendations

If the Stash Hybrid Recommendations Engine plugin is installed and enabled, the app uses it as the primary recommendation source. If the plugin is missing, offline, or returns no usable result, the app falls back to Stash's built-in recommendation data.

A separate legacy recommendation HTTP server is not required for the default path.

## Privacy

Stash Android Player is a direct client for your own server.

- Server settings, API keys, session cookies, local lists, history, and Shorts feedback stay on the device.
- Password login stores the session material needed to refresh the session; do not share device backups or logs publicly.
- A recent-apps privacy option can hide the app preview from Android recents.
- Debug logs redact sensitive auth material.
- Be careful when sharing your own screenshots: avoid showing real server addresses, filenames, credentials, or private media.

## Troubleshooting

| Problem | Try this |
| --- | --- |
| Connection fails | Confirm the phone can reach your Stash URL on the same network, VPN, or reverse proxy. |
| Thumbnails do not load | Re-test the server connection and confirm the selected auth method still works in Stash. |
| Login stops working after restart | Re-enter the username/password from Settings so session refresh material is updated. |
| Shorts is empty | Check that your library has scenes shorter than the Shorts maximum duration in Settings. |
| Recommendations are empty | Enable the Hybrid Recommendations plugin, or let the app use Stash fallback recommendations. |
| APK install is blocked | Allow APK installs for the browser, file manager, or installer app that opened the APK. |

## Developers

End-user installation and usage are above. Build instructions, project structure, signing notes, and verification commands live in [DEVELOPMENT.md](DEVELOPMENT.md).

Quick debug build:

```bash
./gradlew :app:assembleDebug
```

## License

MIT. See [LICENSE](LICENSE).
