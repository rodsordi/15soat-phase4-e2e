## Diagrama de Sequência

**Processamento de Webhooks de Pagamento e Liquidação (Referência: `payment_webhook_processing.feature`)**

Este diagrama documenta o recebimento e reconciliação dos webhooks do Mercado Pago, cobrindo o fluxo de aprovação e o fluxo de recusa com publicação de eventos transacionais no Apache Kafka.

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 950px;">

```mermaid
sequenceDiagram
    autonumber
    participant MP as Mercado Pago<br/>Gateway
    participant Kong as Kong<br/>API Gateway
    participant Billing as API Billing<br/>(Spring Boot 4 / PG)
    participant Kafka as Apache Kafka<br/>Broker

    %% Pagamento Aprovado
    Note over MP,Kafka: 1. Processamento de Webhook Aprovado
    MP->>Kong: POST /api/v1/payments/webhook<br/>{ action: "payment.updated", id, status: "approved" }
    Kong->>Billing: Proxy Webhook Request
    Billing->>MP: Consulta e reconcilia transação
    MP-->>Billing: Status "approved"
    Billing->>Billing: Atualiza fatura para PAID
    Billing->>Kafka: Publica PaymentConfirmedEvent (Tópico: payment-events)
    Billing-->>Kong: Retorna 200 OK
    Kong-->>MP: Retorna 200 OK

    %% Pagamento Recusado
    Note over MP,Kafka: 2. Processamento de Webhook Recusado
    MP->>Kong: POST /api/v1/payments/webhook<br/>{ action: "payment.updated", id, status: "rejected" }
    Kong->>Billing: Proxy Webhook Request
    Billing->>Billing: Atualiza fatura para FAILED
    Billing->>Kafka: Publica PaymentFailedEvent (Tópico: payment-events)
    Billing-->>Kong: Retorna 200 OK
    Kong-->>MP: Retorna 200 OK
```

</div>
</div>
