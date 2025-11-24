# TedioSession - Spring Boot Application

Aplicação Spring Boot 3 com Java 17 que integra RabbitMQ e PostgreSQL para processamento assíncrono de mensagens do WhatsApp via Evolution API.

## Tecnologias

- Java 17
- Spring Boot 3.1.5
- Spring Data JPA
- Spring AMQP (RabbitMQ)
- PostgreSQL
- Flyway (Migrations)
- Lombok
- Maven

## Arquitetura

A aplicação segue o padrão MVC com processamento assíncrono de eventos via RabbitMQ:

### Componentes Principais

1. **Consumer** (`MessageConsumer`): Consome mensagens da fila `tedio-message-consumer`
2. **Producer** (`MessageProducer`): Publica mensagens no exchange `tedio-exchange`
3. **Event Dispatcher**: Roteia eventos para os handlers apropriados
4. **Event Handlers**: Services anotados com `@EventHandler` que processam eventos específicos

### Fluxo de Mensagens

```
RabbitMQ Queue (tedio-message-consumer)
    ↓
MessageConsumer
    ↓
EventDispatcher
    ↓
EventHandlerService (baseado no event)
    ↓
Processamento Assíncrono
```

## Configuração

### PostgreSQL

```yaml
Host: 191.252.195.25
Port: 5432
Database: tedioinfernal
User: evolution
Password: Tor1t4ma2013
```

### RabbitMQ

```yaml
Host: 191.252.195.25
Port: 5672
Username: guest
Password: N@ruto2023
Queue: tedio-message-consumer
Exchange: tedio-exchange
Routing Key: tedio.message
```

## Banco de Dados

### Tabelas Existentes

- `owners` - Proprietários
- `users` - Usuários
- `evolutions` - Servidores Evolution API
- `evolution_instances` - Instâncias do WhatsApp

### Tabelas Criadas (Migration V1000)

- `evolution_sessions` - Sessões de conversação do WhatsApp
  - Armazena sessões ativas/inativas por instância e número (remoteJid)
  - Constraint única: `(instance_id, remote_jid)`

## Estrutura do Projeto

```
src/main/java/com/tedioinfernal/tediosession/
├── annotation/
│   └── EventHandler.java          # Anotação customizada para event handlers
├── config/
│   └── RabbitMQConfig.java        # Configuração do RabbitMQ
├── consumer/
│   └── MessageConsumer.java       # Consumer RabbitMQ
├── controller/
│   └── MessageController.java     # REST Controller
├── dto/
│   └── MessageDTO.java            # DTO para mensagens
├── producer/
│   └── MessageProducer.java       # Producer RabbitMQ
├── service/
│   ├── EventDispatcher.java       # Dispatcher de eventos
│   ├── EventHandlerService.java   # Interface para handlers
│   └── impl/
│       └── MessageReceivedEventHandler.java  # Handler para MESSAGE-RECEIVED
└── TedioSessionApplication.java   # Classe principal
```

## Como Usar

### 1. Compilar o Projeto

```bash
mvn clean install
```

### 2. Executar a Aplicação

```bash
mvn spring-boot:run
```

Ou executar o JAR:

```bash
java -jar target/tediosession-1.0.0.jar
```

### 3. Enviar Mensagem via API

```bash
curl -X POST http://localhost:8080/api/messages/send \
  -H "Content-Type: application/json" \
  -d '{
    "event": "MESSAGE-RECEIVED",
    "object": {
      "key1": "value1",
      "key2": "value2"
    }
  }'
```


## Formato da Mensagem

As mensagens devem seguir o formato:

```json
{
  "event": "NOME_DO_EVENTO",
  "object": {
    "campo1": "valor1",
    "campo2": "valor2"
  }
}
```

## Criando Novos Event Handlers

Para criar um novo handler de evento:

1. Crie uma classe que implemente `EventHandlerService`
2. Anote com `@EventHandler("NOME_DO_EVENTO")`
3. Implemente o método `handle(Map<String, Object> object)`

Exemplo:

```java
@Slf4j
@EventHandler("MEU-EVENTO")
public class MeuEventoHandler implements EventHandlerService {
    
    @Override
    @Async
    public void handle(Map<String, Object> object) {
        log.info("Processando MEU-EVENTO: {}", object);
        // Sua lógica aqui
    }
}
```

## Endpoints da API

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/messages/send` | Envia mensagem para RabbitMQ |

## Logs

A aplicação utiliza SLF4J com Logback. Os logs incluem:

- Recebimento de mensagens do RabbitMQ
- Dispatch de eventos
- Processamento de eventos
- Erros e exceções

## Observações

- A aplicação está configurada para processamento assíncrono com `@EnableAsync`
- O RabbitMQ usa Topic Exchange para roteamento de mensagens
- As mensagens são convertidas automaticamente para JSON
- Mensagens não são persistidas, apenas processadas em memória
