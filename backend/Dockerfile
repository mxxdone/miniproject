# 1. 빌드 스테이지 (Build Stage): 이미지 크기 최적화를 위한 Multi-Stage Build
FROM gradle:8.14.0-jdk21-jammy AS builder

# 1.1. Gradle Wrapper 파일 복사 (변경 빈도가 가장 낮음)
# 복사 경로는 빌드 컨텍스트인 프로젝트 루트(.) 기준
WORKDIR /app
COPY gradlew settings.gradle build.gradle /app/
COPY gradle /app/gradle
RUN chmod +x ./gradlew

# 1.2. 의존성 파일 복사 및 다운로드 (변경 빈도가 낮음)
# build.gradle만 변경되었을 경우 이 레이어부터 재빌드되어 속도 향상
# :backend:dependencies 명령어로 의존성 다운로드만 수행
# --no-daemon: 컨테이너 빌드 환경에서 데몬 사용을 비활성화하여 빌드 시간을 단축
COPY backend/build.gradle backend/
RUN ./gradlew :backend:dependencies --no-daemon

# 1.3. 소스 코드 복사 및 빌드 (변경 빈도가 높음)
# 소스 코드 변경 시 이 레이어부터 재빌드
COPY backend/src backend/src/
# -x test: Docker 빌드에서 테스트를 제외(테스트는 CI 단계에서 처리)
RUN ./gradlew :backend:bootJar -x test --no-daemon

# ----------------------------------------------------

# 2. 실행 스테이지 (Run Stage): 경량화 및 보안
FROM openjdk:21-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# 시간대 설정
ENV TZ=Asia/Seoul
# Spring Profiles는 env_file에서 주입되므로 여기서 명시할 필요는 없습니다.

# 2.1. JAR 파일 동적 복사 (최적화)
# 빌드 스테이지의 Jar 파일을 *.jar 와일드카드로 복사하여 파일명에 종속되지 않습니다.
# Jar 경로는 /app/backend/build/libs/miniproject-0.0.1-SNAPSHOT.jar 입니다.
COPY --from=builder /app/backend/build/libs/*.jar /app/app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행 명령
ENTRYPOINT ["java", "-jar", "/app/app.jar"]