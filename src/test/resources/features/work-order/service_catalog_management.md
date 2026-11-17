## Diagrama de Sequência

**Gestão do Catálogo de Serviços (Referência: `service_catalog_management.feature`)**

Este diagrama documenta a gestão e padronização dos serviços oferecidos pela oficina no catálogo corporativo:
1. **Cadastro de Serviço Padronizado (`POST /api/v1/services`)**: Provisionamento administrativo de tipos de serviços com código único e tabela de preços (Cenário 1).
2. **Consulta e Localização de Serviço por Código (`GET /api/v1/services?code=...`)**: Recuperação rápida de procedimentos e valores vigentes para montagem de orçamentos (Cenário 2).
3. **Tratamento de Sad Path (Código Duplicado)**: Rejeição com código `400 Bad Request` perante tentativa de cadastro de serviço com código já existente na base (Cenário 3).

> [!NOTE]
> **🔒 Autenticação & Emissão de JWT**: A obtenção e emissão de tokens Bearer JWT via Keycloak estão detalhadas no diagrama [user_authentication.md](../auth-lambda/user_authentication.md).

---

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Administrador<br/>(ADMIN)
    participant WO as API Work Order<br/>(Spring Boot 4 / PG)
    participant DB as PostgreSQL<br/>(work_order_db)

    Note over Admin,WO: 🔒 Borda Autenticada via Bearer JWT (vide user_authentication.md):<br/>Requisições utilizam token OIDC emitido pelo Keycloak (Role: ADMIN)

    %% 1. Cadastro de Serviço Padronizado
    Note over Admin,DB: 1. Cadastro de Serviço Padronizado no Catálogo
    Admin->>+WO: POST /api/v1/services<br/>{ code, name, price }
    WO->>+DB: Persiste novo serviço no catálogo
    DB-->>-WO: Confirma persistência
    WO-->>-Admin: 201 Created (ServiceCatalogResponse)

    %% 2. Consulta de Serviço por Código
    Note over Admin,DB: 2. Consulta do Serviço por Código
    Admin->>+WO: GET /api/v1/services?code=SRV-OIL-01
    WO->>+DB: Consulta serviço por código
    DB-->>-WO: Serviço localizado
    WO-->>-Admin: 200 OK [ServiceCatalogResponse]

    %% 3. Sad Path: Código Duplicado
    Note over Admin,DB: 3. Tentativa de Cadastro com Código Duplicado (Cenário 3)
    Admin->>+WO: POST /api/v1/services<br/>{ code: "SRV-OIL-01" (Já existente), name, price }
    WO->>+DB: Consulta existência do código
    DB-->>-WO: Código já cadastrado
    WO-->>-Admin: 400 Bad Request (ProblemDetail: RESOURCE_ALREADY_EXISTS)
```
