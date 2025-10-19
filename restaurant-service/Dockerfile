# Stage 1: build with Maven
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /workspace

# copy pom + mvn wrapper to speed up caching
COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn

# download dependencies
RUN mvn -B dependency:go-offline

# copy sources and build
COPY src ./src
RUN mvn -B -DskipTests package

# Stage 2: runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app

# create non-root user
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# copy jar built by Maven
COPY --from=build /workspace/target/*.jar /app/app.jar
RUN chown -R appuser:appgroup /app

USER appuser
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]