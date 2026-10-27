FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Resolve dependencies first for better layer caching
RUN ./mvnw -q -B dependency:go-offline

COPY src src
# application.properties is gitignored; bake the template into the image
RUN cp src/main/resources/application.properties.template \
       src/main/resources/application.properties

RUN ./mvnw -q -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
