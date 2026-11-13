## Diagrama de Sequência

**Geração de Faturas e Checkout Mercado Pago (Referência: `invoice_generation.feature`)**

Este diagrama documenta a arquitetura em duas etapas da API de Faturamento:
1. **Emissão e Registro da Fatura** no PostgreSQL (`POST /api/v1/invoices`).
2. **Geração do Checkout Pix Mercado Pago** (`POST /api/v1/payments/mercadopago/checkout`) em conformidade com o contrato **OpenAPI 3.0** (`openapi.yaml` e `mercadopago-api.yaml`).

<div style="overflow-x: auto; width: 100%;">
<style>
  .mermaid svg {
    max-width: none !important;
    min-width: 1350px !important;
    width: 1350px !important;
  }
</style>

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Analista Financeiro /<br/>Sistema (Role: EMPLOYEE)
    participant Kong as Kong<br/>API Gateway
    participant Billing as API Billing<br/>(Spring Boot 4 / PG)
    participant DB as PostgreSQL<br/>(billing_db)
    participant MP as Mercado Pago<br/>Gateway

    Note over Cliente,Kong: 🔒 Sessão Autenticada: Requisições utilizam Bearer JWT (Role: EMPLOYEE)

    %% Etapa 1: Emissão da Fatura
    Note over Cliente,DB: 1. Emissão de Fatura (Persistência no Domínio)
    Cliente->>+Kong: POST /api/v1/invoices<br/>{ workOrderId, amount: 350.00, customerDocument }
    Kong->>+Billing: Proxy HTTP Request
    Billing->>+DB: Salva fatura com status PENDING no PostgreSQL
    DB-->>-Billing: Confirma persistência (id, createdAt, version=0)
    Billing-->>-Kong: Retorna 201 Created (InvoiceDetailedResponse com _links)
    Kong-->>-Cliente: Retorna 201 Created

    %% Etapa 2: Geração do Checkout Pix Mercado Pago
    Note over Cliente,MP: 2. Geração do Checkout Pix QR Code
    Cliente->>+Kong: POST /api/v1/payments/mercadopago/checkout<br/>{ invoiceId: "UUID" }
    Kong->>+Billing: Proxy HTTP Request
    Billing->>+MP: POST /v1/orders (Cria ordem de pagamento QR Code Pix)
    MP-->>-Billing: Retorna qrCode, qrCodeBase64 e paymentId
    Billing->>+DB: Atualiza fatura com externalPaymentId
    DB-->>-Billing: Confirma atualização
    Billing-->>-Kong: Retorna 200 OK (CheckoutResponse com QR Code PIX)
    Kong-->>-Cliente: Retorna 200 OK
```

</div>
