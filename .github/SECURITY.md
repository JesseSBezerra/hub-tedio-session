# 🔒 Política de Segurança

## ⚠️ Informações Sensíveis

Este projeto foi configurado para **NUNCA** expor informações sensíveis no repositório.

### ✅ Boas Práticas Implementadas

1. **Variáveis de Ambiente**
   - Todas as credenciais são passadas via variáveis de ambiente
   - Arquivo `.env` está no `.gitignore`
   - Arquivo `.env.example` fornece template sem dados reais

2. **application.properties**
   - Usa placeholders: `${DATABASE_HOST:default}`
   - Valores padrão são genéricos
   - Configurações reais vêm de variáveis de ambiente

3. **Docker**
   - Removido `docker-compose.yml` com credenciais hardcoded
   - Documentação usa placeholders: `your-host`, `your-password`
   - Imagens públicas não contêm credenciais

4. **Git**
   - `.gitignore` configurado para ignorar `.env`
   - Histórico limpo de credenciais

### ❌ NUNCA Commitar

- ❌ Arquivos `.env` com credenciais reais
- ❌ `docker-compose.yml` com senhas
- ❌ Configurações com IPs/hosts reais
- ❌ Tokens ou API keys
- ❌ Certificados ou chaves privadas

### 🔐 Como Configurar Localmente

1. Copie o arquivo de exemplo:
```bash
cp app/.env.example app/.env
```

2. Edite com suas credenciais:
```bash
# Edite app/.env com seus dados reais
```

3. O arquivo `.env` será ignorado pelo Git automaticamente

### 🚨 Se Credenciais Foram Expostas

Se você acidentalmente commitou credenciais:

1. **IMEDIATAMENTE** altere as senhas/tokens expostos
2. Remova o commit do histórico (use `git filter-branch` ou BFG Repo-Cleaner)
3. Force push (se possível)
4. Notifique a equipe

### 📝 Checklist Antes de Commit

- [ ] Nenhum arquivo `.env` será commitado
- [ ] `application.properties` usa apenas placeholders
- [ ] Documentação usa exemplos genéricos
- [ ] Nenhuma senha ou token no código

### 🛡️ GitHub Secrets

Para CI/CD, use GitHub Secrets:
- `DOCKER_PASSWORD` - Token do Docker Hub
- Nunca exponha secrets em logs ou outputs

## 📞 Reportar Vulnerabilidades

Se encontrar uma vulnerabilidade de segurança, por favor:
1. **NÃO** abra uma issue pública
2. Entre em contato diretamente com a equipe
3. Aguarde confirmação antes de divulgar
