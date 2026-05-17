<div align="center">

# Stash Android Player

### 셀프 호스팅 Stash 라이브러리를 Android에서 편하게 보는 앱

휴대폰에서 Stash 라이브러리를 탐색하고, 검색하고, 대기열을 만들고, 숏츠처럼 넘겨 보고, 제스처 중심 네이티브 플레이어로 감상하세요. 라이브러리는 계속 사용자의 Stash 서버에 남아 있습니다.

<p>
  <a href="README.md"><img alt="English README" src="https://img.shields.io/badge/README-English-7c3aed?style=for-the-badge"></a>
  <a href="README.zh-CN.md"><img alt="중국어 간체 README" src="https://img.shields.io/badge/README-%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87-7c3aed?style=for-the-badge"></a>
  <a href="README.zh-TW.md"><img alt="중국어 번체 README" src="https://img.shields.io/badge/README-%E7%B9%81%E9%AB%94%E4%B8%AD%E6%96%87-7c3aed?style=for-the-badge"></a>
  <a href="https://github.com/gomeng-dev/stash-player-android/releases"><img alt="APK 다운로드" src="https://img.shields.io/badge/download-APK-34d399?style=for-the-badge&logo=github&logoColor=white"></a>
  <img alt="Android 10+" src="https://img.shields.io/badge/Android-10%2B-34d399?style=for-the-badge&logo=android&logoColor=white">
  <img alt="Kotlin + Compose" src="https://img.shields.io/badge/Kotlin%20%2B%20Compose-111827?style=for-the-badge&logo=kotlin&logoColor=white">
</p>

</div>

---

## 왜 필요한가요?

Stash는 강력하지만, 휴대폰에서 데스크톱 웹 UI를 그대로 쓰기는 불편할 때가 있습니다. **Stash Android Player**는 직접 운영하는 Stash 라이브러리를 Android 앱처럼 자연스럽게 쓰기 위한 클라이언트입니다.

- **홈**에서 이어보기, 로컬 목록, 빠른 재생을 시작합니다.
- **탐색**에서 검색, 필터, 정렬, 무작위/셔플, 저장 필터, 일괄 작업을 한 화면에서 처리합니다.
- **숏츠**에서 짧은 영상을 세로 피드처럼 넘겨 봅니다.
- **재생 대기열 / 나중에 보기 / 즐겨찾기**는 휴대폰에 로컬로 저장됩니다.
- **Watch page + Media3 플레이어**에서 제스처, 전체화면, 스트림 선택, 자막, PiP, 재생목록 조작을 사용합니다.
- **추천**은 Stash Hybrid Recommendations 플러그인이 있으면 우선 사용하고, 없으면 Stash 기본 추천으로 fallback됩니다.

별도 호스팅 서비스는 필요하지 않습니다. 앱은 사용자의 Stash 서버에 직접 연결하며, 라이브러리 메타데이터를 이 프로젝트 서버로 업로드하지 않습니다.

## 스크린샷

아래 이미지는 실제 Android 앱에서 공개 데모 미디어로 캡처했습니다. 개인 미디어, 인증 정보, 서버 주소, API key, 사용자 데이터는 보이지 않습니다.

<p align="center">
  <img src="docs/assets/readme/screenshots/01-home.png" alt="이어보기와 라이브러리 바로가기가 있는 홈 화면" width="220">
  <img src="docs/assets/readme/screenshots/02-explore.png" alt="검색, 필터, 영상 카드가 있는 탐색 화면" width="220">
  <img src="docs/assets/readme/screenshots/03-shorts.png" alt="피드백 버튼이 있는 숏츠 세로 피드" width="220">
</p>
<p align="center">
  <img src="docs/assets/readme/screenshots/04-queue.png" alt="나중에 보기와 로컬 목록이 있는 재생 대기열" width="220">
  <img src="docs/assets/readme/screenshots/05-watch-page.png" alt="메타데이터, 평점, 태그, 액션이 있는 watch page" width="220">
  <img src="docs/assets/readme/screenshots/06-player-controls.png" alt="탐색 바, 속도, 잠금, 전체화면이 보이는 플레이어 조작 화면" width="220">
</p>

| 화면 | 무엇을 할 수 있나요? |
| --- | --- |
| **홈** | 이어보기, 로컬 목록 열기, 빠른 재생 시작 |
| **탐색** | 검색과 전체 탐색을 같은 필터/일괄 도구로 처리 |
| **숏츠** | 짧은 영상을 세로로 넘기고 로컬 피드백 남기기 |
| **대기열** | Up Next, 나중에 보기, 즐겨찾기, 최근 재생 관리 |
| **Watch page** | 영상 정보, 평점, 태그, 액션, 비슷한 영상을 한 화면에서 보기 |
| **플레이어 조작** | 탐색, 속도, 스트림, 전체화면, 잠금, 이전/다음, 화면 비율 제어 |

## 설치하기

