# 🐳 Docker - TedioSession

## 📋 Pré-requisitos

- Docker 20.10+
- Docker Compose 2.0+
- Arquivo `.env` configurado (veja [SECURITY.md](SECURITY.md))

## ⚠️ IMPORTANTE: Configuração Inicial

Antes de fazer o build, configure suas credenciais:

```bash
# 1. Copiar template
cp .env.example .env

# 2. Editar .env com suas credenciais reais
nano .env  # ou use seu editor preferido

# 3. Verificar que .env não será commitado
git status
```

**NUNCA commite o arquivo `.env` com credenciais reais!**

## 🚀 Build e Deploy

### Build da Imagem

```bash
docker build -t tediosession:latest .
```

### Executar com Docker Compose (Recomendado)

```bash
# Iniciar (usa .env automaticamente)
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar
docker-compose down

# Rebuild e restart
docker-compose up -d --build
```

### Executar com Docker Run

```bash
# Opção 1: Carregar do arquivo .env
docker run -d \
  --name tediosession-app \
  -p 8103:8103 \
  --env-file .env \
  tediosession:latest

# Opção 2: Passar variáveis individualmente (NÃO RECOMENDADO para produção)
docker run -d \
  --name tediosession-app \
  -p 8103:8103 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_HOST=seu_host \
  -e DATABASE_PASSWORD=sua_senha \
  -e RABBITMQ_PASSWORD=sua_senha \
  -e EVOLUTION_AUTH_PASSWORD=sua_senha \
  -e MONDAY_API_TOKEN=seu_token \
  -e OPENAI_API_KEY=sua_chave \
  tediosession:latest
```

## 🔍 Verificação

### Health Check

```bash
curl http://localhost:8103/actuator/health
```

### Logs

```bash
# Docker Compose
docker-compose logs -f tediosession

# Docker Run
docker logs -f tediosession-app
```

### Status do Container

```bash
docker ps | grep tediosession
```

## 🛠️ Comandos Úteis

### Acessar o Container

```bash
docker exec -it tediosession-app bash
```

### Ver Variáveis de Ambiente

```bash
docker exec tediosession-app env
```

### Reiniciar Container

```bash
docker restart tediosession-app
```

### Remover Container e Imagem

```bash
# Parar e remover container
docker stop tediosession-app
docker rm tediosession-app

# Remover imagem
docker rmi tediosession:latest
```

## 📦 Estrutura da Imagem

### Stage 1: Build
- Base: `maven:3.9.5-eclipse-temurin-17`
- Compila o projeto com Maven
- Gera o JAR

### Stage 2: Runtime
- Base: `eclipse-temurin:17-jre-jammy`
- Copia apenas o JAR compilado
- Usuário não-root para segurança
- Health check configurado

## 🔐 Segurança

- Container roda com usuário não-root (`spring:spring`)
- Variáveis sensíveis devem ser passadas via environment
- Recomendado usar secrets em produção

## 🌐 Portas

- **8103**: Porta da aplicação Spring Boot

## 📊 Recursos

### Limites Recomendados

```yaml
deploy:
  resources:
    limits:
      cpus: '2'
      memory: 2G
    reservations:
      cpus: '1'
      memory: 1G
```

## 🔄 CI/CD

### Build Automatizado

```bash
#!/bin/bash
VERSION=$(date +%Y%m%d-%H%M%S)
docker build -t tediosession:${VERSION} .
docker tag tediosession:${VERSION} tediosession:latest
```

### Push para Registry

```bash
# Docker Hub
docker tag tediosession:latest username/tediosession:latest
docker push username/tediosession:latest

# Private Registry
docker tag tediosession:latest registry.example.com/tediosession:latest
docker push registry.example.com/tediosession:latest
```

## 🐛 Troubleshooting

### Container não inicia

```bash
# Ver logs detalhados
docker logs tediosession-app

# Verificar health check
docker inspect tediosession-app | grep Health
```

### Problemas de conexão

```bash
# Testar conectividade com banco
docker exec tediosession-app curl -v telnet://191.252.195.25:5432

# Testar conectividade com RabbitMQ
docker exec tediosession-app curl -v telnet://191.252.195.25:5672
```

### Rebuild completo

```bash
docker-compose down -v
docker system prune -a
docker-compose up -d --build
```
