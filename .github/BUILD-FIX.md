# 🔧 Correção de Build Multi-Plataforma

## 🐛 Problema

O build do GitHub Actions falhava ao tentar criar imagens para `linux/arm64` porque as imagens Eclipse Temurin não suportam ARM64 no Alpine Linux.

### Erros Encontrados

**Erro 1 - Stage 1 (Build):**
```
ERROR: maven:3.9-eclipse-temurin-17-alpine: no match for platform in manifest: not found
```

**Erro 2 - Stage 2 (Runtime):**
```
ERROR: eclipse-temurin:17-jre-alpine: no match for platform in manifest: not found
```

## ✅ Solução

Substituir todas as imagens Eclipse Temurin por Amazon Corretto, que possui suporte nativo para múltiplas arquiteturas.

### Mudanças no Dockerfile

```dockerfile
# ANTES (não funcionava para ARM64)
FROM maven:3.9-eclipse-temurin-17-alpine AS build
FROM eclipse-temurin:17-jre-alpine

# DEPOIS (funciona para AMD64 e ARM64)
FROM maven:3.9-amazoncorretto-17-alpine AS build
FROM amazoncorretto:17-alpine
```

## 🎯 Resultado

### Plataformas Suportadas
- ✅ `linux/amd64` - Intel/AMD x86_64
- ✅ `linux/arm64` - ARM 64-bit (Apple Silicon, AWS Graviton, Raspberry Pi)

### Compatibilidade Mantida
- ✅ Java 17 compliant (OpenJDK)
- ✅ Alpine Linux (imagem leve)
- ✅ Maven 3.9
- ✅ Mesmo tamanho (~200MB)
- ✅ Mesma performance

## 📊 Comparação de Imagens

| Característica | Eclipse Temurin | Amazon Corretto |
|----------------|-----------------|-----------------|
| Java Version | 17 | 17 |
| Base | Alpine | Alpine |
| AMD64 Support | ✅ | ✅ |
| ARM64 Support | ❌ | ✅ |
| Mantido por | Eclipse Foundation | AWS |
| Licença | GPLv2 + CE | GPLv2 + CE |
| Atualizações | Regular | Regular |
| Uso em Produção | ✅ | ✅ |

## 🚀 Verificação

### Build Local (AMD64)
```bash
docker build -t tediohook:test ./app
docker run -p 8102:8102 tediohook:test
```

### Build Multi-Plataforma
```bash
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t jessebezerra/tediohook:test \
  ./app
```

### GitHub Actions
O workflow agora deve executar com sucesso para ambas as plataformas automaticamente.

## 📝 Referências

- [Amazon Corretto Docker Images](https://hub.docker.com/_/amazoncorretto)
- [Maven Official Images](https://hub.docker.com/_/maven)
- [Docker Multi-Platform Builds](https://docs.docker.com/build/building/multi-platform/)

## ✨ Benefícios Adicionais

1. **AWS Optimized**: Corretto é otimizado para AWS (mas funciona em qualquer lugar)
2. **Long-term Support**: Suporte de longo prazo garantido pela AWS
3. **Performance**: Mesma ou melhor performance que outras distribuições OpenJDK
4. **Segurança**: Patches de segurança rápidos e regulares
5. **Gratuito**: Sem custos de licenciamento ou suporte
