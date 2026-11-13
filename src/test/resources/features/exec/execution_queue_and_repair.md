## Diagrama de Sequência

**Gerenciamento da Fila de Oficina e Execução de Reparos (Referência: `execution_queue_and_repair.feature`)**

Este diagrama documenta a entrada do veículo na fila de oficina, avanço de status durante os reparos e finalização técnica com persistência documental no MongoDB e emissão de eventos no Kafka.

<div style="overflow-x: auto; width: 100%;">
<style>
  .mermaid svg {
    max-width: none !important;
    min-width: 1300px !important;
    width: 1300px !important;
  }
</style>

```mermaid
sequenceDiagram
    autonumber
    actor Mecanico as Mecânico / Técnico<br/>(Funcionário: EMPLOYEE)
    participant Kong as Kong<br/>API Gateway
    participant Exec as API Exec<br/>(Spring Boot 4 / Mongo)
    participant Mongo as MongoDB<br/>(exec_db)
    participant Kafka as Apache Kafka<br/>Broker

    Note over Mecanico,Kong: 🔒 Sessão Autenticada: Requisições utilizam Bearer JWT (Role: EMPLOYEE)

    %% Entrada na Fila
    Note over Mecanico,Mongo: 1. Entrada na Fila de Execução (Status: QUEUED)
    Mecanico->>+Kong: POST /api/v1/executions<br/>{ workOrderId, technicianId: "TECH-101" }
    Kong->>+Exec: Proxy HTTP Request
    Exec->>Mongo: Salva documento com status QUEUED
    Exec->>Kafka: Publica ExecutionStartedEvent (Tópico: execution-events)
    Exec-->>-Kong: Retorna 201 Created (ExecutionResponse)
    Kong-->>-Mecanico: Retorna 201 Created

    %% Consulta da Fila Operacional
    Note over Mecanico,Mongo: 2. Consulta da Fila Ativa da Oficina (Status: QUEUED / IN_REPAIR)
    Mecanico->>+Kong: GET /api/v1/executions/queue
    Kong->>+Exec: Proxy HTTP Request
    Exec->>Mongo: Busca ordens de execução ativas (QUEUED, IN_REPAIR)
    Mongo-->>Exec: Retorna lista de ordens
    Exec-->>-Kong: Retorna 200 OK (PagedExecutionResponse)
    Kong-->>-Mecanico: Retorna 200 OK

    %% Início dos Reparos
    Note over Mecanico,Mongo: 3. Transição para Reparo em Andamento (Status: IN_REPAIR)
    Mecanico->>+Kong: PATCH /api/v1/executions/{id}<br/>{ status: "IN_REPAIR" }
    Kong->>+Exec: Proxy HTTP Request
    Exec->>Mongo: Atualiza status para IN_REPAIR
    Exec-->>-Kong: Retorna 200 OK
    Kong-->>-Mecanico: Retorna 200 OK

    %% Conclusão ou Falha dos Reparos
    alt Conclusão Técnica do Reparo (Happy Path)
        Note over Mecanico,Kafka: 4a. Finalização Técnica e Notificação (Status: COMPLETED)
        Mecanico->>+Kong: PATCH /api/v1/executions/{id}<br/>{ status: "COMPLETED" }
        Kong->>+Exec: Proxy HTTP Request
        Exec->>Mongo: Atualiza status para COMPLETED e completedAt
        Exec->>Kafka: Publica ExecutionCompletedEvent (Tópico: execution-events)
        Exec-->>-Kong: Retorna 200 OK
        Kong-->>-Mecanico: Retorna 200 OK
    else Interrupção por Impedimento Técnico (Falha)
        Note over Mecanico,Kafka: 4b. Registro de Falha e Justificativa (Status: FAILED)
        Mecanico->>+Kong: PATCH /api/v1/executions/{id}<br/>{ status: "FAILED", notes: "Bloco trincado" }
        Kong->>+Exec: Proxy HTTP Request
        Exec->>Mongo: Atualiza status para FAILED e completedAt
        Exec->>Kafka: Publica ExecutionFailedEvent (Tópico: execution-events)
        Exec-->>-Kong: Retorna 200 OK
        Kong-->>-Mecanico: Retorna 200 OK
    end

    %% Regra de Negócio: Bloqueio Pós-Finalização
    Note over Mecanico,Exec: 5. Bloqueio de Alteração em Ordem Finalizada (Regra Defensiva)
    Mecanico->>+Kong: PATCH /api/v1/executions/{id}<br/>{ status: "IN_REPAIR" }
    Kong->>+Exec: Proxy HTTP Request
    Exec->>Exec: Valida status atual (COMPLETED/FAILED) -> Dispara DomainException
    Exec-->>-Kong: Retorna 400 Bad Request (RFC 7807 ProblemDetail: EXECUTION_ALREADY_FINALIZED)
    Kong-->>-Mecanico: Retorna 400 Bad Request
```

</div>

