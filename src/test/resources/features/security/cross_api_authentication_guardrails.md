# Diagrama de Sequência: Validação Transversal de Segurança e Autenticação de APIs

> **Status**: Aprovado  
> **Referência Normativa**: [ADR-004: Padrão Visual para Diagramas de Sequência Mermaid](../../docs/adr/004-padrao-visual-diagramas-sequencia-mermaid.md)  
> **Especificação BDD Associada**: [cross_api_authentication_guardrails.feature](cross_api_authentication_guardrails.feature)  
> **Origem dos Tokens Válidos**: [user_authentication.md](../auth-lambda/user_authentication.md)

---

## 1. Visão Geral da Arquitetura de Segurança (Defense-in-Depth)

No modelo Zero Trust adotado pela Fase 4, nenhuma chamada interna ou externa trafega desprotegida. Todas as APIs (`work-order`, `billing`, `exec`) operam como **OAuth2 Resource Servers** protegidos por filtros do Spring Security (`BearerTokenAuthenticationFilter` e `SecurityFilterChain`).

Qualquer requisição que atinja os microsserviços sem o cabeçalho `Authorization: Bearer <token>` ou contendo um token sintaticamente malformado, forjado ou expirado é **imediatamente interceptada e rejeitada na camada de filtro web**, antes mesmo de invocar o Controller de negócio ou consumir recursos do banco de dados ou mensageria.

---

## 2. Diagrama de Sequência: Bloqueio Transversal de Requisições Não Autenticadas (HTTP 401)

<div style="overflow-x: auto; zoom: 1.5; min-width: 900px;">

```mermaid
sequenceDiagram
    autonumber
    actor Ator as Ator Externo / Cliente HTTP
    participant SecFilter as Spring Security Filter<br/>(BearerTokenFilter)
    participant ResourceServer as OAuth2 Resource Server<br/>(JwtAuthenticationProvider)
    participant Controller as Microservice Controller<br/>(WorkOrder / Billing / Exec)

    Note over Ator,Controller: 🔒 Borda Segura Zero Trust: Nenhuma requisição desautenticada atinge a camada de negócio

    %% =========================================================================
    %% Fluxo 1: Requisição Sem Cabeçalho de Autenticação (Cenário 1)
    %% =========================================================================
    rect rgb(253, 242, 242)
        Note over Ator,Controller: [Cenário 1] Tentativa de Acesso Sem Cabeçalho Authorization
        Ator->>+SecFilter: GET / POST /api/v1/{resource}<br/>Headers: [Authorization: ausente]
        SecFilter->>SecFilter: Verifica ausência de credenciais Bearer<br/>(AuthenticationException disparada)
        SecFilter-->>-Ator: 401 Unauthorized<br/>WWW-Authenticate: Bearer resource_metadata="..."
    end

    %% =========================================================================
    %% Fluxo 2: Requisição com Token JWT Malformado / Inválido (Cenário 2)
    %% =========================================================================
    rect rgb(254, 249, 195)
        Note over Ator,Controller: [Cenário 2] Tentativa de Acesso com Token Malformado / Inválido
        Ator->>+SecFilter: GET / POST /api/v1/{resource}<br/>Headers: Authorization: Bearer {malformed}
        SecFilter->>+ResourceServer: Submete token para validação e decodificação
        ResourceServer-->>-SecFilter: Rejeição: BadJwtException / Parse Failure
        SecFilter-->>-Ator: 401 Unauthorized<br/>WWW-Authenticate: Bearer error="invalid_token"
    end

    %% =========================================================================
    %% Fluxo 3: Requisição Legítima com Token Válido (Cenário 3 - Controle de Sanidade)
    %% =========================================================================
    rect rgb(240, 253, 244)
        Note over Ator,Controller: [Cenário 3] Requisição com Bearer Token Assinado pelo Keycloak
        Ator->>+SecFilter: GET / POST /api/v1/{resource}<br/>Headers: Authorization: Bearer {valid_jwt}
        SecFilter->>+ResourceServer: Decodifica e valida assinatura RS256 via JWKS
        ResourceServer->>ResourceServer: Extrai roles, claims e tenant
        ResourceServer-->>-SecFilter: Retorna JwtAuthenticationToken populado
        SecFilter->>+Controller: Encaminha requisição autenticada
        Controller-->>-SecFilter: 200 OK / 201 Created (Resposta de Negócio)
        SecFilter-->>-Ator: 200 OK / 201 Created
    end
```

</div>
