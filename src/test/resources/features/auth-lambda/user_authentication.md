## Diagrama de Sequência

**Autenticação de Usuários e Emissão de Tokens JWT (Referência: `user_authentication.md`)**

Este diagrama documenta a arquitetura de autenticação centralizada e emissão de tokens de acesso baseada em **OpenID Connect (OIDC)** e **OAuth 2.0**:
1. **Ponto Único de Login (`POST /auth/login`)**: Todos os atores da plataforma (Administrador, Funcionário e Cliente) autenticam-se através do endpoint unificado de autenticação, enviando suas credenciais seguras (usuário/documento e senha).
2. **Delegação ao Identity Provider (Keycloak)**: O serviço de autenticação consome internamente o Token Endpoint do **Keycloak** (`/realms/garage/protocol/openid-connect/token`) via fluxo padrão `grant_type=password`.
3. **Emissão de Tokens com Perfis Segregados**:
   - **Administrador (`ADMIN`)**: Token com privilégios totais de gestão de usuários e catálogos.
   - **Funcionário / Atendente (`EMPLOYEE`)**: Token com permissões operacionais para ordens de serviço, veículos e execuções técnicas.
   - **Cliente (`CUSTOMER`)**: Token restrito para acompanhamento de ordens de serviço e pagamentos próprios.
4. **Tratamento Defensivo de Falhas**: Em caso de credenciais incorretas, o Keycloak recusa a emissão com `401 Unauthorized`, e a API retorna uma resposta padronizada no padrão RFC 7807 (`ProblemDetail`).

> [!NOTE]
> **Gestão de Identidades**:
> O gerenciamento e cadastro prévio dos usuários no Keycloak e catálogo estão documentados em [user_identity_management.md](user_identity_management.md).

---

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Administrador /<br/>RH (ADMIN)
    actor Ator as Atendente<br/>(Funcionário: EMPLOYEE)
    actor Cliente as Cliente<br/>(CUSTOMER)
    participant Lambda as Auth API / Gateway<br/>(Endpoint /auth/login)
    participant KC as Keycloak (IdP / OIDC)<br/>(Realm: garage)

    %% -------------------------------------------------------------------------
    %% 1. Autenticação e Emissão de Token para Administrador (ADMIN)
    %% -------------------------------------------------------------------------
    Note over Admin,KC: 1. Autenticação e Emissão de Token para Administrador (ADMIN)
    Admin->>+Lambda: POST /auth/login<br/>{ username: "admin_user", password: "AdminPassword123!" }
    Lambda->>+KC: POST /realms/garage/protocol/openid-connect/token<br/>(grant_type=password, client_id=garage-backend)
    KC-->>-Lambda: 200 OK com Payload OIDC (Tokens + Claims)
    Lambda-->>-Admin: 200 OK com Access Token JWT<br/>(claims: roles: ["ADMIN"], sub: "uuid-admin")

    %% -------------------------------------------------------------------------
    %% 2. Autenticação e Emissão de Token para Funcionário / Atendente (EMPLOYEE)
    %% -------------------------------------------------------------------------
    Note over Ator,KC: 2. Autenticação e Emissão de Token para Funcionário / Atendente (EMPLOYEE)
    Ator->>+Lambda: POST /auth/login<br/>{ username: "86266070087", password: "EmployeePass123!" }
    Lambda->>+KC: POST /realms/garage/protocol/openid-connect/token<br/>(grant_type=password, client_id=garage-backend)
    KC-->>-Lambda: 200 OK com Payload OIDC (Tokens + Claims)
    Lambda-->>-Ator: 200 OK com Access Token JWT<br/>(claims: roles: ["EMPLOYEE"], sub: "uuid-employee")

    %% -------------------------------------------------------------------------
    %% 3. Autenticação e Emissão de Token para Cliente (CUSTOMER)
    %% -------------------------------------------------------------------------
    Note over Cliente,KC: 3. Autenticação e Emissão de Token para Cliente (CUSTOMER)
    Cliente->>+Lambda: POST /auth/login<br/>{ username: "43768298096", password: "CustomerPass123!" }
    Lambda->>+KC: POST /realms/garage/protocol/openid-connect/token<br/>(grant_type=password, client_id=garage-backend)
    KC-->>-Lambda: 200 OK com Payload OIDC (Tokens + Claims)
    Lambda-->>-Cliente: 200 OK com Access Token JWT<br/>(claims: roles: ["CUSTOMER"], sub: "uuid-customer")

    %% -------------------------------------------------------------------------
    %% 4. Tratamento Defensivo: Credenciais Inválidas (401 Unauthorized)
    %% -------------------------------------------------------------------------
    Note over Cliente,KC: 4. Tratamento Defensivo: Falha de Autenticação (Cenário de Erro)
    Cliente->>+Lambda: POST /auth/login<br/>{ username: "43768298096", password: "WrongPassword" }
    Lambda->>+KC: POST /realms/garage/protocol/openid-connect/token
    KC-->>-Lambda: 401 Unauthorized (invalid_grant)
    Lambda-->>-Cliente: 401 Unauthorized (ProblemDetail: INVALID_CREDENTIALS)
```
