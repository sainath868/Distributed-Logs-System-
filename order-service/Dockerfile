FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -DskipTests -pl order-service -am clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/order-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
