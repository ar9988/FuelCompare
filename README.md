# 🚗 AAOS 기반 실시간 주행 모니터링 & 음성 비서 앱

> **Android Automotive OS(AAOS)** 환경에서 VHAL 센서 데이터를 실시간으로 분석하여 연비 모니터링, 운전 습관 분석, 그리고 지능형 음성 피드백(STT/TTS)을 제공하는 안드로이드 애플리케이션입니다.

## 🎥 Demo / Preview



https://github.com/user-attachments/assets/9caf0124-2300-49d3-ba45-f522284a8770




## ✨ 핵심 기능 (Key Features)

### 1. 실시간 차량 센서 모니터링 (VHAL 연동)
- 차량의 **Speed, Fuel, RPM, Gear** 센서 데이터를 통합하여 실시간 상태 분석
- 발차, 정속 주행(Cruise), 타력 주행(Coasting), 공회전, 급가속/급감속 등 세밀한 주행 상태 감지

### 2. 지능형 음성 비서 (STT & TTS)
- 운전 중 시선 분산을 막기 위해 **"연비"** 등의 특정 트리거 명령어를 인식(STT)하여 현재 상태 브리핑
- 주행 이벤트(급가속 경고, 정속 주행 안내 등) 발생 시 **TTS 피드백** 제공

### 3. 직관적인 대시보드 UI (Jetpack Compose)
- 차량용 디스플레이 환경(Glanceability)에 맞춘 **Canvas 기반 커스텀 연비 게이지** 구현

### 4. 주행 생명주기 관리 및 리포트
- 시동 대기 -> 주행 중 -> 기어 P단(주행 종료)의 생명주기를 감지하여 자동으로 요약 리포트(Trip Summary) 화면으로 전환 및 운전 통계 제공

---

## 🛠 기술 스택 (Tech Stack)
- **Language:** Kotlin
- **UI:** Jetpack Compose, Navigation Compose
- **Architecture:** Clean Architecture, MVVM (UDF - State/Event/SideEffect Pattern)
- **Asynchronous & Reactive:** Coroutines, Kotlin Flow (`StateFlow`, `SharedFlow`)
- **Dependency Injection:** Dagger-Hilt
- **Android System:** `SpeechRecognizer`, `TextToSpeech (TTS)`, Car VHAL (Vehicle Hardware Abstraction Layer)
- **Testing:** Python (VHAL Event Injector 시나리오 스크립트 작성)

---

## 🔥 트러블슈팅 및 기술적 성과 (Troubleshooting & Achievements)

### 1. 연비 계산 엣지 케이스(Edge Case) 방어 및 안정화
- **문제:** 시뮬레이션 환경 및 실제 차량의 발차 극초반 구간에서 분모(연료 소모량)가 0에 가까워져 연비가 무한대로 치솟는 `Divide by Zero` 유사 현상(30km/L 초과 버그) 발생.
- **해결:** - 미세한 센서 노이즈로 인한 잦은 초기화를 막기 위해 연료 증가량에 임계값(Threshold)을 부여하여 필터링 적용.
  - 최소 주행 거리(200m) 및 최소 연료 소모량 도달 전까지는 계산을 보류하고 `Initializing` 상태를 유지하도록 초기 방어 로직 구현.
  - UI 일관성을 위해 계산된 실시간 연비의 최대치를 `coerceIn(0.1f, 30.0f)`로 클램핑(Clamping) 처리하고, 이전 연비 값에 가중치를 두는 스무딩(Smoothing) 기법을 적용하여 튀는 값을 부드럽게 보정.

### 2. VHAL 에뮬레이터 기본 노이즈와 시나리오 데이터 충돌 해결
- **문제:** 안드로이드 에뮬레이터 VHAL 기본 설정에서 백그라운드로 자동 주입되는 더미(Dummy) 센서 값과, 테스트를 위해 파이썬(Python) 스크립트로 직접 주입하는 시나리오 값이 충돌하여 데이터 신뢰성이 심각하게 훼손되는 문제 확인.
- **해결:** 에뮬레이터 자체적으로 발생시키는 특정 기본 노이즈 값을 매직 넘버(Magic Number)로 정의하여 예외 처리함. 이를 통해 파이썬 스크립트로 의도하여 주입한 유효 데이터 스트림만 연비 및 주행 계산 로직에 반영되도록 정확도를 대폭 향상시킴.

### 3. 다중 데이터 스트림 결합을 통한 연산 편차 및 동기화 제어
- **문제:** 속도, 연료, RPM, 기어 등 파편화된 다중 센서 데이터 스트림이 0.2초 단위의 짧은 주기로 비동기적으로 수신되면서, 동일한 테스트 시나리오를 반복 실행함에도 타이밍 이슈로 인해 주행 판정 결과의 편차가 크게 발생하는 현상 발생.
- **해결:** `flatMapLatest`와 `combine` 등 Kotlin Flow의 반응형(Reactive) 연산자를 적극 활용하여, 개별적으로 들어오는 4개의 센서 스트림을 `VehicleMonitoringUseCase`에서 단일 `VehicleStatus` 스트림으로 동기화 및 병합. 흩어져 있던 연산 지점을 한 곳으로 통합(Single Source of Truth)하여 비동기 타이밍 이슈를 해결하고, 테스트 결과의 편차를 완벽하게 제어하는 데 성공.
