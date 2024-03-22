# Étape de build
FROM maven:3.8.4-openjdk-17-slim AS build

# Copier les fichiers du projet dans le conteneur
COPY src /home/app/src
COPY pom.xml /home/app

# Compiler et empaqueter l'application
RUN mvn -f /home/app/pom.xml clean package

# Étape d'exécution
FROM openjdk:17-oracle

# Copier le JAR du build précédent
COPY --from=build /home/app/target/ChessResultsStats-0.0.1-SNAPSHOT.jar /usr/local/lib/ChessResultsStats.jar

# Exposer le port sur lequel l'application s'exécute
EXPOSE 8080

# Exécuter l'application
ENTRYPOINT ["java","-jar","/usr/local/lib/ChessResultsStats.jar"]
