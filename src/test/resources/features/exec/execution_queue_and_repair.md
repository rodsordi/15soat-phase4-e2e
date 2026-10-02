## Diagrama de Sequência

**Gerenciamento da Fila de Oficina e Execução de Reparos (Referência: `execution_queue_and_repair.feature`)**

Este diagrama documenta a entrada do veículo na fila de oficina, avanço de status durante os reparos e finalização técnica com persistência documental no MongoDB e emissão de eventos no Kafka.

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 950px;">

```mermaid
sequenceDiagram
    autonumber
    actor Mecanico as Mecânico /<br/>Técnico
    participant Kong as Kong<br/>API Gateway
    participant Exec as API Exec<br/>(Spring Boot 4 / Mongo)
    participant Mongo as MongoDB<br/>(exec_db)
    participant Kafka as Apache Kafka<br/>Broker

    %% Entrada na Fila
    Note over Mecanico,Mongo: 1. Entrada na Fila de Execução (Status: QUEUED)
    Mecanico->>Kong: POST /api/v1/executions<br/>{ workOrderId, technicianId: "TECH-101" }
    Kong->>Exec: Proxy HTTP Request
    Exec->>Mongo: Salva documento com status QUEUED
    Exec->>Kafka: Publica ExecutionStartedEvent (Tópico: execution-events)
    Exec-->>Kong: Retorna 201 Created (ExecutionResponse)
    Kong-->>Mecanico: Retorna 201 Created

    %% Início dos Reparos
    Note over Mecanico,Mongo: 2. Transição para Reparo em Andamento (Status: IN_REPAIR)
    Mecanico->>Kong: PATCH /api/v1/executions/{id}/status<br/>{ status: "IN_REPAIR" }
    Kong->>Exec: Proxy HTTP Request
    Exec->>Mongo: Atualiza status para IN_REPAIR
    Exec-->>Kong: Retorna 200 OK
    Kong-->>Mecanico: Retorna 200 OK

    %% Conclusão dos Reparos
    Note over Mecanico,Kafka: 3. Finalização Técnica e Notificação (Status: COMPLETED)
    Mecanico->>Kong: PATCH /api/v1/executions/{id}/status<br/>{ status: "COMPLETED" }
    Kong->>Exec: Proxy HTTP Request
    Exec->>Mongo: Atualiza status para COMPLETED e completedAt
    Exec->>Kafka: Publica ExecutionCompletedEvent (Tópico: execution-events)
    Exec-->>Kong: Retorna 200 OK
    Kong-->>Mecanico: Retorna 200 OK
```

</div>
</div>
