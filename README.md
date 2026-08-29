<div align="center">

# Stash Android Player

### Your Stash library, finally comfortable on Android.

A native Android client for people who already run Stash and want the phone experience to feel fast, private, and built for one hand.

<p>
  <a href="README.ko.md"><img alt="Korean README" src="https://img.shields.io/badge/README-%ED%95%9C%EA%B5%AD%EC%96%B4-7c3aed?style=for-the-badge"></a>
  <a href="README.zh-CN.md"><img alt="Simplified Chinese README" src="https://img.shields.io/badge/README-%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87-7c3aed?style=for-the-badge"></a>
  <a href="README.zh-TW.md"><img alt="Traditional Chinese README" src="https://img.shields.io/badge/README-%E7%B9%81%E9%AB%94%E4%B8%AD%E6%96%87-7c3aed?style=for-the-badge"></a>
  <a href="https://github.com/gomeng-dev/stash-player-android/releases"><img alt="Download APK" src="https://img.shields.io/badge/download-APK-34d399?style=for-the-badge&logo=github&logoColor=white"></a>
  <img alt="Android 10+" src="https://img.shields.io/badge/Android-10%2B-34d399?style=for-the-badge&logo=android&logoColor=white">
  <img alt="Kotlin + Compose" src="https://img.shields.io/badge/Kotlin%20%2B%20Compose-111827?style=for-the-badge&logo=kotlin&logoColor=white">
</p>

<img src="docs/assets/readme/screenshots/00-hero.png" alt="Stash Android Player preview showing Home, Explore, Queue, Player controls, Images, and Gallery appreciation mode" width="100%">

</div>

---

## Why this exists

Stash is great as a server. On a phone, the desktop web UI can feel like work.

**Stash Android Player** keeps your library on your own Stash server and gives the day-to-day browsing and watching flow a native Android shell: quick resume, proper bottom navigation, local lists, swipe-friendly discovery, and a player that understands phone gestures.

No hosted account. No cloud sync service. No metadata upload to this project.

## What you get

- **A home screen that starts where you left off.** Resume the latest scene, open your queue, jump to Watch Later, or start browsing without digging through menus.
- **Explore that works like a real app.** Search, filter, sort, shuffle, switch layouts, save filters, and batch-select scenes from one place.
- **Local lists for phone-side intent.** Queue, Watch Later, Favorites, recent playback, and Shorts feedback live on the device so you can organize your viewing flow without rewriting your whole Stash library.
- **A watch page around the video.** Metadata, rating, tags, actions, similar videos, and the player sit together instead of making you bounce between pages.
- **A gesture-first player.** Double-tap seek, horizontal scrub, side brightness/volume, long-press speed hold, lock mode, stream selection, subtitles, PiP, fullscreen, and playlist controls.
- **Gallery and image browsing.** Browse server-backed galleries and image folders, open the photo viewer, and use appreciation mode when you want the top and bottom chrome out of the way.
- **Recommendations when your server has them.** The app prefers the Stash Hybrid Recommendations Engine plugin and falls back to Stash's built-in recommendation data when the plugin is unavailable.

## Screenshots

Captured from the real Android app with public demo media. These images do not show private media, server addresses, API keys, credentials, cookies, or personal library data.

<p align="center">
  <img src="docs/assets/readme/screenshots/01-home.png" alt="Home screen with continue watching and library shortcuts" width="220">
  <img src="docs/assets/readme/screenshots/02-explore.png" alt="Explore screen with search, filters, and scene cards" width="220">
  <img src="docs/assets/readme/screenshots/03-shorts.png" alt="Shorts feed with vertical playback and local feedback" width="220">
</p>
<p align="center">
  <img src="docs/assets/readme/screenshots/04-queue.png" alt="Playback queue with Watch Later and local lists" width="220">
  <img src="docs/assets/readme/screenshots/05-watch-page.png" alt="Watch page with metadata, rating, tags, and actions" width="220">
  <img src="docs/assets/readme/screenshots/06-player-controls.png" alt="Player controls with seek bar, speed, lock, and fullscreen" width="220">
