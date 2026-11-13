## Diagrama de Sequência

**Processamento de Webhooks de Pagamento e Liquidação (Referência: `payment_webhook_processing.feature`)**

Este diagrama documenta o recebimento e reconciliação dos webhooks do Mercado Pago, em total conformidade com a especificação **OpenAPI 3.0** (`openapi.yaml` e `mercadopago-api.yaml`) e a **ADR 0001 (Processamento Idempotente de Webhooks)**. Cobre o fluxo de aprovação e o fluxo de recusa com verificação ativa (*pull verification*) e publicação de eventos transacionais na Saga coreografada via Apache Kafka.

<div style="overflow-x: auto; width: 100%;">
<style>
  .mermaid svg {
    max-width: none !important;
    min-width: 1100px !important;
    width: 1100px !important;
  }
</style>

```mermaid
sequenceDiagram
    autonumber
    participant MP as Mercado Pago<br/>Gateway
    participant Kong as Kong<br/>API Gateway
    participant Billing as API Billing<br/>(Spring Boot 4 / PG)
    participant Kafka as Apache Kafka<br/>Broker

    %% Pagamento Aprovado
    Note over MP,Kafka: 1. Processamento de Webhook Aprovado (Conforme OpenAPI & ADR 0001)
    MP->>Kong: POST /api/v1/payments/mercadopago/webhook<br/>{ action: "payment.updated", data: { id: "ORD-9988" } }
    Kong->>Billing: Proxy Webhook Request
    Billing->>MP: GET /v1/orders/{order_id} (Reconciliação e Verificação Ativa)
    MP-->>Billing: Retorna dados da ordem (status: "approved")
    Billing->>Billing: Atualiza fatura para PAID (Controle Otimista @Version)
    Billing->>Kafka: Publica PaymentConfirmedEvent (Tópico: payment-events)
    Billing-->>Kong: Retorna 200 OK { status: "PROCESSED" }
    Kong-->>MP: Retorna 200 OK

    %% Pagamento Recusado
    Note over MP,Kafka: 2. Processamento de Webhook Recusado (Conforme OpenAPI & ADR 0001)
    MP->>Kong: POST /api/v1/payments/mercadopago/webhook<br/>{ action: "payment.updated", data: { id: "ORD-9988" } }
    Kong->>Billing: Proxy Webhook Request
    Billing->>MP: GET /v1/orders/{order_id} (Reconciliação e Verificação Ativa)
    MP-->>Billing: Retorna dados da ordem (status: "rejected")
    Billing->>Billing: Atualiza fatura para FAILED (Controle Otimista @Version)
    Billing->>Kafka: Publica PaymentFailedEvent (Tópico: payment-events)
    Billing-->>Kong: Retorna 200 OK { status: "PROCESSED" }
    Kong-->>MP: Retorna 200 OK
```

</div>
