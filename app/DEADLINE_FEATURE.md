# 📅 Feature: Gerenciamento de Prazo de Tarefas

## 🎯 Objetivo

Implementar gerenciamento inteligente de prazos para tarefas criadas no Monday.com:
- Se o GPT retornar um prazo válido, usar esse prazo
- Se não retornar prazo, usar **1 semana a partir de hoje** como padrão
- Se o prazo for no passado, usar **1 semana a partir de hoje**
- Formato unificado: **YYYY-MM-DD (ISO 8601)** - mesmo formato do Monday.com

## 🔧 Implementação

### 1. DTO Atualizado

**ImprovedTaskDTO.java**
```java
public class ImprovedTaskDTO {
    private String titulo;
    private String detalhe;
    private String prazo; // Formato YYYY-MM-DD (ISO 8601)
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
    "titulo, detalhe e prazo ANO(4 DIGITOS)-MES-DIA";
```

**Formato solicitado ao GPT:** `YYYY-MM-DD` (ISO 8601)  
**Vantagem:** Mesmo formato usado pelo Monday.com, sem necessidade de conversão!

### 3. Validação de Prazo

**OpenAIService.ensureDeadline()**
```java
private ImprovedTaskDTO ensureDeadline(ImprovedTaskDTO task) {
    if (task.getPrazo() == null || task.getPrazo().trim().isEmpty()) {
        // Prazo não informado: definir como 1 semana a partir de hoje
        LocalDate oneWeekFromNow = LocalDate.now().plusWeeks(1);
        String defaultDeadline = oneWeekFromNow.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        log.info("No deadline provided, setting default: {} (1 week from now)", 
                 defaultDeadline);
        task.setPrazo(defaultDeadline);
    } else {
        log.info("Deadline provided by GPT: {}", task.getPrazo());
    }
    
    return task;
}
```

### 4. Validação de Prazo no Monday.com

**MondayService.ensureValidDeadline()**
```java
private String ensureValidDeadline(String deadline) {
    try {
        if (deadline == null || deadline.trim().isEmpty()) {
            // Fallback: 1 semana a partir de hoje
            String defaultDeadline = LocalDate.now().plusWeeks(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
            log.info("No deadline provided, using default: {}", defaultDeadline);
            return defaultDeadline;
        }
        
        // Validar formato YYYY-MM-DD
        LocalDate date = LocalDate.parse(deadline, DateTimeFormatter.ISO_LOCAL_DATE);
        
        // Verificar se a data não é no passado
        if (date.isBefore(LocalDate.now())) {
            log.warn("Deadline '{}' is in the past, using default (1 week from now)", 
                     deadline);
            return LocalDate.now().plusWeeks(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        
        log.info("Using deadline: {}", deadline);
        return deadline;
        
    } catch (Exception e) {
        log.warn("Failed to parse deadline '{}', using default (1 week from now)", 
                 deadline, e);
        return LocalDate.now().plusWeeks(1)
            .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
```

**Nova validação:** Verifica se a data não está no passado!

### 5. Criação de Item Atualizada

**MondayService.createTaskItem()**
```java
public String createTaskItem(String taskName, String deadline) {
    log.info("Creating task item in Monday.com: {} with deadline: {}", 
             taskName, deadline);
    
    // Validar e aplicar prazo padrão se necessário
    String mondayDate = ensureValidDeadline(deadline);
    
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

### Cenário 1: GPT Retorna Prazo Válido

```
Usuário: "Preciso implementar login até dia 30/12/2024"
    ↓
GPT Response: {
    "titulo": "Implementação de Sistema de Login",
    "detalhe": "Como cliente, gostaria de...",
    "prazo": "2024-12-30"
}
    ↓
ensureDeadline(): Mantém "2024-12-30"
    ↓
ensureValidDeadline(): Valida formato e data futura → OK
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
ensureDeadline(): Define prazo = hoje + 7 dias = "2024-12-01"
    ↓
ensureValidDeadline(): Valida formato → OK
    ↓
