## Diagrama de Sequência

**Gerenciamento da Fila de Oficina e Execução de Reparos (Referência: `execution_queue_and_repair.feature`)**

Este diagrama documenta a entrada do veículo na fila de oficina, avanço de status durante os reparos e finalização técnica com persistência documental no MongoDB e emissão de eventos no Kafka:
1. **Entrada na Fila Operacional (`POST /api/v1/executions`)**: Registro do veículo com status `QUEUED` e emissão do evento `ExecutionStartedEvent`.
2. **Consulta e Triagem das Baias (`GET /api/v1/executions/queue`)**: Recuperação de ordens ativas nos estados `QUEUED` e `IN_REPAIR`.
3. **Início e Evolução do Reparo (`PATCH /api/v1/executions/{id}`)**: Transição de estado com persistência documental no MongoDB.
4. **Finalização Técnica ou Bloqueio**: Conclusão (`COMPLETED`) com publicação de `ExecutionCompletedEvent` ou interrupção por falha técnica (`FAILED`) com publicação de `ExecutionFailedEvent`.
5. **Bloqueio Defensivo de Mutações**: Rejeição via RFC 7807 (`EXECUTION_ALREADY_FINALIZED`) em caso de tentativa de alteração em execução já encerrada.

> [!NOTE]
> **🔒 Autenticação & Emissão de JWT**: A obtenção e emissão de tokens Bearer JWT via Keycloak estão detalhadas no diagrama [user_authentication.md](../auth-lambda/user_authentication.md).

---

<div style="overflow-x: auto; zoom: 1.5; min-width: 900px;">

```mermaid
sequenceDiagram
    autonumber
    box rgba(100, 116, 139, 0.08) Atores de Negócio
        actor Mecanico as Mecânico / Técnico<br/>(Funcionário: EMPLOYEE)
    end
    box rgba(59, 130, 246, 0.08) Cluster Amazon EKS (Microsserviços)
        participant Exec as API Exec<br/>(Spring Boot 4 / Mongo)
    end
    box rgba(168, 85, 247, 0.08) Camada de Persistência
        participant Mongo as MongoDB<br/>(exec_db)
    end
    box rgba(34, 197, 94, 0.08) Mensageria Gerenciada
        participant Kafka as Apache Kafka<br/>Broker
    end

    Note over Mecanico,Exec: 🔒 Borda Autenticada via Bearer JWT (vide user_authentication.md):<br/>Requisições utilizam token OIDC emitido pelo Keycloak (Role: EMPLOYEE)

    %% Entrada na Fila
    Note over Mecanico,Mongo: 1. Entrada na Fila de Execução (Status: QUEUED)
    Mecanico->>+Exec: POST /api/v1/executions<br/>{ workOrderId, technicianId: "TECH-101" }
    Exec->>+Mongo: Salva documento com status QUEUED
    Mongo-->>-Exec: Confirma persistência
    Exec--)Kafka: Publica ExecutionStartedEvent (Tópico: execution-events)
    Exec-->>-Mecanico: Retorna 201 Created (ExecutionResponse)

    %% Consulta da Fila Operacional
    Note over Mecanico,Mongo: 2. Consulta da Fila Ativa da Oficina (Status: QUEUED / IN_REPAIR)
    Mecanico->>+Exec: GET /api/v1/executions/queue
    Exec->>+Mongo: Busca ordens de execução ativas (QUEUED, IN_REPAIR)
    Mongo-->>-Exec: Retorna lista de ordens
    Exec-->>-Mecanico: Retorna 200 OK (PagedExecutionResponse)

    %% Início dos Reparos
    Note over Mecanico,Mongo: 3. Transição para Reparo em Andamento (Status: IN_REPAIR)
    Mecanico->>+Exec: PATCH /api/v1/executions/{id}<br/>{ status: "IN_REPAIR" }
    Exec->>+Mongo: Atualiza status para IN_REPAIR
    Mongo-->>-Exec: Confirma atualização
    Exec-->>-Mecanico: Retorna 200 OK

    %% Conclusão ou Falha dos Reparos
    alt Conclusão Técnica do Reparo (Happy Path)
        Note over Mecanico,Kafka: 4a. Finalização Técnica e Notificação (Status: COMPLETED)
        Mecanico->>+Exec: PATCH /api/v1/executions/{id}<br/>{ status: "COMPLETED" }
        Exec->>+Mongo: Atualiza status para COMPLETED e completedAt
        Mongo-->>-Exec: Confirma atualização
        Exec--)Kafka: Publica ExecutionCompletedEvent (Tópico: execution-events)
        Exec-->>-Mecanico: Retorna 200 OK
    else Interrupção por Impedimento Técnico (Falha)
        Note over Mecanico,Kafka: 4b. Registro de Falha e Justificativa (Status: FAILED)
        Mecanico->>+Exec: PATCH /api/v1/executions/{id}<br/>{ status: "FAILED", notes: "Bloco trincado" }
        Exec->>+Mongo: Atualiza status para FAILED e completedAt
        Mongo-->>-Exec: Confirma atualização
        Exec--)Kafka: Publica ExecutionFailedEvent (Tópico: execution-events)
        Exec-->>-Mecanico: Retorna 200 OK
    end

    %% Regra de Negócio: Bloqueio Pós-Finalização
    Note over Mecanico,Exec: 5. Bloqueio de Alteração em Ordem Finalizada (Regra Defensiva)
    Mecanico->>+Exec: PATCH /api/v1/executions/{id}<br/>{ status: "IN_REPAIR" }
    Exec->>Exec: Valida status atual (COMPLETED/FAILED) -> Dispara DomainException
    Exec-->>-Mecanico: Retorna 400 Bad Request (ProblemDetail: EXECUTION_ALREADY_FINALIZED)
```

</div>
