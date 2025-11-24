# 🔐 Segurança - TedioSession

## ⚠️ IMPORTANTE: Gerenciamento de Credenciais

### 🚫 NÃO COMMITAR

**NUNCA** commite os seguintes arquivos com credenciais reais:
- `.env`
- Qualquer arquivo contendo tokens, senhas ou API keys
- Arquivos de configuração com credenciais hardcoded

### ✅ O QUE ESTÁ PROTEGIDO

O `.gitignore` está configurado para ignorar:
```
.env
.env.local
.env.*.local
*.env
secrets/
*.key
*.pem
*.p12
*.jks
```

## 📋 Setup de Credenciais

### 1. Copiar o Template

```bash
cp .env.example .env
```

### 2. Editar o .env

Abra o arquivo `.env` e preencha com suas credenciais reais:

```bash
# Database
DATABASE_PASSWORD=sua_senha_real_aqui

# RabbitMQ
RABBITMQ_PASSWORD=sua_senha_rabbitmq_aqui

# Evolution API
EVOLUTION_AUTH_EMAIL=seu_email@example.com
EVOLUTION_AUTH_PASSWORD=sua_senha_evolution_aqui

# Monday.com
MONDAY_API_TOKEN=seu_token_monday_aqui

# OpenAI
OPENAI_API_KEY=sua_chave_openai_aqui
```

### 3. Verificar Permissões

```bash
# Linux/Mac
chmod 600 .env

# Verificar que .env não está no git
git status
```

## 🔑 Credenciais Necessárias

### PostgreSQL
- **Host**: 191.252.195.25
- **Port**: 5432
- **Database**: tedioinfernal
- **User**: evolution
- **Password**: ⚠️ Obter do administrador

### RabbitMQ
- **Host**: 191.252.195.25
- **Port**: 5672
- **Username**: guest
- **Password**: ⚠️ Obter do administrador

### Evolution API
- **URL**: http://191.252.195.25:8101
- **Email**: ⚠️ Configurar conta
- **Password**: ⚠️ Obter do administrador

### Monday.com API
- **URL**: https://api.monday.com/v2
- **Token**: ⚠️ Gerar em https://monday.com/developers
- **Board ID**: 18387065071
- **Group ID**: topics

### OpenAI API
- **Key**: ⚠️ Gerar em https://platform.openai.com/api-keys
- **Chat Model**: gpt-4.1
- **Transcription Model**: gpt-4o-transcribe

## 🛡️ Boas Práticas

### Em Desenvolvimento

1. **Use .env local**
   ```bash
   # Nunca commite este arquivo
   .env
   ```

2. **Rotacione credenciais regularmente**
   - Tokens de API devem ser renovados periodicamente
   - Senhas devem seguir política de segurança

3. **Não compartilhe credenciais**
   - Use canais seguros (1Password, Vault, etc)
   - Nunca envie por email ou chat

### Em Produção

1. **Use Secrets Management**
   ```bash
   # Docker Swarm
   docker secret create db_password db_password.txt
   
   # Kubernetes
   kubectl create secret generic tediosession-secrets \
     --from-literal=database-password=xxx \
     --from-literal=rabbitmq-password=xxx
   ```

2. **Use variáveis de ambiente do CI/CD**
   - GitHub Actions: Secrets
   - GitLab CI: Variables
   - Jenkins: Credentials

3. **Limite acesso**
   - Princípio do menor privilégio
   - Auditoria de acessos
   - Logs de uso de credenciais

## 🔍 Verificação de Segurança

### Antes de Commitar

```bash
# Verificar se não há credenciais expostas
grep -r "password\|token\|key" --include="*.java" --include="*.yml" .

# Verificar status do git
git status

# Verificar diff antes do commit
git diff
```

### Scan de Segurança

```bash
# Usar ferramentas como:
# - git-secrets
# - truffleHog
# - gitleaks

# Exemplo com gitleaks
gitleaks detect --source . --verbose
```

## 🚨 Em Caso de Exposição

### Se credenciais foram commitadas:

1. **Revogar imediatamente**
   - Trocar todas as senhas expostas
   - Revogar tokens de API
   - Gerar novas credenciais

2. **Remover do histórico do Git**
   ```bash
   # CUIDADO: Reescreve histórico
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch .env" \
     --prune-empty --tag-name-filter cat -- --all
   
   # Forçar push (coordenar com equipe)
   git push origin --force --all
   ```

3. **Notificar equipe**
   - Informar sobre o incidente
   - Coordenar rotação de credenciais
   - Documentar lições aprendidas

## 📞 Contatos

- **Administrador de Sistemas**: [contato]
- **Segurança**: [contato]
- **Emergências**: [contato]

## 📚 Referências

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [12 Factor App - Config](https://12factor.net/config)
- [Docker Secrets](https://docs.docker.com/engine/swarm/secrets/)
- [Kubernetes Secrets](https://kubernetes.io/docs/concepts/configuration/secret/)
