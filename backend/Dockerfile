FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
RUN mkdir /app/uploads && chown -R spring:spring /app
USER spring:spring

COPY --from=build --chown=spring:spring /app/target/*.jar app.jar
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q -O - http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
