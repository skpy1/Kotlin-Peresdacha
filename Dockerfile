FROM gradle:8.11.1-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :app:clean :app:shadowJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app/build/libs/app-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
