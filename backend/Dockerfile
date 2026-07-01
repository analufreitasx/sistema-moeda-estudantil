FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src

RUN mvn -DskipTests package \
    && find target -maxdepth 1 -type f -name "*.jar" ! -name "original-*.jar" -exec cp {} app.jar \;

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/app.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
