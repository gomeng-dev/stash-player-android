# Changelog

All notable changes to Stash Android Player are documented here.

When bumping `versionName`, add a new section for the release version and summarize the changes since the previous version. The public release workflow uses the matching section for GitHub Release notes.

## [Unreleased]

## [1.5.1] - 2026-05-10

### English

- Fixed the device authentication app lock option so devices with weak biometric enrollment or device credentials can enable and use app lock.

### 한국어

- 약한 생체 인증 등록 또는 기기 잠금 인증을 사용할 수 있는 기기에서 앱 잠금 옵션을 켜고 사용할 수 있도록 수정했습니다.

## [1.5.0] - 2026-05-10

### English

- Added the Shorts tab with vertical swipe playback, prewarm, full-surface play/pause gestures, seek controls, long-press speed hold, delete actions, and configurable maximum clip duration.
- Added Shorts recommendation learning from likes, not-interested feedback, watch outcomes, and liked-anchor Hybrid/GraphQL similarity signals, plus a Settings reset action.
- Merged Browse and Search into one Explore tab and reserved the center tab for Shorts.
- Added a Home recommended videos row powered by Shorts likes, Favorites, Hybrid/GraphQL similarity, and a broader whole-library candidate pool.
- Added in-app update checks with changelist viewing and APK download/install handoff from Support.
- Removed explanatory UI helper copy and simplified Home by removing the old Explore/Shorts promo sections.
- Preserve password-based Stash sessions across app restarts by storing the login material needed to refresh the session cookie.
- Refreshed README content and screenshots for the 1.5.0 app shape.

### 한국어

- 세로 스와이프 재생, prewarm, 전체 화면 play/pause 제스처, 탐색 컨트롤, 롱프레스 배속, 삭제, 최대 길이 설정을 갖춘 Shorts 탭을 추가했습니다.
- 좋아요, 관심없음, 시청 결과, 좋아요 앵커 기반 Hybrid/GraphQL 유사도 신호를 학습하는 Shorts 추천과 설정 초기화 기능을 추가했습니다.
- 둘러보기와 검색을 하나의 탐색 탭으로 통합하고 중앙 탭을 Shorts로 예약했습니다.
- Shorts 좋아요, 즐겨찾기, Hybrid/GraphQL 유사도, 전체 라이브러리 후보 풀을 사용하는 홈 추천 영상 섹션을 추가했습니다.
- 앱 내 업데이트 확인, 변경 내역 보기, APK 다운로드/설치 연결 흐름을 Support에 추가했습니다.
- 공간을 차지하던 설명성 UI 문구를 제거하고 홈의 기존 탐색/숏츠 홍보 섹션을 정리했습니다.
- 앱 재시작 후에도 ID/비밀번호 기반 Stash 세션을 복구할 수 있도록 session cookie 갱신에 필요한 로그인 정보를 보존합니다.
- 1.5.0 앱 구조에 맞춰 README 내용과 스크린샷을 갱신했습니다.

## [1.4.1] - 2026-05-09

### English

- Refreshed the README for the current app release with new safe-fixture Android screenshots, updated release badges, and copy that reflects the latest player chrome and privacy behavior.
- Fixed thumbnail loading for authenticated Stash sessions by carrying signed thumbnail requests through Browse, Search, and the player playlist drawer.
- Improved blank scene title fallback by requesting the primary file basename from Stash before falling back to `Video {id}`.
- Fixed the Browse/Search grid-list toggle labels so they resolve from the active app language instead of caching Korean text.

### 한국어

- 현재 앱 릴리스에 맞춰 README를 최신 safe-fixture Android 스크린샷, 릴리스 배지, 최신 플레이어 크롬/프라이버시 동작 설명으로 갱신했습니다.
- 인증된 Stash 세션에서 Browse, Search, 플레이어 재생목록 썸네일 요청에 인증 정보를 유지해 썸네일 로딩 문제를 수정했습니다.
- 제목이 비어 있는 scene에서 `Video {id}`로 떨어지기 전에 Stash의 기본 파일 basename을 제목 fallback으로 사용하도록 개선했습니다.
- Browse/Search의 격자/목록 전환 라벨이 한글로 캐싱되지 않고 현재 앱 언어 기준으로 표시되도록 수정했습니다.

