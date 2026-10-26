FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
COPY target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]