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

# create uploads directory with proper permissions
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app/uploads

# copy jar built by Maven
COPY --from=build /workspace/target/*.jar /app/app.jar

# copy pre-existing images from build stage
COPY --from=build /workspace/src/main/resources/uploads/* /app/uploads/

# set ownership
RUN chown -R appuser:appgroup /app

USER appuser
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]