</p>
<p align="center">
  <img src="docs/assets/readme/screenshots/07-images.png" alt="Images browser with search, sort, and filters" width="220">
  <img src="docs/assets/readme/screenshots/08-photo-viewer.png" alt="Photo viewer appreciation mode with chrome hidden" width="220">
</p>

## The normal flow

### 1. Open Home and keep watching

Home is for the moment you open the app and already know what you want. Resume, play the queue, check Watch Later, or go straight into discovery.

### 2. Find something without fighting filters

Explore combines browsing and search. You can filter by tags, date, duration, rating, watched state, local favorite, resolution/file type, and more. Random sort and shuffle are there when you do not want to think too hard.

### 3. Build a queue for this session

Add scenes to Up Next, save items for later, or keep a local favorite list. The point is simple: your phone can have its own watching plan without forcing every tiny decision back into the server.

### 4. Watch with phone-native controls

The player is built around touch:

- tap to reveal or hide controls,
- double-tap to jump backward or forward,
- drag horizontally to scrub,
- swipe the sides for brightness and volume,
- long-press for temporary speed-up,
- lock the controls when you do not want accidental taps,
- switch streams, subtitles, aspect, orientation, and playlist items from the overlay.

### 5. Use recommendations when they help

If the Hybrid Recommendations plugin is installed on your Stash server, similar videos come from that engine. If not, the app still uses Stash fallback data instead of leaving the area empty.

### 6. Browse images without UI chrome

The Images tab supports search, sort, filters, and full-screen browsing. In the photo viewer, Appreciation mode hides the top bar and bottom tools after activation so the image can fill the moment without extra UI noise.

## Install

1. Open the [latest public release](https://github.com/gomeng-dev/stash-player-android/releases) on your Android phone.
2. Download the APK.
3. Open the APK and allow installs from your browser or file manager if Android asks.
4. Launch **Stash Player** and connect it to your Stash server.

Current public release: **v1.10.5**

## Requirements

- Android 10 or newer.
- A Stash server reachable from your phone, either on your LAN, VPN, Tailscale, or your own HTTPS reverse proxy.
- One authentication mode:
  - trusted local server without auth,
  - Stash API key,
  - Stash username/password login.
- Optional: [Stash Hybrid Recommendations](https://github.com/gomeng-dev/stash-recommendation-server) for richer similar-video recommendations.

## First connection

On first launch, enter your Stash server address and choose the matching auth method.

Common examples:

- `http://192.168.0.10:9999`
- `http://stash.local:9999`
- your own HTTPS reverse-proxy address

Tap **Test connection**. If it succeeds, save the server and the app opens Home.

## Privacy

This app is a direct client for your server.

- Server settings, API keys, session cookies, local lists, playback history, and Shorts feedback stay on the device.
- Password login stores the session material needed to refresh the session. Do not share device backups or debug logs publicly.
- A recent-apps privacy option can hide the app preview from Android recents.
- Debug logs redact sensitive auth material.
- If you share your own screenshots, check for real server URLs, filenames, credentials, and private media first.

## Troubleshooting

| Problem | Try this |
| --- | --- |
| Connection fails | Confirm the phone can reach your Stash URL on the same network, VPN, or reverse proxy. |
| Thumbnails do not load | Re-test the server connection and confirm the selected auth method still works in Stash. |
| Login stops working after restart | Re-enter the username/password from Settings so session refresh material is updated. |
| Shorts is empty | Check that your library has scenes shorter than the Shorts maximum duration in Settings. |
| Recommendations are empty | Enable the Hybrid Recommendations plugin, or let the app use Stash fallback recommendations. |
| APK install is blocked | Allow APK installs for the browser, file manager, or installer app that opened the APK. |

## For developers

This README is for people installing and using the app. Build instructions, project structure, signing notes, and verification commands live in [DEVELOPMENT.md](DEVELOPMENT.md).

Quick debug build:

```bash
./gradlew :app:assembleDebug
```

## License

MIT. See [LICENSE](LICENSE).
