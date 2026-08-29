<div align="center">

# Stash Android Player

### 讓自架 Stash 媒體庫在 Android 上更順手。

在手機上瀏覽、搜尋、排隊、滑動短影音，並使用以手勢為核心的原生播放器觀看內容，同時媒體庫仍保留在你自己的 Stash 伺服器上。

<p>
  <a href="README.md"><img alt="English README" src="https://img.shields.io/badge/README-English-7c3aed?style=for-the-badge"></a>
  <a href="README.ko.md"><img alt="Korean README" src="https://img.shields.io/badge/README-%ED%95%9C%EA%B5%AD%EC%96%B4-7c3aed?style=for-the-badge"></a>
  <a href="README.zh-CN.md"><img alt="Simplified Chinese README" src="https://img.shields.io/badge/README-%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87-7c3aed?style=for-the-badge"></a>
  <a href="https://github.com/gomeng-dev/stash-player-android/releases"><img alt="下載 APK" src="https://img.shields.io/badge/download-APK-34d399?style=for-the-badge&logo=github&logoColor=white"></a>
  <img alt="Android 10+" src="https://img.shields.io/badge/Android-10%2B-34d399?style=for-the-badge&logo=android&logoColor=white">
  <img alt="Kotlin + Compose" src="https://img.shields.io/badge/Kotlin%20%2B%20Compose-111827?style=for-the-badge&logo=kotlin&logoColor=white">
</p>

<img src="docs/assets/readme/screenshots/00-hero.png" alt="Stash Android Player 預覽，顯示首頁、探索、佇列、播放器控制、圖片和圖庫欣賞模式" width="100%">

</div>

---

## 為什麼使用它？

Stash 很強大，但桌面 Web UI 在手機上不一定順手。**Stash Android Player** 為你的自架媒體庫提供原生 Android 體驗：

- **首頁**：繼續觀看、開啟本機清單，並快速開始播放。
- **探索**：在同一個畫面搜尋、篩選、排序、隨機/洗牌、使用已儲存篩選器和批次操作。
- **短影音**：以直向動態瀏覽較短的影片。
- **佇列 / 稍後觀看 / 收藏**：本機儲存在裝置上。
- **觀看頁 + Media3 播放器**：支援手勢、全螢幕、串流選擇、字幕、PiP 和播放清單控制。
- **推薦**：優先使用 Stash Hybrid Recommendations 外掛；不可用時回退到 Stash 預設推薦資料。
- **圖庫**：瀏覽 Stash 圖庫和圖片，支援全螢幕照片檢視器；欣賞模式會隱藏頂部列和底部工具。

不需要託管服務。應用程式會直接連線到你的 Stash 伺服器，不會把媒體庫中繼資料上傳到本專案。

## 截圖

以下截圖來自使用公開示範媒體執行的真實 Android 應用程式，不包含私人媒體、伺服器位址、API Key、憑證、Cookie 或個人媒體庫資料。

<p align="center">
  <img src="docs/assets/readme/screenshots/01-home.png" alt="有繼續觀看和媒體庫捷徑的首頁" width="220">
  <img src="docs/assets/readme/screenshots/02-explore.png" alt="有搜尋、篩選和影片卡片的探索頁" width="220">
  <img src="docs/assets/readme/screenshots/03-shorts.png" alt="有直向播放和本機回饋的短影音動態" width="220">
</p>
<p align="center">
  <img src="docs/assets/readme/screenshots/04-queue.png" alt="有稍後觀看和本機清單的播放佇列" width="220">
  <img src="docs/assets/readme/screenshots/05-watch-page.png" alt="有中繼資料、評分、標籤和操作的觀看頁" width="220">
  <img src="docs/assets/readme/screenshots/06-player-controls.png" alt="有進度列、速度、鎖定和全螢幕的播放器控制" width="220">
</p>
<p align="center">
  <img src="docs/assets/readme/screenshots/07-images.png" alt="有搜尋、排序和篩選的圖片瀏覽頁" width="220">
  <img src="docs/assets/readme/screenshots/08-photo-viewer.png" alt="隱藏頂部列和底部工具的照片欣賞模式" width="220">
</p>

## 安裝

