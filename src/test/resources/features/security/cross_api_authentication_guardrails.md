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

<div style="overflow-x: auto; width: 100%;">
<style>
  .mermaid svg {
    max-width: none !important;
    min-width: 950px !important;
    width: 950px !important;
  }
</style>

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
        Ator->>SecFilter: GET / POST /api/v1/{resource}<br/>Headers: [Authorization: ausente]
        activate SecFilter
        SecFilter->>SecFilter: Verifica ausência de credenciais Bearer<br/>(AuthenticationException disparada)
        SecFilter-->>Ator: 401 Unauthorized<br/>WWW-Authenticate: Bearer resource_metadata="..."
        deactivate SecFilter
    end

    %% =========================================================================
    %% Fluxo 2: Requisição com Token JWT Malformado / Inválido (Cenário 2)
    %% =========================================================================
    rect rgb(254, 249, 195)
        Note over Ator,Controller: [Cenário 2] Tentativa de Acesso com Token Malformado / Inválido
        Ator->>SecFilter: GET / POST /api/v1/{resource}<br/>Headers: Authorization: Bearer {malformed}
        activate SecFilter
        SecFilter->>ResourceServer: Submete token para validação e decodificação
        activate ResourceServer
        ResourceServer-->>SecFilter: Rejeição: BadJwtException / Parse Failure
        deactivate ResourceServer
        SecFilter-->>Ator: 401 Unauthorized<br/>WWW-Authenticate: Bearer error="invalid_token",<br/>error_description="Bearer token is malformed"
        deactivate SecFilter
    end

    Note over Controller: Microservice Controller nunca é acionado nestes cenários,<br/>garantindo proteção contra sobrecarga de I/O e banco de dados.
```

</div>

---

## 3. Matriz de Paridade com os Cenários BDD

| Cenário Gherkin | Rota / Método Auditado | Comportamento de Interceptação | Status HTTP |
| :--- | :--- | :--- | :---: |
| **Cenário 1: Token Ausente** | Todas as rotas de `work-order`, `billing`, `exec` | Interceptado no `BearerTokenAuthenticationFilter` por ausência de credenciais | **401 Unauthorized** |
| **Cenário 2: Token Malformado** | Todas as rotas de `work-order`, `billing`, `exec` | Interceptado na tentativa de parsing do token (`BadJwtException`) | **401 Unauthorized** |