1. Android 휴대폰에서 [최신 릴리즈](https://github.com/gomeng-dev/stash-player-android/releases)를 엽니다.
2. APK 파일을 다운로드합니다.
3. APK를 열고, Android가 요청하면 브라우저나 파일 관리자에 APK 설치 권한을 허용합니다.
4. **Stash Player**를 실행하고 Stash 서버를 연결합니다.

> 현재 공개 릴리스는 **v1.7.2**입니다.

## 필요 조건

- Android 10 이상
- 휴대폰에서 접근 가능한 Stash 서버
- 다음 중 하나의 연결 방식
  - 인증을 끈 신뢰 가능한 로컬 서버
  - Stash API Key
  - Stash 아이디/비밀번호 로그인
- 선택 사항: 더 풍부한 비슷한 영상 추천을 위한 Stash Hybrid Recommendations Engine 플러그인

## 처음 연결하기

첫 실행 화면에서 Stash 서버 주소를 입력하고, 서버 설정에 맞는 인증 방식을 선택합니다.

자주 쓰는 예시:

- `http://192.168.0.10:9999`
- `http://stash.local:9999`
- 직접 구성한 HTTPS reverse proxy 주소

**연결 테스트**를 눌러 성공하면 저장하세요. 이후 홈 화면이 열립니다.

## 주요 기능

### 홈

홈은 감상을 시작하는 빠른 출발점입니다. 최근 보던 영상을 이어보고, 탐색/숏츠로 이동하고, 재생 대기열·나중에 보기·즐겨찾기를 바로 열 수 있습니다.

### 탐색

탐색은 browse와 search를 하나로 합친 화면입니다. 검색어를 입력해도, 검색어 없이 전체 라이브러리를 둘러봐도 같은 도구를 사용할 수 있습니다.

- 태그, 날짜, 길이, 평점, 화질, 시청 상태 필터
- 로컬 즐겨찾기와 저장 필터
- 무작위 정렬과 다시 섞기
- 격자/목록 보기
- 선택 재생, 대기열 추가, 삭제 같은 다중 선택 작업

### 숏츠

숏츠는 짧은 영상을 세로 피드처럼 넘겨 보는 화면입니다. 주변 항목을 미리 준비해 스와이프를 부드럽게 만들고, 피드백은 기기 안에 저장합니다.

사용할 수 있는 동작:

- 탭해서 재생/일시정지
- 스와이프로 다음 영상 이동
- 더블탭으로 좋아요
- 길게 눌러 임시 1.5배속 재생
- 하단 탐색 바를 드래그해 정확히 이동
- 관심없음 표시 또는 삭제

### Watch page와 플레이어

Watch page는 영상, 액션, 메타데이터, 추천을 한 화면에 모읍니다. 플레이어는 전체화면, 세로 watch-page 모드, 더블탭 탐색, 가로 스크럽, 좌우 밝기/볼륨 제스처, 길게 눌러 배속 유지, 잠금 모드, 스트림 선택, 자막, PiP, 화면 방향 제어, 재생목록 이동을 지원합니다.

### 로컬 목록

아래 목록은 휴대폰에 저장되어 Stash 서버 데이터를 꼭 바꾸지 않고도 감상 흐름을 정리할 수 있습니다.

- **재생 대기열** — 지금 이어서 볼 목록
- **나중에 보기** — 나중에 볼 영상
- **즐겨찾기** — 이 앱 안에서만 쓰는 로컬 즐겨찾기
- **재생 기록** — 홈과 대기열 화면의 이어보기에 쓰는 로컬 기록

## 추천

Stash 서버에 Stash Hybrid Recommendations Engine 플러그인이 설치되고 활성화되어 있으면 앱은 이를 기본 추천 소스로 사용합니다. 플러그인이 없거나, 꺼져 있거나, 사용할 수 있는 결과가 없으면 Stash 기본 추천 데이터로 대체합니다.

기본 사용 흐름에서는 별도의 레거시 추천 HTTP 서버가 필요하지 않습니다.

## 개인정보와 보안

Stash Android Player는 사용자의 Stash 서버에 직접 연결하는 클라이언트입니다.

- 서버 설정, API Key, 세션 쿠키, 로컬 목록, 재생 기록, 숏츠 피드백은 기기에 저장됩니다.
- ID/비밀번호 로그인은 세션 갱신에 필요한 정보를 저장합니다. 기기 백업이나 로그를 공개하지 마세요.
- 최근 앱 화면 숨기기 옵션으로 Android 최근 앱 목록의 미리보기를 숨길 수 있습니다.
- 디버그 로그는 민감한 인증 정보를 숨기도록 처리합니다.
- 직접 스크린샷을 공유할 때는 실제 서버 주소, 파일명, 인증 정보, 개인 미디어가 보이지 않게 주의하세요.

## 문제 해결

| 문제 | 확인할 것 |
| --- | --- |
| 연결 실패 | 휴대폰이 같은 네트워크, VPN, 또는 reverse proxy를 통해 Stash 주소에 접근 가능한지 확인하세요. |
| 썸네일이 보이지 않음 | 서버 연결 테스트를 다시 실행하고, 현재 인증 방식이 Stash에서 유효한지 확인하세요. |
| 앱 재시작 후 로그인이 풀림 | 설정에서 아이디/비밀번호를 다시 입력해 세션 갱신 정보를 업데이트하세요. |
| 숏츠가 비어 있음 | 설정의 숏츠 최대 길이보다 짧은 영상이 라이브러리에 있는지 확인하세요. |
| 추천이 비어 있음 | Hybrid Recommendations 플러그인이 켜져 있는지 확인하거나 Stash 기본 추천 fallback을 사용하세요. |
| APK 설치가 막힘 | APK를 연 브라우저/파일 관리자/설치 앱에 APK 설치 권한을 허용하세요. |

## 개발자용 정보

일반 사용자를 위한 설치와 사용법은 위에 정리되어 있습니다. 빌드 방법, 프로젝트 구조, 서명, 검증 명령은 [DEVELOPMENT.md](DEVELOPMENT.md)에 있습니다.

로컬 debug APK 빌드:

```bash
./gradlew :app:assembleDebug
```

## 라이선스

MIT. 자세한 내용은 [LICENSE](LICENSE)를 참고하세요.
