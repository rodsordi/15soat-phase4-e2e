## Diagrama de Sequência

**Gestão de Identidade e Autenticação de Funcionários (Referência: `employee_identity_management.feature`)**

Este diagrama documenta o ciclo de vida do colaborador da oficina:
1. **Provisionamento Seguro pelo Administrador**: Cadastro via **AWS Lambda** (`POST /users`), que valida permissão administrativa, validação algorítmica de CPF (Módulo 11) e cria o usuário no **Keycloak (IdP)** com a role `EMPLOYEE`.
2. **Autenticação OIDC & Emissão de JWT**: O colaborador realiza o login via `POST /auth/login`, obtendo o token JWT contendo as claims necessárias (`roles: ["EMPLOYEE"]`, `sub`, `preferred_username`) para operar o sistema no API Gateway.

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 800px;">

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Administrador /<br/>RH
    actor Func as Funcionário<br/>(Atendente)
    participant Lambda as AWS Lambda<br/>(garage-auth-handler)
    participant KC as Keycloak IdP<br/>(Realm: garage)
    participant KCDB as PostgreSQL<br/>(keycloak_db)

    Note over Admin,Lambda: 🔒 Sessão Autenticada: Requisições utilizam Bearer JWT (Role: ADMIN)

    %% 1. Cadastro de Funcionário pelo Administrador
    Note over Admin,KCDB: 1. Provisionamento de Colaborador no Keycloak (IAM)
    Admin->>+Lambda: POST /users<br/>{ role: "EMPLOYEE", name: "Carlos",<br/>  document: "86266070087", email, password }
    Lambda->>Lambda: Valida role ADMIN do solicitante<br/>e algoritmo Módulo 11 (CPF)
    Lambda->>+KC: POST /admin/realms/garage/users<br/>(Bearer Admin Token)
    KC->>+KCDB: Persiste credenciais,<br/>atributos e role EMPLOYEE
    KCDB-->>-KC: Confirma persistência
    KC-->>-Lambda: Retorna 201 Created (userId: uuid)
    Lambda-->>-Admin: Retorna 201 Created<br/>(Funcionário provisionado no IAM)

    %% 2. Rejeição com CPF Inválido
    Note over Admin,Lambda: 2. Sad Path: Tentativa de Cadastro com CPF Inválido
    Admin->>+Lambda: POST /users<br/>{ document: "11111111111", ... }
    Lambda->>Lambda: Detecta dígitos repetidos / inválidos
    Lambda-->>-Admin: Retorna 400 Bad Request<br/>(ProblemDetail: INVALID_DOCUMENT)

    %% 3. Autenticação do Funcionário (Login)
    Note over Func,KCDB: 3. Autenticação e Emissão de Token JWT com Role EMPLOYEE
    Func->>+Lambda: POST /auth/login<br/>{ username: "86266070087",<br/>  password: "StrongP@ss2026" }
    Lambda->>+KC: POST /realms/garage/protocol/openid-connect/token<br/>(grant_type=password)
    KC->>+KCDB: Valida credenciais e busca roles
    KCDB-->>-KC: Credenciais válidas (role: EMPLOYEE)
    KC-->>-Lambda: Retorna Tokens OIDC (access_token, refresh_token)
    Lambda-->>-Func: Retorna 200 OK com Access Token JWT<br/>(claims: roles: ["EMPLOYEE"], sub, username)

    %% 4. Tentativa de Login com Senha Incorreta
    Note over Func,KC: 4. Sad Path: Falha de Autenticação
    Func->>+Lambda: POST /auth/login<br/>{ username: "86266070087", password: "errada" }
    Lambda->>+KC: POST /realms/garage/protocol/openid-connect/token
    KC-->>-Lambda: 401 Unauthorized (Invalid credentials)
    Lambda-->>-Func: Retorna 401 Unauthorized<br/>(ProblemDetail: INVALID_CREDENTIALS)
```

</div>
</div>
