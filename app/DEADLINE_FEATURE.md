# 📅 Feature: Gerenciamento de Prazo de Tarefas

## 🎯 Objetivo

Implementar gerenciamento inteligente de prazos para tarefas criadas no Monday.com:
- Se o GPT retornar um prazo, usar esse prazo
- Se não retornar prazo, usar **1 semana a partir de hoje** como padrão

## 🔧 Implementação

### 1. DTO Atualizado

**ImprovedTaskDTO.java**
```java
public class ImprovedTaskDTO {
    private String titulo;
    private String detalhe;
    private String prazo; // Formato DD/MM/YYYY
}
```

### 2. Prompt GPT Atualizado

**OpenAIService.java**
```java
private static final String SYSTEM_PROMPT = 
    "vamos criar uma historia para um card da mondey, " +
    "abaixo vc ira receber um decritivo e devolver a estoria " +
    "explicada da melhor forma possivel, sempre entanda que na " +
    "abortdagem vamos trabalhar em como cliente eu gostaria de " +
    "ter uma melhor experiencia fazendo...,ah e devolva apenas " +
    "a resposta, evite qualquer forma de itaracao pois vou pegar " +
    "sua resposta e ja usar no card, a resposta devera ser " +
    "devolvida em formato json com 3 campos: " +
    "titulo, detalhe e prazo DD/MM/AAAA";
```

### 3. Validação de Prazo

**OpenAIService.ensureDeadline()**
```java
private ImprovedTaskDTO ensureDeadline(ImprovedTaskDTO task) {
    if (task.getPrazo() == null || task.getPrazo().trim().isEmpty()) {
        // Prazo não informado: definir como 1 semana a partir de hoje
        LocalDate oneWeekFromNow = LocalDate.now().plusWeeks(1);
        String defaultDeadline = oneWeekFromNow.format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );
        
        log.info("No deadline provided, setting default: {} (1 week from now)", 
                 defaultDeadline);
        task.setPrazo(defaultDeadline);
    } else {
        log.info("Deadline provided by GPT: {}", task.getPrazo());
    }
    
    return task;
}
```

### 4. Conversão de Formato

**MondayService.convertToMondayFormat()**
```java
private String convertToMondayFormat(String deadline) {
    try {
        if (deadline == null || deadline.trim().isEmpty()) {
            // Fallback: 1 semana a partir de hoje
            return LocalDate.now().plusWeeks(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        // Parse DD/MM/YYYY
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.parse(deadline, inputFormatter);
        
        // Format para YYYY-MM-DD (formato Monday.com)
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
    } catch (Exception e) {
        log.warn("Failed to parse deadline '{}', using default (1 week from now)", 
                 deadline, e);
        return LocalDate.now().plusWeeks(1)
            .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
```

### 5. Criação de Item Atualizada

**MondayService.createTaskItem()**
```java
public String createTaskItem(String taskName, String deadline) {
    log.info("Creating task item in Monday.com: {} with deadline: {}", 
             taskName, deadline);
    
    // Converter prazo de DD/MM/YYYY para YYYY-MM-DD (formato Monday.com)
    String mondayDate = convertToMondayFormat(deadline);
    
    // Construir query GraphQL com data
    String query = String.format(
        "mutation { create_item(board_id: %s, group_id: \"%s\", " +
        "item_name: \"%s\", column_values: \"{\\\"date\\\":\\\"%s\\\"}\") " +
        "{ id } }",
        boardId,
        groupId,
        escapeGraphQL(taskName),
        mondayDate
    );
    
    // ... rest of implementation
}
```

## 📊 Fluxo de Dados

### Cenário 1: GPT Retorna Prazo

```
Usuário: "Preciso implementar login até dia 30/12/2024"
    ↓
GPT Response: {
    "titulo": "Implementação de Sistema de Login",
    "detalhe": "Como cliente, gostaria de...",
    "prazo": "30/12/2024"
}
    ↓
ensureDeadline(): Mantém "30/12/2024"
    ↓
convertToMondayFormat(): "30/12/2024" → "2024-12-30"
    ↓
Monday.com: Item criado com deadline 2024-12-30
```

### Cenário 2: GPT Não Retorna Prazo

