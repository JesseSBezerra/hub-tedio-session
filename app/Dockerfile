# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

# Copia o pom.xml e baixa as dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código fonte
COPY src ./src

# Compila a aplicação
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Instala curl para health check
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Cria usuário não-root para segurança
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copia o JAR da stage de build
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta da aplicação
EXPOSE 8103

# Variáveis de ambiente (devem ser fornecidas via .env ou docker-compose)
# IMPORTANTE: Não commitar credenciais no Dockerfile!
# Use .env file ou passe via -e no docker run

# Valores padrão (não sensíveis)
ENV SPRING_PROFILES_ACTIVE=prod
ENV DATABASE_PORT=5432
ENV RABBITMQ_PORT=5672
ENV RABBITMQ_QUEUE_NAME=tedio-message-consumer
ENV RABBITMQ_EXCHANGE_NAME=tedio-exchange
ENV RABBITMQ_ROUTING_KEY=tedio.message
ENV MONDAY_API_URL=https://api.monday.com/v2
ENV MONDAY_BOARD_ID=18387065071
ENV MONDAY_GROUP_ID=topics
ENV OPENAI_CHAT_MODEL=gpt-4.1
ENV OPENAI_TRANSCRIPTION_MODEL=gpt-4o-transcribe

# Variáveis que DEVEM ser fornecidas externamente:
# - DATABASE_HOST
# - DATABASE_NAME
# - DATABASE_USER
# - DATABASE_PASSWORD
# - RABBITMQ_HOST
# - RABBITMQ_USERNAME
# - RABBITMQ_PASSWORD
# - EVOLUTION_API_URL
# - EVOLUTION_AUTH_EMAIL
# - EVOLUTION_AUTH_PASSWORD
# - MONDAY_API_TOKEN
# - OPENAI_API_KEY

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8103/actuator/health || exit 1

# Comando de inicialização
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", \
    "-jar", \
    "app.jar"]
