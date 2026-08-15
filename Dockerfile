FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S flower && adduser -S flower -G flower

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar
RUN mkdir -p /app/uploads/products && chown -R flower:flower /app

USER flower

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
