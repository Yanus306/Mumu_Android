# Mumu_Android

반려동물 프로필, 행동 영상 분석, 일기, 커뮤니티, 동물병원 조회를 제공하는 Android 앱입니다.

## 다른 환경에서 작업 시작

현재 작업 브랜치: `feature/21_diary_my_figma`

```bash
git clone --branch feature/21_diary_my_figma https://github.com/Yanus306/Mumu_Android.git
cd Mumu_Android
```

이미 저장소가 있다면 변경사항을 먼저 커밋하거나 보관한 뒤 실행합니다.

```bash
git fetch origin
git switch feature/21_diary_my_figma
git pull --ff-only origin feature/21_diary_my_figma
```

새 브랜치는 `feature/` 접두사를 사용합니다.

## 개발 환경과 실행

- JDK 17, Android SDK Platform 36을 준비합니다.
- Gradle 8.11.1은 저장소의 Wrapper가 사용합니다. 첫 동기화에는 인터넷 연결이 필요합니다.
- Android Studio에서 프로젝트를 열고 SDK 경로와 Gradle JDK를 설정한 뒤 Gradle Sync를 실행합니다.
- 각 환경의 SDK 경로는 `local.properties`에 설정합니다. 이 파일과 서명 키는 Git에 포함하지 않습니다.
- 최소 실행 버전은 Android 8.0(API 26)입니다. `app` 구성을 선택해 기기나 에뮬레이터에서 실행합니다.

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:ktlintCheck
```

생성 APK: `app/build/outputs/apk/debug/app-debug.apk`

API 주소는 `app/src/main/java/kr/ac/anu/mumu/di/NetworkModule.kt`에 설정되어 있습니다.
로그인과 데이터 기능을 확인하려면 서버 연결 및 테스트 계정이 필요합니다.

## 이어서 작업할 내용

[작업 인계 메모](docs/HANDOFF.md)에 최근 수정, 분석 모드 설정, 검증 결과와 남은 확인 사항을 정리했습니다.