## [1.4.0] - 2026-05-09

### English

- Reworked the player chrome toward a screenshot-style layout with title/tool controls at the top and seek/transport/action controls at the bottom.
- Moved automatic player orientation control out of Settings and into the player controls while keeping the existing saved preference.
- Kept compact bar phones locked to portrait outside fullscreen player mode, even when sensor orientation is enabled.

### 한국어

- 플레이어 크롬을 스크린샷 스타일에 맞춰 상단 제목/도구 컨트롤과 하단 탐색/재생/액션 컨트롤 구조로 개편했습니다.
- 자동 화면 방향 제어를 Settings에서 플레이어 컨트롤로 옮기고 기존 저장된 선호값은 유지했습니다.
- 센서 방향이 켜져 있어도 바형 스마트폰은 전체화면 플레이어가 아닐 때 세로 화면을 유지하도록 했습니다.

## [1.3.1] - 2026-05-09

### English

- Made local HTTP Stash connections the default path for server setup and settings, without extra HTTPS recommendation copy or local HTTP confirmation friction.

### 한국어

- 서버 설정과 설정 화면에서 로컬 HTTP Stash 연결을 기본 흐름으로 두고, HTTPS 권장 문구와 로컬 HTTP 확인 단계를 제거했습니다.

## [1.3.0] - 2026-05-09

### English

- Added a seek-preview timeline while scrubbing so the target position is visible against the full duration, not only as a time badge.
- Matched Stash Web title fallback behavior for scenes with blank titles by showing the primary file name instead of `Video {id}` when a file path is available.

### 한국어

- 탐색/스크럽 중 목표 위치가 전체 재생 시간 대비 어디인지 보이도록 seek preview timeline을 추가했습니다.
- 제목이 비어 있는 scene은 파일 경로가 있을 때 `영상 {id}` 대신 기본 파일명을 표시하도록 Stash Web의 제목 fallback 동작과 맞췄습니다.

## [1.2.0] - 2026-05-09

### English

- Redesigned the fullscreen player chrome with a video-first MX Player-style layout that keeps the center of the video clear.
- Restored accessible adjustable/progress semantics for the custom thin seek bar.
- Added the full upstream Stash scene sort criteria to Browse and Search.
- Made player automatic orientation control follow the Settings value without requiring an app restart.

### 한국어

- 전체화면 플레이어 크롬을 영상 중심의 MX Player 스타일로 재설계해 영상 중앙을 가리지 않도록 개선했습니다.
- 커스텀 얇은 탐색 바에 접근성용 조절/진행률 semantics를 복구했습니다.
- Browse와 Search에 Stash 원본 Scene 정렬 기준 전체를 추가했습니다.
- 플레이어 자동 화면 방향 제어가 앱 재시작 없이 설정값을 따르도록 수정했습니다.

## [1.1.0] - 2026-05-08

### English

- Added server setup support for link-only connections without an API key or username/password.
- Improved server settings so onboarding-style connection options are available from Settings.
- Removed the small preview overlay shown during player timeline seek gestures.
- Added playback history localization for the Queue tab.
- Fixed bottom navigation clipping above Android system navigation.
- Kept compact bar phones in portrait outside fullscreen player mode while preserving fullscreen sensor rotation.

### 한국어

- API Key나 ID/비밀번호 없이 링크만으로 접속하는 서버 설정을 추가했습니다.
- 설정 화면에서도 온보딩처럼 서버 연결 방식을 선택할 수 있도록 개선했습니다.
- 플레이어 타임라인 제스처 이동 중 작게 표시되던 프리뷰 오버레이를 제거했습니다.
- Queue 탭의 재생 기록 문구가 영어 설정에서 한글로 나오던 문제를 수정했습니다.
- Android 시스템 내비게이션 위에서 하단 네비게이션 바가 잘리던 문제를 수정했습니다.
- 바형 스마트폰은 전체화면 플레이어가 아닐 때 세로 화면을 유지하고, 전체화면에서는 기존 센서 회전을 보존했습니다.
