## Diagrama de Sequência

**Geração de Faturas e Checkout Mercado Pago (Referência: `invoice_generation.feature`)**

Este diagrama documenta o fluxo de emissão de faturas e integração com o Mercado Pago para disponibilização de checkout e QR Code Pix, com persistência no PostgreSQL.

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 900px;">

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Analista Financeiro /<br/>Sistema (Role: EMPLOYEE)
    participant Kong as Kong<br/>API Gateway
    participant Billing as API Billing<br/>(Spring Boot 4 / PG)
    participant MP as Mercado Pago<br/>Gateway
    participant DB as PostgreSQL<br/>(billing_db)

    Note over Cliente,Kong: 🔒 Sessão Autenticada: Requisições utilizam Bearer JWT (Role: EMPLOYEE)

    %% Geração da Fatura
    Note over Cliente,DB: 1. Emissão de Fatura e Geração de Checkout
    Cliente->>+Kong: POST /api/v1/invoices<br/>{ workOrderId, amount: 350.00, customerDocument }
    Kong->>+Billing: Proxy HTTP Request
    Billing->>+MP: Cria Preferência de Pagamento / QR Code Pix (POST /checkout/preferences)
    MP-->>-Billing: Retorna checkoutUrl e ID da transação
    Billing->>+DB: Salva fatura com status PENDING no PostgreSQL
    DB-->>-Billing: Confirma persistência
    Billing-->>-Kong: Retorna 201 Created (InvoiceResponse com link)
    Kong-->>-Cliente: Retorna 201 Created
```

</div>
</div>
