## Diagrama de Sequência

**Transação Compensatória e Rollback da Saga Distribuída (Referência: `compensating_saga_rollback.feature`)**

Este diagrama documenta o mecanismo transacional de compensação (Saga Rollback) disparado pelo Apache Kafka quando o pagamento é recusado, garantindo o cancelamento atômico da Ordem de Serviço.

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 950px;">

```mermaid
sequenceDiagram
    autonumber
    actor Atendente as Atendente<br/>(Recepção: EMPLOYEE)
    participant Kong as Kong<br/>API Gateway
    participant OS as API Ordem de Serviço<br/>(PostgreSQL)
    participant Kafka as Apache Kafka<br/>Broker
    participant Billing as API Faturamento<br/>(PostgreSQL)
    participant MP as Mercado Pago<br/>Gateway

    Note over Atendente,Kong: 🔒 Sessão Autenticada: Requisições utilizam Bearer JWT (Role: EMPLOYEE)

    Note over Atendente,Billing: 1. Ordem Aprovada e Fatura Gerada (Status: PENDING)
    Atendente->>+Kong: PATCH /api/v1/work-orders/{id}/status<br/>{ status: "APPROVED", totalAmount: 450.00 }
    Kong->>+OS: Proxy Request
    OS->>OS: Atualiza status para APPROVED
    OS->>Kafka: Publica WorkOrderApprovedEvent (Tópico: work-order-events)
    OS-->>-Kong: 200 OK
    Kong-->>-Atendente: 200 OK
    Kafka->>+Billing: Consome WorkOrderApprovedEvent
    Billing->>Billing: Cria Fatura PENDING no PG
    Billing-->>-Kafka: Fatura registrada

    Note over MP,Kafka: 2. Pagamento Recusado / Cancelado pelo Gateway
    MP->>+Kong: POST /api/v1/payments/webhook<br/>{ action: "payment.updated", status: "rejected" }
    Kong->>+Billing: Proxy Request
    Billing->>Billing: Atualiza Fatura (Status: FAILED)
    Billing->>Kafka: Publica PaymentFailedEvent (Tópico: payment-events)
    Billing-->>-Kong: 200 OK
    Kong-->>-MP: 200 OK

    Note over Kafka,OS: 3. Transação Compensatória (Saga Rollback)
    Kafka->>+OS: Consome PaymentFailedEvent
    OS->>OS: Cancela Ordem de Serviço (Status: CANCELED)
    OS-->>-Kafka: Rollback concluído
    Note over OS: Rollback garantido sem<br/>inconsistência distribuída!
```

</div>
</div>
