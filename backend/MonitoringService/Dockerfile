FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw

RUN ./mvnw -q -B dependency:go-offline

COPY src src
RUN cp src/main/resources/application.properties.template \
       src/main/resources/application.properties

RUN ./mvnw -q -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
