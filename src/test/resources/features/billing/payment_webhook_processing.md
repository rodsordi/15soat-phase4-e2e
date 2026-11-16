## Diagrama de Sequência

**Processamento de Webhooks de Pagamento e Liquidação (Referência: `payment_webhook_processing.feature`)**

Este diagrama documenta o recebimento e reconciliação dos webhooks do Mercado Pago, em total conformidade com a especificação **OpenAPI 3.0** (`openapi.yaml` e `mercadopago-api.yaml`) e a **ADR 0001 (Processamento Idempotente de Webhooks)**. Cobre o fluxo de aprovação e o fluxo de recusa com verificação ativa (*pull verification*) e publicação de eventos transacionais na Saga coreografada via Apache Kafka.

> [!NOTE]
> **🔒 Ingress de Pagamentos**: As notificações assíncronas do Mercado Pago são recebidas através da rota pública de webhook, sendo processadas com verificação ativa de status e idempotência estrita.

<div style="overflow-x: auto; width: 100%;">
<style>
  .mermaid svg {
    max-width: none !important;
    min-width: 900px !important;
    width: 900px !important;
  }
</style>

```mermaid
sequenceDiagram
    autonumber
    participant MP as Mercado Pago<br/>Gateway
    participant Billing as API Billing<br/>(Spring Boot 4 / PG)
    participant Kafka as Apache Kafka<br/>Broker

    Note over MP,Billing: 🔒 Ingress de Notificação de Pagamento:<br/>Webhook do Mercado Pago recebido e validado com idempotência

    %% Pagamento Aprovado
    Note over MP,Kafka: 1. Processamento de Webhook Aprovado (Conforme OpenAPI & ADR 0001)
    MP->>+Billing: POST /api/v1/payments/mercadopago/webhook<br/>{ action: "payment.updated",<br/>  data: { id: "ORD-9988" } }
    Billing->>+MP: GET /v1/orders/{order_id}<br/>(Reconciliação e Verificação Ativa)
    MP-->>-Billing: Retorna dados da ordem (status: "approved")
    Billing->>Billing: Atualiza fatura para PAID<br/>(Controle Otimista @Version)
    Billing->>Kafka: Publica PaymentConfirmedEvent<br/>(Tópico: payment-events)
    Billing-->>-MP: Retorna 200 OK { status: "PROCESSED" }

    %% Pagamento Recusado
    Note over MP,Kafka: 2. Processamento de Webhook Recusado (Conforme OpenAPI & ADR 0001)
    MP->>+Billing: POST /api/v1/payments/mercadopago/webhook<br/>{ action: "payment.updated",<br/>  data: { id: "ORD-9988" } }
    Billing->>+MP: GET /v1/orders/{order_id}<br/>(Reconciliação e Verificação Ativa)
    MP-->>-Billing: Retorna dados da ordem (status: "rejected")
    Billing->>Billing: Atualiza fatura para FAILED<br/>(Controle Otimista @Version)
    Billing->>Kafka: Publica PaymentFailedEvent<br/>(Tópico: payment-events)
    Billing-->>-MP: Retorna 200 OK { status: "PROCESSED" }

    %% Retransmissão / Idempotência
    Note over MP,Kafka: 3. Retransmissão de Webhook Repetido e Idempotência Estrita (Cenário 3 / ADR 0001)
    MP->>+Billing: POST /api/v1/payments/mercadopago/webhook<br/>{ action: "payment.updated",<br/>  data: { id: "ORD-9988" } }
    Billing->>Billing: Localiza fatura já liquidada (Status: PAID)<br/>Identifica processamento prévio
    Note over Billing: Descarte idempotente:<br/>Fatura mantida em PAID<br/>Sem nova publicação no Kafka
    Billing-->>-MP: Retorna 200 OK { status: "PROCESSED" }
```

</div>
