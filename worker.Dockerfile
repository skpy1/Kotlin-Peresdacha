FROM gradle:8.11.1-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :worker:clean :worker:installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/worker/build/install/worker /app/worker
ENTRYPOINT ["/app/worker/bin/worker"]
