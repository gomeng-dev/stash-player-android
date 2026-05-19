# Changelog

All notable changes to Stash Android Player are documented here.

When bumping `versionName`, add a new section for the release version and summarize the changes since the previous version. The public release workflow uses the matching section for GitHub Release notes.

## [Unreleased]

## [1.7.4] - 2026-05-19

### English

- Preserved Gallery detail photo-grid loaded pages and viewport position when returning from the photo viewer, while resetting that viewport when switching to a different gallery.
- Added regression coverage for Gallery detail load-more/back navigation state preservation.

### 한국어

- 사진 뷰어에서 돌아올 때 갤러리 상세 사진 그리드의 추가 로드된 페이지와 스크롤 위치가 유지되도록 하고, 다른 갤러리로 이동할 때는 해당 위치를 초기화했습니다.
- 갤러리 상세 추가 로드/뒤로 가기 상태 보존 회귀 테스트를 추가했습니다.

### 简体中文

- 从照片查看器返回时保留图库详情照片网格已加载的分页和滚动位置，同时在切换到其他图库时重置该位置。
- 新增图库详情加载更多/返回导航状态保留的回归测试。

### 繁體中文

- 從照片檢視器返回時保留圖庫詳情照片網格已載入的分頁與捲動位置，同時在切換到其他圖庫時重置該位置。
- 新增圖庫詳情載入更多/返回導覽狀態保留的迴歸測試。

## [1.7.3] - 2026-05-18

### English

- Fixed playlist playback for scenes whose saved resume position is already at the watched threshold, restarting them from the beginning instead of immediately auto-advancing to the next queued scene.
- Added regression coverage for watched-threshold resume handling, including the exact cutoff boundary.

### 한국어

- 저장된 이어보기 위치가 이미 시청 완료 기준에 도달한 재생목록 영상을 열 때 다음 항목으로 즉시 넘어가지 않고 처음부터 재생되도록 수정했습니다.
- 시청 완료 기준의 경계값을 포함해 이어보기 위치 재시작 정책 회귀 테스트를 추가했습니다.

### 简体中文

- 修复播放列表中已保存到已看完阈值位置的影片会立即跳到下一项的问题，现在会从开头重新播放。
- 新增已看完阈值与边界位置的续播回归测试。

### 繁體中文

- 修復播放列表中已儲存到已看完門檻位置的影片會立即跳到下一項的問題，現在會從開頭重新播放。
- 新增已看完門檻與邊界位置的續播回歸測試。

## [1.7.2] - 2026-05-18

### English

- Fixed the public release workflow so Korean, Simplified Chinese, and Traditional Chinese README files are copied into the sanitized public repository snapshot.
- Added release workflow regression coverage and validation so localized README links cannot be published as broken links again.
- Updated README current-release badges/copy to point at v1.7.2.

### 한국어

- 공개 릴리즈 workflow가 정리된 공개 저장소 스냅샷에 한국어, 중국어 간체, 중국어 번체 README 파일을 함께 복사하도록 수정했습니다.
- 현지화 README 링크가 다시 깨진 상태로 배포되지 않도록 릴리즈 workflow 회귀 테스트와 필수 파일 검증을 추가했습니다.
- README의 현재 공개 릴리즈 표기를 v1.7.2로 갱신했습니다.

### 简体中文

- 修复公开发布 workflow，使其在生成净化后的公开仓库快照时包含韩语、简体中文和繁体中文 README 文件。
- 新增发布 workflow 回归测试和必需文件校验，防止本地化 README 链接再次以损坏状态发布。
- 将 README 中的当前公开版本更新为 v1.7.2。

### 繁體中文

- 修復公開發布 workflow，使其在產生淨化後的公開儲存庫快照時包含韓文、簡體中文和繁體中文 README 檔案。
- 新增發布 workflow 回歸測試與必要檔案驗證，避免本地化 README 連結再次以失效狀態發布。
- 將 README 中的目前公開版本更新為 v1.7.2。

## [1.7.1] - 2026-05-17

### English

- Fixed the Home tab crash that could occur in Chinese/localized app languages when multiple quick action labels resolve to the same translated text.
- Keyed Home quick actions and their icons with stable locale-independent identifiers instead of visible labels.
- Added regression coverage for unique Home quick action keys so localized label collisions cannot break the Compose lazy row again.

### 한국어

- 중국어 등 현지화 앱 언어에서 홈 빠른 실행 라벨이 같은 번역 텍스트로 겹칠 때 홈 탭이 종료될 수 있던 문제를 수정했습니다.
- 홈 빠른 실행 항목과 아이콘을 표시 라벨이 아니라 로케일과 무관한 안정 식별자로 매칭하도록 바꿨습니다.
- 현지화 라벨 중복이 Compose lazy row를 다시 깨뜨리지 않도록 홈 빠른 실행 키 고유성 회귀 테스트를 추가했습니다.

