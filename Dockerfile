FROM openjdk:21-jdk-slim

WORKDIR /app

COPY . ./

RUN ./mvnw package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/springcourse-0.0.1-SNAPSHOT.jar"]
