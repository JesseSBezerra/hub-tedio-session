# 🚀 GitHub Actions - Docker Build & Push

Este workflow automatiza o build e push da imagem Docker para o Docker Hub.

## 📋 Configuração Necessária

### 1. Criar Secret no GitHub

Você precisa adicionar seu token/senha do Docker Hub como secret no repositório:

1. Acesse: `Settings` → `Secrets and variables` → `Actions`
2. Clique em `New repository secret`
3. Nome: `DOCKER_PASSWORD`
4. Valor: Seu token do Docker Hub ou senha

### Como gerar um token no Docker Hub:

1. Acesse: https://hub.docker.com/settings/security
2. Clique em `New Access Token`
3. Nome: `github-actions-tediohook`
4. Permissões: `Read, Write, Delete`
5. Copie o token gerado

## 🎯 Quando o Workflow é Executado

O workflow é disparado automaticamente quando:

- ✅ Push para branches: `main`, `master`, `develop`
- ✅ Pull Request para `main` ou `master`
- ✅ Push de tags com formato `v*` (ex: `v1.0.0`)
- ✅ Manualmente via `Actions` tab (workflow_dispatch)

## 🏷️ Tags Geradas

O workflow gera tags automaticamente baseado no evento:

### Push para branch main/master:
- `jessebezerra/tediohook:latest`
- `jessebezerra/tediohook:main` (ou master)

### Push de tag v1.2.3:
- `jessebezerra/tediohook:1.2.3`
- `jessebezerra/tediohook:1.2`
- `jessebezerra/tediohook:1`
- `jessebezerra/tediohook:latest`

### Pull Request:
- `jessebezerra/tediohook:pr-123`

## 🚀 Recursos do Workflow

- ✅ **Multi-platform**: Build para `linux/amd64` e `linux/arm64`
- ✅ **Cache inteligente**: Usa registry cache para builds mais rápidos
- ✅ **Metadata automático**: Tags e labels gerados automaticamente
- ✅ **Summary**: Resumo visual no GitHub Actions

## 📝 Exemplo de Uso Manual

Para disparar o workflow manualmente:

1. Acesse a aba `Actions` no GitHub
2. Selecione `Docker Build and Push`
3. Clique em `Run workflow`
4. Selecione a branch
5. Clique em `Run workflow`

## 🔍 Verificar Imagem Publicada

Após o workflow executar com sucesso:

```bash
# Pull da imagem
docker pull jessebezerra/tediohook:latest

# Verificar tags disponíveis
# Acesse: https://hub.docker.com/r/jessebezerra/tediohook/tags
```

## 🐛 Troubleshooting

### Erro de autenticação:
- Verifique se o secret `DOCKER_PASSWORD` está configurado corretamente
- Verifique se o username `jessebezerra` está correto no workflow

### Build falha:
- Verifique os logs do workflow no GitHub Actions
- Teste o build localmente: `docker build -t test ./app`

### Cache não funciona:
- O cache é armazenado no Docker Hub como uma imagem especial
- Primeira execução sempre será mais lenta