### 简体中文

- 修复在中文等本地化语言下，多个首页快捷操作显示为相同翻译文本时可能导致首页崩溃的问题。
- 首页快捷操作及其图标改用与语言无关的稳定标识符，而不是可见标签文本。
- 新增首页快捷操作 key 唯一性的回归测试，防止本地化标签冲突再次破坏 Compose lazy row。

### 繁體中文

- 修復在中文等本地化語言下，多個首頁快捷操作顯示為相同翻譯文字時可能導致首頁崩潰的問題。
- 首頁快捷操作及其圖示改用與語言無關的穩定識別碼，而不是可見標籤文字。
- 新增首頁快捷操作 key 唯一性的回歸測試，避免本地化標籤衝突再次破壞 Compose lazy row。

## [1.7.0] - 2026-05-17

### English

- Added Simplified Chinese and Traditional Chinese app language options, including localized Android string resources.
- Added Simplified Chinese and Traditional Chinese README files and linked them from the existing README badges.
- Added Settings access to Stash's "create galleries from folders containing images" server option, plus a guarded library scan action.
- Verified the current feature set with unit tests, debug assembly, lint, resource checks, and placeholder parity checks for the new Chinese resources.

### 한국어

- 중국어 간체와 중국어 번체 앱 언어 옵션을 추가하고 Android 문자열 리소스를 현지화했습니다.
- 중국어 간체와 중국어 번체 README를 추가하고 기존 README 배지에서 연결했습니다.
- 설정에서 Stash 서버의 "이미지가 들어있는 폴더로부터 갤러리 생성" 옵션을 확인/변경하고, 확인 모달 뒤 라이브러리 스캔을 시작할 수 있게 했습니다.
- 새 중국어 리소스의 placeholder parity 확인을 포함해 unit test, debug assemble, lint, 리소스 검증으로 현재 기능 세트를 확인했습니다.

### 简体中文

- 新增简体中文和繁体中文应用语言选项，并加入对应的 Android 字符串资源。
- 新增简体中文和繁体中文 README，并从现有 README 徽章链接。
- 在设置中加入 Stash 服务器的“从包含图片的文件夹创建图库”选项，并提供带确认弹窗的媒体库扫描操作。
- 通过单元测试、debug 构建、lint、资源检查，以及新中文资源的占位符一致性检查验证当前功能。

### 繁體中文

- 新增簡體中文和繁體中文應用程式語言選項，並加入對應的 Android 字串資源。
- 新增簡體中文和繁體中文 README，並從現有 README 徽章連結。
- 在設定中加入 Stash 伺服器的「從包含圖片的資料夾建立圖庫」選項，並提供帶確認對話框的媒體庫掃描操作。
- 透過單元測試、debug 建置、lint、資源檢查，以及新中文資源的佔位符一致性檢查驗證目前功能。

## [1.6.12] - 2026-05-17

### English

- Fixed folder-created Stash galleries so blank gallery titles fall back to the server folder name before falling back to the numeric gallery ID.
- Removed the custom Gallery > Images > Folders mode now that Stash can create folder-backed galleries directly.
- Kept Gallery parent-folder filtering intact for server-backed galleries while simplifying the Images tab display modes to Grid and Wall.
- Updated regression coverage for folder-backed gallery naming and the retired image-folder grouping mode.

### 한국어

- Stash에서 폴더로 생성된 갤러리의 제목이 비어 있을 때 숫자 ID로 표시되기 전에 서버 폴더 이름을 표시하도록 수정했습니다.
- Stash 자체의 폴더 기반 갤러리 생성 기능을 사용하도록, 앱의 별도 Gallery > Images > Folders 모드를 제거했습니다.
- 서버 기반 갤러리의 부모 폴더 필터는 유지하고, Images 탭 표시 모드는 Grid와 Wall로 단순화했습니다.
- 폴더 기반 갤러리 이름 fallback과 제거된 이미지 폴더 그룹핑 모드에 맞춰 회귀 테스트를 갱신했습니다.

## [1.6.11] - 2026-05-17

### English

- Reworked Gallery > Images > Folders to build folder cards from the currently loaded image results for faster first paint instead of blocking on a full server folder index.
- Kept folder drill-in server-backed and exact, so opening a folder loads all direct images in that folder with a parent-folder filter.
- Added lazy exact folder counts for visible folder cards using batched GraphQL image-count lookups, while keeping folder cards visible if count hydration fails.
- Added parent-folder metadata parsing for images and regression coverage for image-backed folder grouping, exact folder detail, and batched count queries.

### 한국어

