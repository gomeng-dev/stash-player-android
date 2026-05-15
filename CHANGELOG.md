# Changelog

All notable changes to Stash Android Player are documented here.

When bumping `versionName`, add a new section for the release version and summarize the changes since the previous version. The public release workflow uses the matching section for GitHub Release notes.

## [Unreleased]

## [1.6.5] - 2026-05-15

### English

- Refined the Browse/Explore discovery toolbar by moving video filter shortcuts into a Gallery-style filter group row below the primary sort/random/view controls, while preserving active filter chips and selection-mode behavior.
- Retired the legacy Search tab surface and migrated remaining reusable query/page-size/playback-continuation concepts to Discovery/Explore naming without removing normal text search inputs in Gallery or filter sheets.

### 한국어

- Browse/Explore 탐색 툴바에서 영상 필터 바로가기를 기본 정렬/랜덤/보기 컨트롤과 분리하고, Gallery 스타일의 필터 그룹 row로 배치했습니다. active filter chip과 선택 모드 동작은 유지했습니다.
- 더 이상 사용하지 않는 legacy Search 탭 표면을 제거하고, 계속 필요한 query/page-size/playback continuation 개념은 Discovery/Explore 명칭으로 정리했습니다. Gallery와 필터 시트의 일반 텍스트 검색 입력은 유지했습니다.

## [1.6.4] - 2026-05-15

### English

- Fixed Gallery > Images Folders drill-in so opening a photo uses the selected folder grid's image order for the full-screen viewer and next/previous navigation.
- Added regression coverage that protects folder viewer requests from falling back to original flat-image indexes after folder sorting.

### 한국어

- Gallery > Images의 폴더별 보기에서 폴더 안 사진을 열 때 전체화면 뷰어와 이전/다음 이동이 선택한 폴더 그리드 순서를 그대로 따르도록 수정했습니다.
- 폴더 정렬 이후에도 뷰어가 원래 전체 이미지 목록 인덱스로 되돌아가지 않도록 회귀 테스트를 추가했습니다.

## [1.6.3] - 2026-05-15

### English

- Fixed Gallery > Images Folders mode so Random sort fetches images by path first and then applies a stable seed-based shuffle to folder cards, instead of grouping an image-randomized page.
- Kept non-folder image modes on Stash's seeded random server sort, preserving the existing grid and wall random behavior.
- Added regression coverage for folder-mode random server sorting and folder-level randomization that is independent of the loaded image order.

### 한국어

- Gallery > Images의 폴더별 보기에서 무작위 정렬 시 이미지 목록을 먼저 랜덤으로 받아온 뒤 폴더로 묶던 문제를 수정하고, 경로 기준으로 폴더를 만든 다음 폴더 카드 단위로 안정적인 seed 기반 셔플을 적용하도록 변경했습니다.
- 폴더별 보기가 아닌 이미지 그리드/벽 모드에서는 기존처럼 Stash의 seed 기반 랜덤 서버 정렬을 유지합니다.
- 폴더별 무작위 정렬이 로딩된 이미지 순서가 아니라 폴더 단위 순서에 적용되는지 검증하는 회귀 테스트를 추가했습니다.

## [1.6.2] - 2026-05-14

### English

- Changed Gallery > Images Folders mode from inline section headers to gallery-like folder cards that open one level deeper into the selected folder.
- Sorted folder cards and in-folder images by normalized filesystem path, including descending path order when the Image sort direction is toggled.
- Kept full-screen photo viewer navigation anchored to the original loaded image list so folder drill-down cards still open at the correct photo.

### 한국어

- Gallery > Images의 폴더별 보기 방식을 인라인 섹션 헤더 대신 갤러리처럼 보이는 폴더 카드로 바꾸고, 선택한 폴더 안으로 한 뎁스 들어가 사진을 보도록 개선했습니다.
- 폴더 카드와 폴더 내부 사진을 정규화된 파일 경로 기준으로 정렬하며, Image 정렬 방향을 내림차순으로 바꾸면 경로별 결과도 내림차순으로 표시합니다.
- 폴더 안 사진을 열어도 전체 화면 사진 뷰어가 기존에 불러온 이미지 목록의 정확한 위치에서 시작하도록 유지했습니다.

