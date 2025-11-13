FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl product-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/product-service/target/product-service-*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
