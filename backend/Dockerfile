FROM gradle:8-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle installDist --no-daemon

FROM openjdk:17-slim
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/install/trustmesh-backend /app/trustmesh-backend
ENTRYPOINT ["/app/trustmesh-backend/bin/trustmesh-backend"]
