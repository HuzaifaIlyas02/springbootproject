FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl discovery-server -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/discovery-server/target/discovery-server-*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