- Gallery > Images > Folders에서 전체 서버 폴더 인덱스를 기다리지 않고 현재 로드된 이미지 결과를 폴더별로 묶어 폴더 카드를 더 빠르게 표시하도록 개선했습니다.
- 폴더 진입은 서버 기반 정확 조회를 유지해, 선택한 폴더의 직접 이미지를 parent-folder 필터로 전체 페이지네이션합니다.
- 표시된 폴더 카드의 정확한 이미지 개수는 batched GraphQL 조회로 나중에 채우고, 개수 조회가 실패해도 폴더 카드는 그대로 유지합니다.
- 이미지 parent-folder 메타데이터 파싱과 이미지 기반 폴더 그룹핑, 정확한 폴더 상세, batched count 조회 회귀 테스트를 추가했습니다.

## [1.6.9] - 2026-05-17

### English

- Fixed Gallery > Images folder mode so selecting a folder loads that folder's images from Stash with a server-side directory filter instead of showing only the images already loaded on the current page.
- Replaced the folder-card index with a server-backed folder query, so folder browsing is no longer limited to the partially loaded global image grid.
- Preserved folder-relative photo viewer order, folder paging, path/random sort behavior, and separate folder index/detail loading states.
- Updated the release workflow action runtime so patch releases publish with the current GitHub Actions platform.

### 한국어

- Gallery > Images의 폴더별 보기에서 폴더를 선택하면 현재 페이지에 이미 로딩된 일부 이미지만 보여주는 대신, Stash 서버의 디렉터리 필터로 해당 폴더 이미지를 다시 조회하도록 수정했습니다.
- 폴더 카드 목록을 서버 기반 폴더 조회로 전환해, 폴더 브라우징이 일부 로딩된 전체 이미지 그리드에 제한되지 않도록 했습니다.
- 폴더 내부 사진 뷰어 순서, 폴더별 페이지네이션, 경로/랜덤 정렬 동작, 폴더 목록/상세 로딩 상태 분리를 유지했습니다.
- 패치 릴리스가 최신 GitHub Actions 환경에서 배포되도록 릴리스 워크플로 런타임을 갱신했습니다.

## [1.6.8] - 2026-05-17

### English

- Fixed Gallery > Images folder mode so the normal image grid, folder list, and selected folder detail each keep separate scroll positions when opening and dismissing the photo viewer.
- Added regression coverage for the distinct folder-mode scroll states so folder cards and folder images cannot overwrite each other's viewport anchors.

### 한국어

- Gallery > Images의 폴더별 보기에서 일반 이미지 그리드, 폴더 목록, 선택한 폴더 내부가 각각 별도 스크롤 위치를 유지하도록 수정해 사진 뷰어를 열었다가 닫아도 스크롤이 초기화되지 않게 했습니다.
- 폴더 카드와 폴더 내부 이미지가 서로 다른 viewport anchor를 덮어쓰지 않도록 별도 스크롤 상태 회귀 테스트를 추가했습니다.

## [1.6.7] - 2026-05-17

### English

- Fixed media route back-navigation restoration so Browse, Explore, Gallery, and Image results/page state are preserved after opening the player or photo viewer and pressing back.
- Prevented preserved media routes from unconditionally reloading page 1 on re-entry while still reloading when filters, sort, source, or query actually change.
- Cleared cancelled transient loading flags on route disposal so interrupted loads do not leave preserved lists stuck in a loading state.

### 한국어

- Browse, Explore, Gallery, Image에서 플레이어 또는 사진 뷰어를 열었다가 뒤로 돌아올 때 결과/page 상태가 유지되도록 뒤로가기 복원 문제를 수정했습니다.
- 보존된 미디어 Route가 재진입 시 1페이지를 무조건 다시 불러오지 않도록 막고, 필터/정렬/소스/검색어가 실제로 바뀐 경우에는 정상 재로드되도록 했습니다.
- 화면 전환으로 진행 중이던 로드가 취소될 때 임시 `isLoading` 상태를 정리해, 보존된 목록이 로딩 상태에 갇히지 않도록 했습니다.

## [1.6.6] - 2026-05-16

### English

- Preserved Browse, Explore, Gallery, Gallery detail, and Queue scroll positions when opening a player or photo viewer and pressing back.
- Added regression coverage that verifies media routes own and wire explicit lazy scroll state for their list/grid containers.

### 한국어

- Browse, Explore, Gallery, Gallery 상세, Queue에서 플레이어 또는 사진 뷰어를 열었다가 뒤로 돌아와도 목록/그리드 스크롤 위치가 유지되도록 수정했습니다.
- 미디어 Route가 명시적인 lazy scroll state를 소유하고 실제 목록/그리드 컨테이너에 연결하는지 검증하는 회귀 테스트를 추가했습니다.

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