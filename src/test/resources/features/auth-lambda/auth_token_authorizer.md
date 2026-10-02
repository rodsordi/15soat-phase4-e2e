## Diagrama de Sequência

**Autenticação e Autorização Serverless via Lambda Token Authorizer (Referência: `auth_token_authorizer.feature`)**

Este diagrama documenta a validação stateless de tokens Bearer JWT no API Gateway através do AWS Lambda Token Authorizer, controlando o acesso aos microsserviços privados.

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 900px;">

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Cliente /<br/>Operador
    participant Gateway as AWS API Gateway /<br/>Kong Ingress
    participant Lambda as Lambda Token Authorizer<br/>(Node.js 20)
    participant Service as Microsserviço Privado<br/>(Work Order / Billing / Exec)

    %% Token Válido
    Note over Cliente,Service: 1. Requisição com Token Válido (Effect: Allow)
    Cliente->>Gateway: GET /api/v1/work-orders<br/>(Header Authorization: Bearer JWT)
    Gateway->>Lambda: Invoca com event.authorizationToken
    Lambda->>Lambda: Valida assinatura e formato do token
    Lambda-->>Gateway: Retorna IAM Policy { Effect: "Allow", PrincipalId }
    Gateway->>Service: Encaminha requisição HTTP com contexto do usuário
    Service-->>Gateway: Retorna 200 OK
    Gateway-->>Cliente: Retorna 200 OK

    %% Token Inválido
    Note over Cliente,Gateway: 2. Requisição com Token Inválido (Effect: Deny)
    Cliente->>Gateway: GET /api/v1/work-orders<br/>(Header Authorization: Bearer deny)
    Gateway->>Lambda: Invoca com event.authorizationToken
    Lambda->>Lambda: Detecta token inválido / revogado
    Lambda-->>Gateway: Retorna IAM Policy { Effect: "Deny" }
    Gateway-->>Cliente: Retorna 403 Forbidden
```

</div>
</div>
