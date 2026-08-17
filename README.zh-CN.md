<div align="center">

# Stash Android Player

### 让自托管 Stash 媒体库在 Android 上更好用。

在手机上浏览、搜索、排队、滑动短视频，并用以手势为核心的原生播放器观看内容，同时媒体库仍保留在你自己的 Stash 服务器上。

<p>
  <a href="README.md"><img alt="English README" src="https://img.shields.io/badge/README-English-7c3aed?style=for-the-badge"></a>
  <a href="README.ko.md"><img alt="Korean README" src="https://img.shields.io/badge/README-%ED%95%9C%EA%B5%AD%EC%96%B4-7c3aed?style=for-the-badge"></a>
  <a href="README.zh-TW.md"><img alt="Traditional Chinese README" src="https://img.shields.io/badge/README-%E7%B9%81%E9%AB%94%E4%B8%AD%E6%96%87-7c3aed?style=for-the-badge"></a>
  <a href="https://github.com/gomeng-dev/stash-player-android/releases"><img alt="下载 APK" src="https://img.shields.io/badge/download-APK-34d399?style=for-the-badge&logo=github&logoColor=white"></a>
  <img alt="Android 10+" src="https://img.shields.io/badge/Android-10%2B-34d399?style=for-the-badge&logo=android&logoColor=white">
  <img alt="Kotlin + Compose" src="https://img.shields.io/badge/Kotlin%20%2B%20Compose-111827?style=for-the-badge&logo=kotlin&logoColor=white">
</p>

<img src="docs/assets/readme/screenshots/00-hero.png" alt="Stash Android Player 预览，显示首页、探索、队列、播放器控制、图片和图库欣赏模式" width="100%">

</div>

---

## 为什么使用它？

Stash 很强大，但桌面 Web UI 在手机上并不总是顺手。**Stash Android Player** 为你的自托管媒体库提供原生 Android 体验：

- **首页**：继续观看、打开本地列表，并快速开始播放。
- **探索**：在一个界面中搜索、筛选、排序、随机/洗牌、使用已保存筛选器和批量操作。
- **短视频**：以竖向信息流浏览较短的视频。
- **队列 / 稍后观看 / 收藏**：本地保存在设备上。
- **观看页 + Media3 播放器**：支持手势、全屏、流选择、字幕、PiP 和播放列表控制。
- **推荐**：优先使用 Stash Hybrid Recommendations 插件；不可用时回退到 Stash 默认推荐数据。
- **图库**：浏览 Stash 图库和图片，支持全屏照片查看器；欣赏模式会隐藏顶部栏和底部工具。

不需要托管服务。应用直接连接你的 Stash 服务器，不会把媒体库元数据上传到本项目。

## 截图

以下截图来自使用公开演示媒体运行的真实 Android 应用，不包含私人媒体、服务器地址、API Key、凭据、Cookie 或个人媒体库数据。

<p align="center">
  <img src="docs/assets/readme/screenshots/01-home.png" alt="带继续观看和媒体库快捷入口的首页" width="220">
  <img src="docs/assets/readme/screenshots/02-explore.png" alt="带搜索、筛选和视频卡片的探索页" width="220">
  <img src="docs/assets/readme/screenshots/03-shorts.png" alt="带竖向播放和本地反馈的短视频信息流" width="220">
</p>
<p align="center">
  <img src="docs/assets/readme/screenshots/04-queue.png" alt="带稍后观看和本地列表的播放队列" width="220">
  <img src="docs/assets/readme/screenshots/05-watch-page.png" alt="带元数据、评分、标签和操作的观看页" width="220">
  <img src="docs/assets/readme/screenshots/06-player-controls.png" alt="带进度条、速度、锁定和全屏的播放器控制" width="220">
</p>
<p align="center">
  <img src="docs/assets/readme/screenshots/07-images.png" alt="带搜索、排序和筛选的图片浏览页" width="220">
  <img src="docs/assets/readme/screenshots/08-photo-viewer.png" alt="隐藏顶部栏和底部工具的照片欣赏模式" width="220">