Monday.com: Item criado com deadline 2024-12-01
```

### Cenário 3: GPT Retorna Data no Passado

```
GPT Response: {
    "prazo": "2024-01-01"
}
    ↓
ensureDeadline(): Mantém "2024-01-01"
    ↓
ensureValidDeadline(): Detecta data no passado!
    ↓
Fallback: hoje + 7 dias = "2024-12-01"
    ↓
Monday.com: Item criado com deadline 2024-12-01
```

### Cenário 4: Erro no Parse

```
GPT Response: {
    "prazo": "invalid-date"
}
    ↓
ensureValidDeadline(): Catch exception
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
INFO  - Deadline provided by GPT: 2024-12-30
INFO  - Task improved - Title: Implementação de Login, Deadline: 2024-12-30
INFO  - Creating task item in Monday.com: Implementação de Login with deadline: 2024-12-30
INFO  - Using deadline: 2024-12-30
INFO  - Monday.com item created with ID: 123456 and deadline: 2024-12-30
```

### Sem Prazo (Fallback)
```
INFO  - Improving task description with OpenAI GPT...
INFO  - GPT response received
INFO  - No deadline provided, setting default: 2024-12-01 (1 week from now)
INFO  - Task improved - Title: Implementação de Login, Deadline: 2024-12-01
INFO  - Creating task item in Monday.com: Implementação de Login with deadline: 2024-12-01
INFO  - Using deadline: 2024-12-01
INFO  - Monday.com item created with ID: 123456 and deadline: 2024-12-01
```

### Data no Passado (Fallback)
```
INFO  - Deadline provided by GPT: 2024-01-01
WARN  - Deadline '2024-01-01' is in the past, using default (1 week from now)
INFO  - Monday.com item created with ID: 123456 and deadline: 2024-12-01
```

## ✅ Validações Implementadas

1. **Prazo Nulo ou Vazio**
   - Aplica prazo padrão: hoje + 7 dias (YYYY-MM-DD)

2. **Formato Inválido**
   - Tenta parse YYYY-MM-DD (ISO 8601)
   - Se falhar, aplica prazo padrão

3. **Data no Passado** ⭐ NOVO!
   - Verifica se data < hoje
   - Se sim, aplica prazo padrão
   - Evita criar tarefas com deadline vencido

4. **Formato Unificado**
   - GPT retorna: YYYY-MM-DD
   - Monday.com usa: YYYY-MM-DD
   - **Sem necessidade de conversão!**

5. **Fallback em Caso de Erro**
   - Sempre retorna um prazo válido
   - Nunca deixa tarefa sem deadline

## 🎯 Benefícios

- ✅ **Formato Unificado**: YYYY-MM-DD em todo o fluxo (GPT → App → Monday.com)
- ✅ **Sem Conversão**: Elimina complexidade de transformação de formatos
- ✅ **Validação de Data Passada**: Evita criar tarefas com deadline vencido
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
| **GPT Response** | YYYY-MM-DD | 2024-12-30 |
| **Logs** | YYYY-MM-DD | 2024-12-30 |
| **Monday.com API** | YYYY-MM-DD | 2024-12-30 |
| **Cálculo Interno** | LocalDate | 2024-12-30 |

**Vantagem:** Formato unificado ISO 8601 em todo o fluxo! 🎉

## 🔄 Próximas Melhorias

1. ~~**Validação de Data Passada**~~ ✅ **IMPLEMENTADO!**
   - ✅ Detecta se prazo é anterior a hoje
   - ✅ Ajusta automaticamente para hoje + 7 dias

2. **Prazos Relativos**
   - "próxima semana" → hoje + 7 dias
   - "próximo mês" → hoje + 30 dias

3. **Configuração de Prazo Padrão**
   - Permitir configurar via variável de ambiente
   - `DEFAULT_DEADLINE_DAYS=7`

4. **Notificação de Prazo**
   - Incluir prazo na mensagem de confirmação
   - "Tarefa criada com prazo para 30/12/2024"