## [1.6.1] - 2026-05-14

### English

- Added an image-only Folders display mode in Gallery > Images, grouping loaded photos by parent folder while preserving the original viewer order.
- Persisted the photo viewer Fit/Original display mode across viewer sessions with safe defaults and regression coverage.
- Added focused model/settings tests and an independent OpenClaw follow-up review for the folder view and viewer display-mode persistence.

### 한국어

- Gallery > Images에 이미지 전용 Folders 보기 모드를 추가해, 불러온 사진을 상위 폴더별로 묶으면서 기존 뷰어 순서를 유지합니다.
- 사진 뷰어의 맞춤/Fit 및 원본/Original 표시 모드를 세션 간 저장하도록 개선하고 안전한 기본값과 회귀 테스트를 추가했습니다.
- 폴더 보기와 뷰어 표시 모드 저장 동작에 대한 모델/설정 테스트와 OpenClaw 후속 독립 리뷰를 추가했습니다.

## [1.6.0] - 2026-05-14

### English

- Added a native Gallery tab with GraphQL-backed gallery browsing, detail photo grids, full-screen photo viewing, zoom/pan gestures, preloading, and Web-parity toolbar controls.
- Added a global Image browsing mode with sort/random/page-size/display controls, ImageFilterType GraphQL filtering, saved/recent image filters, and Gallery/Image mode persistence.
- Expanded photo-viewer controls with slideshow, display mode, rating and O-count actions, linked gallery access, appreciation mode, compact one-row bottom chrome, and details rows including the image path.
- Improved Gallery/Image metadata parsing and display fallbacks, including privacy-safe basename titles, decoded filenames, VisualFile field selections, count fields, and retained toolbar/filter preferences.
- Polished Gallery selection, list/wall card views, read-only detail panels, entity filters, saved/recent filters, and final QA/debug-release handoff for the Gallery and Image workflows.
- Fixed Stash GraphQL VisualFile selections and preserved user-chosen HTTP Stash auth behavior.

### 한국어

- GraphQL 기반 갤러리 탐색, 상세 사진 그리드, 전체 화면 사진 뷰어, 줌/팬 제스처, 다음 이미지 preloading, Web parity 툴바를 갖춘 네이티브 Gallery 탭을 추가했습니다.
- 정렬/랜덤/페이지 크기/표시 모드, ImageFilterType GraphQL 필터, 이미지 저장/최근 필터, Gallery/Image 모드 저장을 갖춘 전역 Image 탐색 모드를 추가했습니다.
- 슬라이드쇼, 표시 모드, 별점과 O-count 조작, 연결 갤러리 접근, 감상 모드, 한 줄 compact 하단 크롬, 이미지 경로를 포함한 상세정보 행으로 사진 뷰어 조작을 확장했습니다.
- 개인정보 보호용 basename 제목 fallback, 파일명 디코딩, VisualFile 필드 선택, count 필드, 툴바/필터 선호 저장 등 Gallery/Image 메타데이터 파싱과 표시 안정성을 개선했습니다.
- Gallery 선택, 목록/벽 카드 보기, 읽기 전용 상세 패널, 엔티티 필터, 저장/최근 필터, Gallery/Image 최종 QA/debug release 전달을 다듬었습니다.
- Stash GraphQL VisualFile selection 문제를 수정하고 사용자가 선택한 HTTP Stash 인증 정책을 유지했습니다.

## [1.5.2] - 2026-05-11

### English

- Fixed device authentication app lock activation when the app uses an in-app language context, so the biometric/device-credential prompt can still open from Settings.

### 한국어

- 앱 언어 설정용 컨텍스트를 사용하는 상태에서도 Settings에서 생체/기기 인증 프롬프트가 열리도록, 기기 인증 앱 잠금 활성화 문제를 수정했습니다.

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