```
Usuário: "Preciso implementar login"
    ↓
GPT Response: {
    "titulo": "Implementação de Sistema de Login",
    "detalhe": "Como cliente, gostaria de...",
    "prazo": null
}
    ↓
ensureDeadline(): Define prazo = hoje + 7 dias = "01/12/2024"
    ↓
convertToMondayFormat(): "01/12/2024" → "2024-12-01"
    ↓
Monday.com: Item criado com deadline 2024-12-01
```

### Cenário 3: Erro no Parse

```
GPT Response: {
    "prazo": "invalid-date"
}
    ↓
convertToMondayFormat(): Catch exception
    ↓
Fallback: hoje + 7 dias = "2024-12-01"
    ↓
Monday.com: Item criado com deadline 2024-12-01
```

## 🔍 Logs Gerados

### Com Prazo do GPT
```
INFO  - Improving task description with OpenAI GPT...
INFO  - GPT response received
INFO  - Deadline provided by GPT: 30/12/2024
INFO  - Task improved - Title: Implementação de Login, Deadline: 30/12/2024
INFO  - Creating task item in Monday.com: Implementação de Login with deadline: 30/12/2024
INFO  - Monday.com item created with ID: 123456 and deadline: 30/12/2024
```

### Sem Prazo (Fallback)
```
INFO  - Improving task description with OpenAI GPT...
INFO  - GPT response received
INFO  - No deadline provided, setting default: 01/12/2024 (1 week from now)
INFO  - Task improved - Title: Implementação de Login, Deadline: 01/12/2024
INFO  - Creating task item in Monday.com: Implementação de Login with deadline: 01/12/2024
INFO  - Monday.com item created with ID: 123456 and deadline: 01/12/2024
```

## ✅ Validações Implementadas

1. **Prazo Nulo ou Vazio**
   - Aplica prazo padrão: hoje + 7 dias

2. **Formato Inválido**
   - Tenta parse DD/MM/YYYY
   - Se falhar, aplica prazo padrão

3. **Conversão para Monday.com**
   - Converte DD/MM/YYYY → YYYY-MM-DD
   - Garante formato correto para API

4. **Fallback em Caso de Erro**
   - Sempre retorna um prazo válido
   - Nunca deixa tarefa sem deadline

## 🎯 Benefícios

- ✅ **Flexibilidade**: Aceita prazo do GPT ou usa padrão
- ✅ **Robustez**: Múltiplos níveis de fallback
- ✅ **Rastreabilidade**: Logs detalhados de cada decisão
- ✅ **Consistência**: Sempre cria tarefa com prazo válido
- ✅ **Manutenibilidade**: Código limpo e bem documentado

## 🧪 Testes Sugeridos

### Teste 1: Prazo Explícito
```
Input: "Implementar login até 25/12/2024"
Expected: Tarefa criada com deadline 2024-12-25
```

### Teste 2: Sem Prazo
```
Input: "Implementar login"
Expected: Tarefa criada com deadline = hoje + 7 dias
```

### Teste 3: Prazo Relativo
```
Input: "Implementar login para próxima semana"
Expected: GPT interpreta e retorna data específica
```

### Teste 4: Prazo Inválido
```
GPT retorna: "prazo": "amanhã"
Expected: Fallback para hoje + 7 dias
```

## 📝 Formato de Datas

| Contexto | Formato | Exemplo |
|----------|---------|---------|
| **GPT Response** | DD/MM/YYYY | 30/12/2024 |
| **Logs** | DD/MM/YYYY | 30/12/2024 |
| **Monday.com API** | YYYY-MM-DD | 2024-12-30 |
| **Cálculo Interno** | LocalDate | 2024-12-30 |

## 🔄 Próximas Melhorias

1. **Validação de Data Passada**
   - Alertar se prazo for anterior a hoje
   - Ajustar automaticamente para hoje + 1 dia

2. **Prazos Relativos**
   - "próxima semana" → hoje + 7 dias
   - "próximo mês" → hoje + 30 dias

3. **Configuração de Prazo Padrão**
   - Permitir configurar via variável de ambiente
   - `DEFAULT_DEADLINE_DAYS=7`

4. **Notificação de Prazo**
   - Incluir prazo na mensagem de confirmação
   - "Tarefa criada com prazo para 30/12/2024"
