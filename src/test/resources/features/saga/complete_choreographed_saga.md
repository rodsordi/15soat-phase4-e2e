## Diagrama de Sequência

**Coreografia Completa da Saga Distribuída - Happy Path (Referência: `complete_choreographed_saga.feature`)**

Este diagrama documenta a coordenação distribuída assíncrona orientada a eventos via Apache Kafka entre os microsserviços `api-work-order`, `api-billing` e `api-exec` no fluxo feliz completo.

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 1100px;">

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Cliente /<br/>Operador
    participant Kong as Kong<br/>API Gateway
    participant OS as API Ordem de Serviço<br/>(PostgreSQL)
    participant Kafka as Apache Kafka<br/>Broker
    participant Billing as API Faturamento<br/>(PostgreSQL)
    participant MP as Mercado Pago<br/>Gateway
    participant Exec as API Execução & Oficina<br/>(MongoDB)

    %% Abertura
    Note over Cliente,OS: 1. Abertura da Ordem de Serviço
    Cliente->>Kong: POST /api/v1/work-orders<br/>{ customerDocument, licensePlate, description }
    Kong->>OS: Proxy Request
    OS->>OS: Salva OS no PG (Status: RECEIVED)
    OS-->>Cliente: 201 Created (workOrderId)

    %% Aprovação
    Note over Cliente,Kafka: 2. Aprovação de Orçamento
    Cliente->>Kong: PATCH /api/v1/work-orders/{id}/status<br/>{ status: "APPROVED", totalAmount: 500.00 }
    Kong->>OS: Proxy Request
    OS->>OS: Atualiza OS (Status: APPROVED)
    OS->>Kafka: Publica WorkOrderApprovedEvent (Tópico: work-order-events)
    OS-->>Cliente: 200 OK

    %% Faturamento e Pagamento
    Note over Kafka,MP: 3. Geração de Fatura e Liquidação Mercado Pago
    Kafka->>Billing: Consome WorkOrderApprovedEvent
    Billing->>MP: Gera checkout / QR Code Pix
    MP-->>Billing: Retorna checkout URL
    Billing->>Billing: Salva Fatura no PG (Status: PENDING)
    MP->>Kong: POST /api/v1/payments/webhook<br/>{ action: "payment.updated", status: "approved" }
    Kong->>Billing: Proxy Request
    Billing->>Billing: Atualiza Fatura (Status: PAID)
    Billing->>Kafka: Publica PaymentConfirmedEvent (Tópico: payment-events)
    Billing-->>MP: 200 OK

    %% Execução na Oficina
    Note over Kafka,OS: 4. Execução dos Reparos e Finalização
    Kafka->>Exec: Consome PaymentConfirmedEvent
    Exec->>Exec: Enfileira OS no MongoDB (Status: QUEUED)
    Exec->>Kafka: Publica ExecutionStartedEvent
    Exec->>Exec: Mecânico executa reparos (Status: COMPLETED)
    Exec->>Kafka: Publica ExecutionCompletedEvent (Tópico: execution-events)

    %% Conclusão Final
    Kafka->>OS: Consome ExecutionCompletedEvent
    OS->>OS: Atualiza OS para COMPLETED no PostgreSQL
    Cliente->>Kong: GET /api/v1/work-orders/{id}
    Kong->>OS: Consulta OS
    OS-->>Cliente: 200 OK (Status: COMPLETED)
```

</div>
</div>
