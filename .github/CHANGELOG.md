# Changelog

## [Unreleased]

### Fixed
- Corrigido erro de build multi-plataforma no GitHub Actions
  - **Stage 1 (Build)**: Substituída `maven:3.9-eclipse-temurin-17-alpine` por `maven:3.9-amazoncorretto-17-alpine`
  - **Stage 2 (Runtime)**: Substituída `eclipse-temurin:17-jre-alpine` por `amazoncorretto:17-alpine`
  - Amazon Corretto suporta nativamente linux/amd64 e linux/arm64
  - Resolve erro: "no match for platform in manifest: not found"

### Changed
- Imagem base Docker alterada para Amazon Corretto 17 Alpine
- Mantém compatibilidade total com Java 17
- Mantém tamanho reduzido (~200MB)
- Adiciona suporte nativo para ARM64 (Apple Silicon, Raspberry Pi, etc)

## Notas Técnicas

### Por que Amazon Corretto?
- **Multi-arquitetura**: Suporte nativo para amd64 e arm64
- **Mantido pela AWS**: Atualizações regulares de segurança
- **Compatível**: 100% compatível com OpenJDK 17
- **Alpine**: Mantém imagem leve e segura
- **Gratuito**: Sem custos de licenciamento

### Alternativas Consideradas
- ❌ `eclipse-temurin:17-jre-alpine` - Não suporta arm64
- ✅ `amazoncorretto:17-alpine` - Suporta amd64 e arm64
- ⚠️ `openjdk:17-alpine` - Descontinuado
