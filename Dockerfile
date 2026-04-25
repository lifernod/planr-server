FROM eclipse-temurin:25-jdk-alpine

WORKDIR /app

RUN addgroup --system spring && adduser --system spring --ingroup spring

COPY build/libs/*.jar app.jar

RUN chown spring:spring app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-XX:+UseStringDeduplication", "-jar", "app.jar"]