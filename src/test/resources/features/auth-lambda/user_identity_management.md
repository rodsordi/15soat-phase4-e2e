## Diagrama de Sequência

**Gestão e Provisionamento de Identidades de Usuários (Referência: `user_identity_management.feature`)**

Este diagrama documenta o provisionamento de identidades e gerenciamento corporativo via Serverless & Keycloak (IAM) na ordem cronológica de operação:
1. **Administrador Cadastra Funcionário (IAM Corporativo Puro)**: O Administrador/RH provisiona colaboradores da oficina (`POST /users` com role `EMPLOYEE`), gerenciados estritamente no **Keycloak (IdP)**. A API Work Order não gerencia funcionários e não é acionada.
2. **Validação Defensiva de Documento (400 Bad Request)**: Tentativas de provisionamento com CPF inválido são rejeitadas de imediato na Lambda pelo algoritmo Módulo 11 sem onerar o IdP.
3. **Atendente Cadastra Cliente (Saga Orquestrada em 2 Etapas)**: Com o acesso liberado, o Atendente atende o cliente no balcão da oficina via **AWS Lambda** (`POST /users` com role `CUSTOMER`), que valida o CPF (Módulo 11 da Receita Federal), provisiona a identidade no **Keycloak (IdP)** e propaga os dados para a **API Work Order** (`POST /api/v1/customers`).
4. **Saga Compensatória (Rollback)**: Caso a **API Work Order** falhe ou apresente indisponibilidade no banco, a Lambda executa imediatamente a compensação distribuída (`DELETE /admin/realms/garage/users/{uuid}`) no Keycloak, retornando erro 502 ao Atendente.

> [!NOTE]
> **Autenticação OIDC & Emissão de JWT**:
> O fluxo de login e emissão de tokens de acesso JWT via Keycloak está documentado em [user_authentication.md](user_authentication.md).

<div style="overflow-x: auto; width: 100%;">
<style>
  .mermaid svg {
    max-width: none !important;
    min-width: 1400px !important;
    width: 1400px !important;
  }
</style>

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Administrador /<br/>RH (ADMIN)
    actor Ator as Atendente<br/>(Funcionário: EMPLOYEE)
    participant Lambda as AWS Lambda<br/>(garage-auth-handler)
    participant KC as Keycloak IdP<br/>(Realm: garage)
    participant WO as API Work Order<br/>(Spring Boot 4 / PG)
    participant DB as PostgreSQL<br/>(work_order_db)

    Note over Admin,WO: 🔒 Borda Autenticada via Bearer JWT (vide user_authentication.md):<br/>Requisições administrativas utilizam tokens emitidos pelo Keycloak (Roles: ADMIN / EMPLOYEE)

    %% -------------------------------------------------------------------------
    %% 1. Administrador Cadastra Funcionário (IAM Corporativo Puro)
    %% -------------------------------------------------------------------------
    Note over Admin,KC: 1. Administrador Cadastra Funcionário (IAM Puro / Sem Work Order)
    Admin->>+Lambda: POST /users<br/>{ role: "EMPLOYEE", name: "Carlos",<br/>  document: "86266070087", email, password }
    Lambda->>Lambda: Valida role ADMIN do solicitante<br/>e algoritmo Módulo 11 (CPF)
    Lambda->>+KC: POST /admin/realms/garage/users<br/>(Bearer Admin Token)
    KC-->>-Lambda: 201 Created (userId: uuid)
    Note over Lambda: Colaborador não possui entidade no catálogo:<br/>Provisionamento concluído no IAM
    Lambda-->>-Admin: 201 Created<br/>(Funcionário provisionado no IAM com role EMPLOYEE)

    %% -------------------------------------------------------------------------
    %% 1b. Recusa de Cadastro com Documento Inválido (400 Bad Request)
    %% -------------------------------------------------------------------------
    Note over Admin,Lambda: 1b. Validação Defensiva: Recusa de CPF Inválido (Cenário 2)
    Admin->>+Lambda: POST /users<br/>{ role: "EMPLOYEE", name: "Funcionário Teste",<br/>  document: "11111111111", email, password }
    Lambda->>Lambda: Falha na validação do algoritmo Módulo 11 (CPF)
    Lambda-->>-Admin: 400 Bad Request<br/>(ProblemDetail: INVALID_DOCUMENT)

    %% -------------------------------------------------------------------------
    %% 2. Atendente Cadastra Cliente (Saga Orquestrada com Sucesso)
    %% -------------------------------------------------------------------------
    Note over Ator,DB: 2. Atendente Cadastra Cliente na Recepção (Saga Orquestrada: Keycloak + API Work Order)
    Ator->>+Lambda: POST /users<br/>{ role: "CUSTOMER", name: "Rodrigo",<br/>  document: "52998224725", email, password }
    Lambda->>Lambda: Valida formato do documento e<br/>algoritmo Módulo 11 (CPF)
    Lambda->>+KC: POST /admin/realms/garage/users<br/>(Cria identidade com Unified ID)
    KC-->>-Lambda: 201 Created (userId: uuid)
    Lambda->>+WO: POST /api/v1/customers<br/>{ id: uuid, document, name, email }
    WO->>+DB: Persiste cadastro do cliente
    DB-->>-WO: Confirma persistência
    WO-->>-Lambda: 201 Created (CustomerResponse)
    Lambda-->>-Ator: 201 Created<br/>(Cliente provisionado no IAM e Catálogo)

    %% -------------------------------------------------------------------------
    %% 3. Saga Compensatória (Rollback) por Falha na API Work Order
    %% -------------------------------------------------------------------------
    Note over Ator,DB: 3. Falha na API Work Order: Execução de Rollback Compensatório (Saga Rollback)
    Ator->>+Lambda: POST /users<br/>(Dados cadastrais do cliente)
    Lambda->>Lambda: Valida documento (CPF)
    Lambda->>+KC: POST /admin/realms/garage/users
    KC-->>-Lambda: 201 Created (userId: uuid)
    Lambda->>+WO: POST /api/v1/customers<br/>{ id: uuid, document, name, email }
    WO->>+DB: Tenta persistir no banco
    DB-->>-WO: Falha de conexão / Conflito
    WO-->>-Lambda: 500 Internal Server Error
    Note over Lambda,KC: Saga Compensatória: Exclusão Imediata no IdP
    Lambda->>+KC: DELETE /admin/realms/garage/users/{uuid}
    KC-->>-Lambda: 204 No Content (Identidade removida)
    Lambda-->>-Ator: 502 Bad Gateway<br/>(ProblemDetail: REGISTRATION_ROLLBACK)
```

</div>
