## Diagrama de Sequência

**Transação Compensatória e Rollback da Saga Distribuída (Referência: `compensating_saga_rollback.feature`)**

Este diagrama documenta o mecanismo transacional de compensação (Saga Rollback) disparado pelo Apache Kafka quando o pagamento é recusado, garantindo o cancelamento atômico da Ordem de Serviço.

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 950px;">

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Cliente
    participant OS as API Ordem de Serviço<br/>(PostgreSQL)
    participant Kafka as Apache Kafka<br/>Broker
    participant Billing as API Faturamento<br/>(PostgreSQL)
    participant MP as Mercado Pago<br/>Gateway

    Note over Cliente,Billing: Ordem Aprovada e Fatura Gerada (Status: PENDING)
    OS->>Kafka: Publica WorkOrderApprovedEvent
    Kafka->>Billing: Consome WorkOrderApprovedEvent
    Billing->>Billing: Cria Fatura PENDING no PG

    Note over MP,Kafka: Pagamento Recusado / Cancelado
    MP->>Billing: Webhook (Status: rejected / cancelled)
    Billing->>Billing: Atualiza Fatura (Status: FAILED)
    Billing->>Kafka: Publica PaymentFailedEvent (Tópico: payment-events)

    Note over Kafka,OS: Transação Compensatória (Saga Rollback)
    Kafka->>OS: Consome PaymentFailedEvent
    OS->>OS: Cancela Ordem de Serviço (Status: CANCELED)
    Note over OS: Rollback garantido sem<br/>inconsistência distribuída!
```

</div>
</div>
