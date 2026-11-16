## Diagrama de Sequência

**Geração de Faturas, Validações e Checkout Mercado Pago (Referência: `invoice_generation.feature`)**

Este diagrama documenta a arquitetura completa da API de Faturamento cobrindo todos os cenários da especificação BDD em conformidade com o contrato **OpenAPI 3.0** (`openapi.yaml` e `mercadopago-api.yaml`):
1. **Emissão e Idempotência (`POST /api/v1/invoices`)**: Criação da fatura com status `PENDING` no PostgreSQL (Cenário 1) e garantia de idempotência retornando o registro existente em chamadas duplicadas para a mesma Ordem de Serviço (Cenário 2).
2. **Validações de Domínio e Segurança**: Rejeição de valores menores ou iguais a zero com `400 Bad Request` (Cenário 3), documento de cliente inválido com `422 Unprocessable Entity` (Cenário 4) e bloqueio por ausência de credenciais com `401 Unauthorized` (vide [invoice_generation-non-func.feature](invoice_generation-non-func.feature)).

3. **Recuperação e Consulta Detalhada (`GET /api/v1/invoices/{id}` e `/work-order/{woId}`)**: Recuperação da fatura com links HATEOAS canônicos (Cenário 5) e tratamento defensivo `404 Not Found` para faturas inexistentes (Cenário 6).
4. **Geração do Checkout Pix Mercado Pago (`POST /api/v1/payments/mercadopago/checkout`)**: Criação da ordem Pix com QR Code e persistência do `externalPaymentId` (Cenário 7).

> [!NOTE]
> **🔒 Autenticação & Emissão de JWT**: A obtenção e emissão de tokens Bearer JWT via Keycloak estão detalhadas no diagrama [user_authentication.md](../auth-lambda/user_authentication.md).

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
    actor Analista as Analista Financeiro<br/>(Funcionário: EMPLOYEE)
    participant Billing as API Billing<br/>(Spring Boot 4 / PG)
    participant DB as PostgreSQL<br/>(billing_db)
    participant MP as Mercado Pago<br/>Gateway

    Note over Analista,Billing: 🔒 Borda Autenticada via Bearer JWT (vide user_authentication.md):<br/>Requisições utilizam token OIDC emitido pelo Keycloak (Role: EMPLOYEE)

    %% -------------------------------------------------------------------------
    %% 1. Emissão de Fatura e Idempotência
    %% -------------------------------------------------------------------------
    Note over Analista,DB: 1. Emissão de Fatura e Garantia de Idempotência (Cenários 1 e 2)
    Analista->>+Billing: POST /api/v1/invoices<br/>{ workOrderId, amount: 350.00, customerDocument }
    Billing->>+DB: Verifica se já existe fatura para workOrderId
    DB-->>-Billing: Não localizada (nova fatura)
    Billing->>+DB: Salva fatura com status PENDING
    DB-->>-Billing: Confirma persistência (id, createdAt, version=0)
    Billing-->>-Analista: 201 Created (InvoiceDetailedResponse com _links)

    Note over Analista,DB: Rechamada com mesmo workOrderId: Retorno Idempotente (Cenário 2)
    Analista->>+Billing: POST /api/v1/invoices<br/>{ workOrderId, amount: 350.00, customerDocument }
    Billing->>+DB: Consulta fatura por workOrderId
    DB-->>-Billing: Fatura existente localizada
    Billing-->>-Analista: 200/201 OK (Retorna fatura existente sem duplicação)

    %% -------------------------------------------------------------------------
    %% 2. Validações de Domínio e Sad Paths
    %% -------------------------------------------------------------------------
    Note over Analista,Billing: 2. Validações de Domínio, Sad Paths e RBAC (Cenários 3, 4 e 8)
    Analista->>+Billing: POST /api/v1/invoices<br/>{ workOrderId, amount: 0.00, customerDocument }
    Billing-->>Analista: 400 Bad Request (Valor monetário inválido)

    Analista->>Billing: POST /api/v1/invoices<br/>{ workOrderId, amount: 250.00, customerDocument: "11111111111" }
    Billing-->>Analista: 422 Unprocessable Entity (CPF com dígitos inválidos)

    Note over Analista,Billing: Requisição sem token de autenticação (vide invoice_generation-non-func.feature)

    Analista->>Billing: POST /api/v1/invoices (Sem Header Authorization)
    Billing-->>-Analista: 401 Unauthorized (Acesso negado)

    %% -------------------------------------------------------------------------
    %% 3. Consultas Detalhadas HATEOAS e Tratamento de 404
    %% -------------------------------------------------------------------------
    Note over Analista,DB: 3. Consulta Detalhada e Tratamento 404 (Cenários 5 e 6)
    Analista->>+Billing: GET /api/v1/invoices/{id} ou /work-order/{woId}
    Billing->>+DB: Localiza fatura por chave primária ou workOrderId
    DB-->>-Billing: Registro localizado
    Billing-->>-Analista: 200 OK (InvoiceDetailedResponse com links HATEOAS)

    Analista->>+Billing: GET /api/v1/invoices/00000000-0000-0000-0000-000000000000
    Billing->>+DB: Consulta ID inexistente
    DB-->>-Billing: Optional.empty
    Billing-->>-Analista: 404 Not Found (ProblemDetail: RESOURCE_NOT_FOUND)

    %% -------------------------------------------------------------------------
    %% 4. Geração do Checkout Pix Mercado Pago
    %% -------------------------------------------------------------------------
    Note over Analista,MP: 4. Geração do Checkout Pix QR Code (Cenário 7)
    Analista->>+Billing: POST /api/v1/payments/mercadopago/checkout<br/>{ invoiceId: "UUID" }
    Billing->>+MP: POST /v1/orders (Cria ordem de pagamento QR Code Pix)
    MP-->>-Billing: Retorna qrCode, qrCodeBase64 e paymentId
    Billing->>+DB: Atualiza fatura com externalPaymentId
    DB-->>-Billing: Confirma atualização
    Billing-->>-Analista: 200 OK (CheckoutResponse com QR Code PIX)
```

</div>
