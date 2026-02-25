import subprocess
import time

# 센서 ID 변수 선언
SPEED_ID = "291504647"
FUEL_ID = "291504903"
GEAR_ID = "289408000"
RPM_ID = "291505923"

current_fuel = 50000.0

def inject_all(speed, fuel, rpm, gear):
    # 여러 명령어를 하나의 adb shell 호출로 묶어 오버헤드 최소화
    commands = (
        f"cmd car_service inject-vhal-event {SPEED_ID} 0 {float(speed)}; "
        f"cmd car_service inject-vhal-event {FUEL_ID} 0 {float(fuel)}; "
        f"cmd car_service inject-vhal-event {RPM_ID} 0 {float(rpm)}; "
        f"cmd car_service inject-vhal-event {GEAR_ID} 0 {int(gear)}"
    )
    subprocess.run(["adb", "shell", commands], capture_output=True)

def drive_ultra_stable(name, duration, speed, rpm, eff_km_l):
    global current_fuel
    start_time = time.time()
    last_time = start_time
    interval = 0.2 

    print(f"--- [{name}] 구간 시작 ({duration}초) ---")
    while time.time() - start_time < duration:
        now = time.time()
        dt = now - last_time
        last_time = now

        # 💡 수정된 연료 소모 로직 (공회전 방어)
        if speed > 0 and eff_km_l > 0:
            # 주행 중: (속도 km/h * 시간 h) / 연비 km/L * 1000 = mL
            hours = dt / 3600.0
            ml_consumed = (speed * hours / eff_km_l) * 1000.0
        else:
            # 💡 공회전 중: 시간당 약 800mL 소모한다고 가정
            ml_consumed = (800.0 / 3600.0) * dt
            
        current_fuel -= ml_consumed

        inject_all(speed, current_fuel, rpm, 8) # 8은 DRIVE 기어
        
        # 루프 지연 시간을 고려한 정밀한 sleep
        elapsed = time.time() - now
        sleep_time = max(0, interval - elapsed)
        time.sleep(sleep_time)

def run_master_scenario():
    print("🚀 [최종 데이터 검증] 시뮬레이션 시작")
    # 초기 기어 설정 (Drive)
    subprocess.run(["adb", "shell", f"cmd car_service inject-vhal-event {GEAR_ID} 0 8"])
    time.sleep(1)

    # STEP 1: 공회전 
    drive_ultra_stable("IDLING", 10, 0.0, 800.0, 0.0)

    # STEP 1.5: 부드러운 가속 (갑자기 80km/h로 튀는 것 방지)
    drive_ultra_stable("ACCEL", 5, 40.0, 2500.0, 8.0)

    # STEP 2: 정속 주행
    drive_ultra_stable("CRUISE", 30, 80.0, 2000.0, 15.0)

    # STEP 3: 타력 주행 (연비 매우 높음)
    drive_ultra_stable("COASTING", 20, 60.0, 900.0, 50.0)

    print("\n🏁 시나리오 종료")
    inject_all(0.0, current_fuel, 0.0, 4) # 4는 Park 기어

if __name__ == "__main__":
    run_master_scenario()