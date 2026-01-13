## moduwa-frontend

- 모두와 AAC 프로젝트의 프론트 레포지토리입니다.

## 📂 Project Structure

```text
com.example.aac
├── data                # 데이터 레이어 (Data Layer)
│   ├── remote          # API 통신 (Retrofit, API 정의 등)
│   ├── local           # 로컬 DB (Room, DataStore)
│   └── repository      # 데이터 소스 통합 및 추상화 구현
├── domain              # 비즈니스 로직 레이어 (Domain Layer)
│   ├── model           # 도메인 모델 (Pure Kotlin 객체)
│   └── usecase         # 기능별 비즈니스 로직 단위
├── ui                  # 프레젠테이션 레이어 (Presentation Layer)
│   ├── features        # 기능 단위 화면 구성
│   │   └── main        # 메인 화면 (MainScreen, MainViewModel 등)
│   ├── components      # 공통 재사용 UI 컴포넌트 (Buttons, Cards 등)
│   └── theme           # 디자인 시스템 (Color, Shape, Type, Theme)
└── util                # 유틸리티 (Constants, Extensions)
```

## 🌿 Branch Convention

| 브랜치 접두어 | 대상 | 설명 |
| :--- | :--- | :--- |
| **`main`** | 배포용 | 최종 확인된 안정적인 코드만 병합 |
| **`develop`** | 통합용 | 다음 배포를 위해 기능별 브랜치가 모이는 기본 브랜치 |
| **`feature/`** | 신규 기능 | 각 기능 개발을 위한 독립적인 브랜치 (예: `feature/main-ui`) |
| **`fix/`** | 버그 수정 | 발생한 런타임 에러나 로직 오류 수정 (예: `fix/casting-error`) |
| **`docs/`** | 문서 작업 | README, 설정 파일 등 문서 관련 작업 |

---

## 💬 Commit Convention

메시지 형식: `Type: Subject` (예: `feat: 카테고리 선택 기능 추가`)

| Type | 설명 | 상세 내용 |
| :--- | :--- | :--- |
| **feat** | 기능 추가 | 새로운 기능(UI, 로직)이 추가된 경우 |
| **fix** | 버그 수정 | 잘못된 로직이나 크래시 등의 오류를 수정한 경우 |
| **docs** | 문서 수정 | README.md 등 문서 작업만 진행한 경우 |
| **style** | 코드 정돈 | 기능 변화 없이 포맷팅, 세미콜론 등 스타일만 수정한 경우 |
| **refactor** | 리팩토링 | 코드 구조 개선 (기능은 동일) |
| **chore** | 설정 변경 | 라이브러리 추가, Gradle 설정 등 빌드 관련 수정 |

---

## Tech Stack

| 구분 | 내용 |
| :--- | :--- |
| **Language** | Kotlin 1.9.x |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM + Clean Architecture |
| **Target SDK** | API 35 (Android 15) |
| **Min SDK** | API 26 (Oreo) |

