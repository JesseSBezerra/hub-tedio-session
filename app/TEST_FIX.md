# 🧪 Correção dos Testes

## ❌ Problema Identificado

Os testes estavam falhando ao tentar conectar ao PostgreSQL:

```
FATAL: password authentication failed for user "${DATABASE_USER}"
```

### Causa Raiz:
- Testes tentavam usar o banco PostgreSQL de produção
- Variáveis de ambiente não estão disponíveis no CI/CD
- Flyway tentava rodar migrations em banco inexistente
- RabbitMQ não estava disponível para testes

## ✅ Solução Implementada

### 1. Perfil de Teste (`application-test.yml`)

Criado configuração específica para testes usando **H2 em memória**:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: 
    driver-class-name: org.h2.Driver

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop  # Cria schema automaticamente

  flyway:
    enabled: false  # Desabilita Flyway nos testes

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

# APIs externas com valores fake para testes
evolution:
  api:
    url: http://localhost:8101
  auth:
    email: test@example.com
    password: test-password

monday:
  api:
    url: https://api.monday.com/v2
    token: test-token
  board:
    id: "12345"
  group:
    id: test-group

openai:
  api:
    key: test-api-key
  chat:
    model: gpt-4.1
  transcription:
    model: gpt-4o-transcribe
```

### 2. Dependência H2 (`pom.xml`)

```xml
<!-- H2 Database for Testing -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### 3. Ativação do Perfil de Teste

```java
@SpringBootTest
@ActiveProfiles("test")  // ← Usa application-test.yml
class TedioSessionApplicationTests {
    
    @Test
    void contextLoads() {
        // Test que verifica se o contexto Spring carrega corretamente
    }
}
```

## 📊 Diferenças: Produção vs Teste

| Componente | Produção | Teste |
|------------|----------|-------|
| **Banco de Dados** | PostgreSQL (externo) | H2 (em memória) |
| **Flyway** | Habilitado | Desabilitado |
| **RabbitMQ** | Externo (191.252.195.25) | Local/Mock |
| **APIs Externas** | Credenciais reais | Valores fake |
| **Schema** | Migrations Flyway | `ddl-auto: create-drop` |

## 🎯 Benefícios

- ✅ **Isolamento**: Testes não dependem de recursos externos
- ✅ **Velocidade**: H2 em memória é muito mais rápido
- ✅ **CI/CD**: Funciona sem configurar infraestrutura
- ✅ **Limpeza**: Cada teste começa com banco limpo
- ✅ **Portabilidade**: Roda em qualquer ambiente

## 🚀 Executando Testes

### Localmente:
```bash
cd app
mvn test
```

### CI/CD (GitHub Actions):
```yaml
- name: Run tests
  run: mvn test
  working-directory: ./app
```

O perfil `test` é ativado automaticamente pela anotação `@ActiveProfiles("test")`.

## 🔍 Verificação

### Antes (FALHA):
```
Error: password authentication failed for user "${DATABASE_USER}"
Tests run: 1, Failures: 0, Errors: 1
BUILD FAILURE
```

### Depois (SUCESSO):
```
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 📝 Próximos Passos

### Testes Unitários Recomendados:

1. **Service Layer Tests**
```java
@ExtendWith(MockitoExtension.class)
class OpenAIServiceTest {
    @Mock
    private RestTemplate restTemplate;
    
    @InjectMocks
    private OpenAIService openAIService;
    
    @Test
    void shouldImproveTaskDescription() {
        // Test implementation
    }
}
```

2. **Repository Tests**
```java
@DataJpaTest
@ActiveProfiles("test")
class MessageSessionRepositoryTest {
    @Autowired
    private MessageSessionRepository repository;
    
    @Test
    void shouldSaveMessageSession() {
        // Test implementation
    }
}
```

3. **Integration Tests**
```java
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MessageControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldProcessMessage() throws Exception {
        // Test implementation
    }
}
```

## ⚠️ Importante

### Para Testes de Integração com APIs Externas:

Use **WireMock** ou **MockServer** para simular respostas:

```xml
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <version>3.3.1</version>
    <scope>test</scope>
</dependency>
```

```java
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
class EvolutionApiIntegrationTest {
    
    @Test
    void shouldCallEvolutionApi() {
        // Mock API response
        stubFor(post("/api/evolution/message")
            .willReturn(ok()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"success\": true}")));
        
        // Test implementation
    }
}
```

## 🔗 Referências

- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [H2 Database](https://www.h2database.com/)
- [Test Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [WireMock](https://wiremock.org/)