</p>

## 安装

1. 在 Android 手机上打开[最新发布版](https://github.com/gomeng-dev/stash-player-android/releases)。
2. 下载 APK。
3. 打开 APK，并在 Android 提示时允许浏览器或文件管理器安装应用。
4. 启动 **Stash Player** 并连接到你的 Stash 服务器。

> 当前公开版本：**v1.10.2**。

## 需要什么

- Android 10 或更高版本。
- 手机可以访问的 Stash 服务器。
- 一种连接方式：
  - 可信本地服务器且未启用认证，
  - Stash API Key，
  - Stash 用户名/密码登录。
- 可选：Stash Hybrid Recommendations Engine 插件，用于更丰富的相似视频推荐。

## 首次连接

首次启动时，输入 Stash 服务器地址并选择符合服务器设置的认证方式。

常见示例：

- `http://192.168.0.10:9999`
- `http://stash.local:9999`
- 你自己的 HTTPS 反向代理地址

点击 **测试连接**。成功后保存服务器，应用会打开首页。

## 主要功能

### 首页

首页用于快速开始观看：继续上次的视频、进入探索或短视频、打开队列/稍后观看/收藏，并查看应用更新状态。

### 探索

探索合并了浏览与搜索。无论输入搜索词还是直接浏览整个媒体库，都可以使用标签、日期、时长、评分、媒体质量、观看状态、本地收藏、已保存筛选器、随机排序和批量操作。

### 短视频

短视频是竖向信息流。它会预加载附近条目以提升滑动流畅度，并把反馈保存在设备上。

可用手势包括点击播放/暂停、滑动到下一项、双击喜欢、长按临时 1.5 倍速播放，以及拖动进度条精确定位。

### 观看页和播放器

观看页把视频、场景操作、元数据和推荐内容放在一起。播放器支持全屏、竖屏观看页模式、双击快进/后退、横向拖动定位、侧边亮度/音量手势、长按倍速、锁定模式、流选择、字幕、PiP、方向控制和播放列表导航。

### 本地列表

这些列表保存在 Android 设备上：

- **队列**：现在想连续观看的内容。
- **稍后观看**：以后再看的内容。
- **收藏**：仅在本应用内使用的本地收藏标记。
- **播放历史**：用于首页和队列的本地继续观看记录。

### 设置与语言

设置中可以选择应用语言，包括系统默认、韩语、英语、简体中文和繁体中文。服务器设置还可以切换 Stash 的“从包含图片的文件夹创建图库”选项，并在确认后启动媒体库扫描。

## 隐私

Stash Android Player 是连接你自己服务器的直接客户端。

- 服务器设置、API Key、会话 Cookie、本地列表、历史和短视频反馈都保存在设备上。
- 用户名/密码登录只保存刷新会话所需的材料。请勿公开设备备份或日志。
- 最近应用隐私选项可以隐藏 Android 最近任务中的应用预览。
- 调试日志会隐藏敏感认证信息。
- 分享截图时，请避免显示真实服务器地址、文件名、凭据或私有媒体。

## 故障排查

| 问题 | 可以尝试 |
| --- | --- |
| 连接失败 | 确认手机可以通过同一网络、VPN 或反向代理访问 Stash URL。 |
| 缩略图不显示 | 重新测试服务器连接，并确认当前认证方式仍然有效。 |
| 重启后登录失效 | 在设置中重新输入用户名/密码以更新会话刷新材料。 |
| 短视频为空 | 检查媒体库中是否有短于设置里最大时长的视频。 |
| 推荐为空 | 启用 Hybrid Recommendations 插件，或让应用使用 Stash 回退推荐。 |
| APK 无法安装 | 允许打开 APK 的浏览器、文件管理器或安装器安装未知应用。 |

## 开发

面向最终用户的安装和使用说明在上方。构建方式、项目结构、签名和验证命令见 [DEVELOPMENT.md](DEVELOPMENT.md)。

```bash
./gradlew :app:assembleDebug
```

## 许可证

MIT。详见 [LICENSE](LICENSE)。