1. 在 Android 手機上開啟[最新發布版](https://github.com/gomeng-dev/stash-player-android/releases)。
2. 下載 APK。
3. 開啟 APK，並在 Android 提示時允許瀏覽器或檔案管理器安裝應用程式。
4. 啟動 **Stash Player** 並連線到你的 Stash 伺服器。

> 目前公開版本：**v1.10.4**。

## 需要什麼

- Android 10 或更新版本。
- 手機可以連到的 Stash 伺服器。
- 一種連線方式：
  - 可信任的本機伺服器且未啟用認證，
  - Stash API Key，
  - Stash 使用者名稱/密碼登入。
- 選用：Stash Hybrid Recommendations Engine 外掛，用於更豐富的相似影片推薦。

## 首次連線

首次啟動時，輸入 Stash 伺服器位址並選擇符合伺服器設定的認證方式。

常見範例：

- `http://192.168.0.10:9999`
- `http://stash.local:9999`
- 你自己的 HTTPS 反向代理位址

點選 **測試連線**。成功後儲存伺服器，應用程式會開啟首頁。

## 主要功能

### 首頁

首頁用於快速開始觀看：繼續上次的影片、前往探索或短影音、開啟佇列/稍後觀看/收藏，並查看應用程式更新狀態。

### 探索

探索合併了瀏覽與搜尋。無論輸入搜尋字詞或直接瀏覽整個媒體庫，都可以使用標籤、日期、時長、評分、媒體品質、觀看狀態、本機收藏、已儲存篩選器、隨機排序和批次操作。

### 短影音

短影音是直向動態。它會預先載入附近項目以提升滑動流暢度，並把回饋保存在裝置上。

可用手勢包括點一下播放/暫停、滑動到下一項、雙擊喜歡、長按暫時 1.5 倍速播放，以及拖動進度列精準定位。

### 觀看頁和播放器

觀看頁把影片、場景操作、中繼資料和推薦內容放在一起。播放器支援全螢幕、直向觀看頁模式、雙擊快轉/倒退、水平拖動定位、側邊亮度/音量手勢、長按倍速、鎖定模式、串流選擇、字幕、PiP、方向控制和播放清單導覽。

### 本機清單

這些清單儲存在 Android 裝置上：

- **佇列**：現在想連續觀看的內容。
- **稍後觀看**：以後再看的內容。
- **收藏**：僅在本應用程式內使用的本機收藏標記。
- **播放記錄**：用於首頁和佇列的本機繼續觀看記錄。

### 設定與語言

設定中可以選擇應用程式語言，包括系統預設、韓語、英語、簡體中文和繁體中文。伺服器設定也可以切換 Stash 的「從包含圖片的資料夾建立圖庫」選項，並在確認後啟動媒體庫掃描。

## 隱私

Stash Android Player 是連線到你自己伺服器的直接用戶端。

- 伺服器設定、API Key、工作階段 Cookie、本機清單、歷史和短影音回饋都儲存在裝置上。
- 使用者名稱/密碼登入只會保存刷新工作階段所需的材料。請勿公開裝置備份或日誌。
- 最近應用程式隱私選項可以隱藏 Android 最近工作中的應用程式預覽。
- 偵錯日誌會隱藏敏感認證資訊。
- 分享截圖時，請避免顯示真實伺服器位址、檔名、憑證或私人媒體。

## 疑難排解

| 問題 | 可以嘗試 |
| --- | --- |
| 連線失敗 | 確認手機可以透過同一網路、VPN 或反向代理存取 Stash URL。 |
| 縮圖不顯示 | 重新測試伺服器連線，並確認目前認證方式仍然有效。 |
| 重新啟動後登入失效 | 在設定中重新輸入使用者名稱/密碼以更新工作階段刷新材料。 |
| 短影音為空 | 檢查媒體庫中是否有短於設定裡最大時長的影片。 |
| 推薦為空 | 啟用 Hybrid Recommendations 外掛，或讓應用程式使用 Stash 回退推薦。 |
| APK 無法安裝 | 允許開啟 APK 的瀏覽器、檔案管理器或安裝器安裝未知應用程式。 |

## 開發

面向一般使用者的安裝和使用說明在上方。建置方式、專案結構、簽署和驗證命令見 [DEVELOPMENT.md](DEVELOPMENT.md)。

```bash
./gradlew :app:assembleDebug
```

## 授權

MIT。詳見 [LICENSE](LICENSE)。
