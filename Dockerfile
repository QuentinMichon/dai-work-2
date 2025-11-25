FROM eclipse-temurin:21-jdk
LABEL authors="quentinMichon-gianniBee"

WORKDIR /app

COPY target/dai-work-2-1.0-SNAPSHOT.jar app.jar

# ouvertur du port si jamais on veut débug via ncat en dehors de Docker
EXPOSE 4444

ENTRYPOINT ["java", "-jar", "app.jar"]

CMD ["SERVER"]