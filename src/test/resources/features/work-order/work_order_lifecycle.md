## Diagrama de Sequência

**Ciclo de Vida Completo da Ordem de Serviço (Referência: `work_order_lifecycle.feature`)**

Este diagrama documenta o fluxo operacional completo das 7 fases da Ordem de Serviço através do API Gateway Kong até o microsserviço de Ordens de Serviço, cobrindo recepção, diagnóstico, aprovação orçamentária, finalização, liberação e fechamento de métricas.

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 950px;">

```mermaid
sequenceDiagram
    autonumber
    actor Ator as Atendente<br/>(Recepção: EMPLOYEE)
    actor Mec as Mecânico<br/>(Oficina: EMPLOYEE)
    participant Kong as Kong<br/>API Gateway
    participant WO as API Work Order<br/>(Spring Boot 4 / PG)
    participant Kafka as Apache Kafka<br/>Broker

    Note over Ator,Kong: 🔒 Sessão Autenticada: Requisições utilizam Bearer JWT (Role: EMPLOYEE)

    %% 1. Recepção
    Note over Ator,WO: 1. Recepção do Veículo (Status: RECEIVED)
    Ator->>+Kong: POST /api/v1/work-orders<br/>{ customerDocument, licensePlate, description }
    Kong->>+WO: Proxy HTTP Request
    WO->>WO: Salva OS com status RECEIVED no PostgreSQL
    WO-->>-Kong: Retorna 201 Created (WorkOrderResponse)
    Kong-->>-Ator: Retorna 201 Created

    %% 2. Diagnóstico
    Note over Mec,WO: 2. Diagnóstico Técnico (Status: DIAGNOSING)
    Mec->>+Kong: PATCH /api/v1/work-orders/{id}/status<br/>{ status: "DIAGNOSING" }
    Kong->>+WO: Proxy HTTP Request
    WO->>WO: Atualiza status para DIAGNOSING
    WO-->>-Kong: Retorna 200 OK
    Kong-->>-Mec: Retorna 200 OK

    %% 3. Orçamento
    Note over Mec,WO: 3. Conclusão do Orçamento (Status: WAITING_APPROVAL)
    Mec->>+Kong: PATCH /api/v1/work-orders/{id}/status<br/>{ status: "WAITING_APPROVAL", totalAmount: 450.00 }
    Kong->>+WO: Proxy HTTP Request
    WO->>WO: Atualiza status para WAITING_APPROVAL
    WO-->>-Kong: Retorna 200 OK
    Kong-->>-Mec: Retorna 200 OK

    %% 4. Aprovação
    Note over Ator,Kafka: 4. Aprovação do Orçamento pelo Cliente/Atendente (Status: APPROVED)
    Ator->>+Kong: PATCH /api/v1/work-orders/{id}/status<br/>{ status: "APPROVED", totalAmount: 450.00 }
    Kong->>+WO: Proxy HTTP Request
    WO->>WO: Atualiza status para APPROVED
    WO->>Kafka: Publica WorkOrderApprovedEvent (Tópico: work-order-events)
    WO-->>-Kong: Retorna 200 OK
    Kong-->>-Ator: Retorna 200 OK

    %% 5. Finalização
    Note over Mec,WO: 5. Conclusão dos Serviços Técnicos (Status: COMPLETED)
    Mec->>+Kong: PATCH /api/v1/work-orders/{id}/status<br/>{ status: "COMPLETED" }
    Kong->>+WO: Proxy HTTP Request
    WO->>WO: Atualiza status para COMPLETED
    WO-->>-Kong: Retorna 200 OK
    Kong-->>-Mec: Retorna 200 OK

    %% 6. Liberação
    Note over Ator,WO: 6. Retirada e Liberação do Veículo (Status: DELIVERED)
    Ator->>+Kong: PATCH /api/v1/work-orders/{id}/status<br/>{ status: "DELIVERED" }
    Kong->>+WO: Proxy HTTP Request
    WO->>WO: Atualiza status para DELIVERED
    WO-->>-Kong: Retorna 200 OK
    Kong-->>-Ator: Retorna 200 OK

    %% 7. Métricas
    Note over Ator,WO: 7. Apuração do Tempo Médio de Execução
    Ator->>+Kong: GET /api/v1/work-orders/metrics/average-time
    Kong->>+WO: Proxy HTTP Request
    WO->>WO: Consolida métricas dos serviços finalizados
    WO-->>-Kong: Retorna 200 OK com métricas apuradas
    Kong-->>-Ator: Retorna 200 OK
```

</div>
</div>
