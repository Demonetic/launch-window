FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src
RUN mvn --batch-mode --no-transfer-progress package -DskipTests


FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S launchwindow \
    && adduser -S launchwindow -G launchwindow

WORKDIR /app

COPY --from=build /workspace/target/backend-0.0.1-SNAPSHOT.jar app.jar

USER launchwindow

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]