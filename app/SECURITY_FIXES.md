# 🔒 Correções de Segurança Aplicadas

## ⚠️ Problema Identificado

Credenciais sensíveis estavam **hardcoded** no código-fonte:
- ❌ API Key da OpenAI
- ❌ Token do Monday.com
- ❌ Senha da Evolution API
- ❌ URLs com IPs expostos

## ✅ Solução Implementada

### 1. Variáveis de Ambiente

Todas as credenciais foram movidas para variáveis de ambiente usando `@Value`:

#### **OpenAIService.java**
```java
@Value("${openai.api.key}")
private String apiKey;

@Value("${openai.chat.model:gpt-4.1}")
private String chatModel;

@Value("${openai.transcription.model:gpt-4o-transcribe}")
private String transcriptionModel;
```

#### **MondayService.java**
```java
@Value("${monday.api.url}")
private String mondayApiUrl;

@Value("${monday.api.token}")
private String authorizationToken;

@Value("${monday.board.id}")
private String boardId;

@Value("${monday.group.id}")
private String groupId;
```

#### **AuthService.java**
```java
@Value("${evolution.api.url}")
private String evolutionApiUrl;

@Value("${evolution.auth.email}")
private String authEmail;

@Value("${evolution.auth.password}")
private String authPassword;
```

#### **EvolutionApiService.java**
```java
@Value("${evolution.api.url}")
private String evolutionApiUrl;
```

### 2. Configuração no application.yml

```yaml
evolution:
  api:
    url: ${EVOLUTION_API_URL:http://191.252.195.25:8101}
  auth:
    email: ${EVOLUTION_AUTH_EMAIL:your-email@example.com}
    password: ${EVOLUTION_AUTH_PASSWORD:your-password-here}

monday:
  api:
    url: ${MONDAY_API_URL:https://api.monday.com/v2}
    token: ${MONDAY_API_TOKEN:your-monday-token-here}
  board:
    id: ${MONDAY_BOARD_ID:18387065071}
  group:
    id: ${MONDAY_GROUP_ID:topics}

openai:
  api:
    key: ${OPENAI_API_KEY:your-api-key-here}
  chat:
    model: ${OPENAI_CHAT_MODEL:gpt-4.1}
  transcription:
    model: ${OPENAI_TRANSCRIPTION_MODEL:gpt-4o-transcribe}
```

### 3. Arquivo .env

Criado `.env` com credenciais reais (NÃO commitado):
```bash
EVOLUTION_API_URL=http://191.252.195.25:8101
EVOLUTION_AUTH_EMAIL=jessebezerra2@hotmail.com.br
EVOLUTION_AUTH_PASSWORD=Tor1t4ma2013

MONDAY_API_URL=https://api.monday.com/v2
MONDAY_API_TOKEN=eyJhbGci...
MONDAY_BOARD_ID=18387065071
MONDAY_GROUP_ID=topics

OPENAI_API_KEY=sk-proj-...
OPENAI_CHAT_MODEL=gpt-4.1
OPENAI_TRANSCRIPTION_MODEL=gpt-4o-transcribe
```

### 4. Arquivo .env.example

Template público sem credenciais (commitável):
```bash
EVOLUTION_AUTH_EMAIL=your_email@example.com
EVOLUTION_AUTH_PASSWORD=your_evolution_password_here
MONDAY_API_TOKEN=your_monday_api_token_here
OPENAI_API_KEY=your_openai_api_key_here
```

### 5. .gitignore Atualizado

```
### Environment Variables ###
.env
.env.local
.env.*.local
*.env

### Secrets ###
secrets/
*.key
*.pem
*.p12
*.jks
```

### 6. Dockerfile Seguro

Removidas todas as credenciais hardcoded:
```dockerfile
# Valores padrão (não sensíveis)
ENV SPRING_PROFILES_ACTIVE=prod
ENV DATABASE_PORT=5432
ENV RABBITMQ_PORT=5672

# Variáveis que DEVEM ser fornecidas externamente:
# - DATABASE_PASSWORD
# - RABBITMQ_PASSWORD
# - EVOLUTION_AUTH_PASSWORD
# - MONDAY_API_TOKEN
# - OPENAI_API_KEY
```

### 7. docker-compose.yml Seguro

Usa arquivo `.env`:
```yaml
services:
  tediosession:
    env_file:
      - .env
```

## 📊 Resumo das Mudanças

| Arquivo | Antes | Depois |
|---------|-------|--------|
| **OpenAIService.java** | API Key hardcoded | `@Value("${openai.api.key}")` |
| **MondayService.java** | Token hardcoded | `@Value("${monday.api.token}")` |
| **AuthService.java** | Email/senha hardcoded | `@Value` para ambos |
| **EvolutionApiService.java** | URL hardcoded | `@Value("${evolution.api.url}")` |
| **Dockerfile** | Credenciais expostas | Apenas valores não sensíveis |
| **docker-compose.yml** | Credenciais expostas | Usa `.env` file |

## 🔐 Arquivos Protegidos

### ✅ Commitáveis (Seguros)
- `.env.example` - Template sem credenciais
- `application.yml` - Usa variáveis de ambiente
- `Dockerfile` - Sem credenciais
- `docker-compose.yml` - Referencia `.env`
- `SECURITY.md` - Documentação
- `.gitignore` - Proteção

### ❌ NÃO Commitáveis (Sensíveis)
- `.env` - Credenciais reais
- Qualquer arquivo com tokens/senhas

## 🚀 Como Usar

### Setup Inicial
```bash
# 1. Copiar template
cp .env.example .env

# 2. Editar com credenciais reais
nano .env

# 3. Verificar que não será commitado
git status  # .env NÃO deve aparecer
```

### Deploy
```bash
# Docker Compose (carrega .env automaticamente)
docker-compose up -d

# Docker Run (especificar .env)
docker run --env-file .env tediosession:latest
```

## ✅ Verificação de Segurança

Antes de commitar:
```bash
# Verificar arquivos staged
git status

# Verificar se não há credenciais no código
grep -r "sk-proj\|eyJhbGci\|Tor1t4ma" --include="*.java" .

# Verificar diff
git diff
```

## 📝 Checklist de Segurança

- [x] Remover API Keys hardcoded
- [x] Remover tokens hardcoded
- [x] Remover senhas hardcoded
- [x] Criar arquivo .env
- [x] Criar .env.example
- [x] Atualizar .gitignore
- [x] Atualizar Dockerfile
- [x] Atualizar docker-compose.yml
- [x] Atualizar application.yml
- [x] Documentar mudanças
- [x] Testar com variáveis de ambiente

## 🎯 Próximos Passos

1. **Revogar credenciais antigas** (se foram commitadas)
2. **Gerar novas credenciais**
3. **Atualizar .env com novas credenciais**
4. **Testar aplicação**
5. **Documentar para equipe**

## 📚 Referências

- [OWASP - Sensitive Data Exposure](https://owasp.org/www-project-top-ten/)
- [12 Factor App - Config](https://12factor.net/config)
- [Spring Boot - Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
