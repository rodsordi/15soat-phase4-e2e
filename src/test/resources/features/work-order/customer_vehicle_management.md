## Diagrama de Sequência

**Gestão de Clientes e Veículos (Arquitetura Integrada Phase 3 / Phase 4)**

Este diagrama documenta o ciclo completo de onboarding de clientes e gestão de veículos, alinhado à arquitetura serverless da **Phase 3**:
1. **Onboarding Serverless com Saga Orquestrada**: A **AWS Lambda** recebe a requisição pública via Function URL, valida o CPF (algoritmo Módulo 11 da Receita Federal), provisiona a identidade no **Keycloak (IdP)** e propaga os dados para a **API Work Order** no cluster EKS. Caso a API falhe, a Lambda executa **Saga Compensatória (Rollback)** deletando o usuário no Keycloak.
2. **Consulta Direta de Usuário**: Verificação leve de existência e status de usuário diretamente no Keycloak via Lambda (`GET /users/{cpf}`).
3. **Gestão de Veículos e Operações Autenticadas**: Operações de negócio realizadas via **Kong API Gateway** utilizando token **Bearer JWT** emitido pelo Keycloak.

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 950px;">

```mermaid
sequenceDiagram
    autonumber
    actor Ator as Atendente<br/>(Funcionário: EMPLOYEE)
    participant Lambda as AWS Lambda<br/>(Auth & Onboarding)
    participant KC as Keycloak IdP<br/>(IAM / OIDC)
    participant Kong as Kong API Gateway<br/>(Ingress / Proxy)
    participant WO as API Work Order<br/>(Spring Boot 4 / PG)
    participant DB as PostgreSQL<br/>(work_order_db)

    Note over Ator,Kong: 🔒 Sessão Autenticada: Requisições utilizam Bearer JWT (Role: EMPLOYEE)

    %% 1. Onboarding Serverless com Saga Orquestrada
    Note over Ator,DB: 1. Atendente Cadastra Cliente na Recepção (Saga Orquestrada: Keycloak + API)
    Ator->>+Lambda: POST /users<br/>{ document: "52998224725",<br/>  name, email, password }
    Lambda->>Lambda: Valida role EMPLOYEE<br/>e algoritmo Módulo 11 (CPF)
    Lambda->>+KC: POST /admin/realms/garage/users<br/>(Cria identidade com Unified ID)
    KC-->>-Lambda: 201 Created (userId: uuid)
    Lambda->>+Kong: POST /api/v1/customers<br/>{ id: uuid, document, name, email }
    Kong->>+WO: Encaminha requisição autenticada
    WO->>+DB: Persiste cadastro do cliente
    DB-->>-WO: Confirma persistência
    WO-->>-Kong: 201 Created (CustomerResponse)
    Kong-->>-Lambda: 201 Created
    Lambda-->>-Ator: 201 Created<br/>(Usuário provisionado no IAM e Catálogo)

    %% 2. Saga Compensatória (Rollback)
    Note over Ator,DB: 2. Cenário de Falha na API: Execução de Rollback Compensatório
    Ator->>+Lambda: POST /users<br/>(Dados válidos)
    Lambda->>+KC: POST /admin/realms/garage/users
    KC-->>-Lambda: 201 Created (userId: uuid)
    Lambda->>+Kong: POST /api/v1/customers
    Kong->>+WO: Encaminha requisição autenticada
    WO->>+DB: Persiste cadastro do cliente
    DB-->>-WO: Falha / Conflito no banco
    WO-->>-Kong: 500 / 409 Error
    Kong-->>-Lambda: 500 / 409 Error
    Note over Lambda,KC: Saga Rollback: Compensação Imediata
    Lambda->>+KC: DELETE /admin/realms/garage/users/{uuid}
    KC-->>-Lambda: 204 No Content (Usuário removido)
    Lambda-->>-Ator: 502 Bad Gateway<br/>(Cadastro revertido no IAM)

    %% 3. Consulta Rápida de Usuário por CPF
    Note over Ator,KC: 3. Consulta Leve de Status Cadastral via Lambda / Keycloak
    Ator->>+Lambda: GET /users/52998224725
    Lambda->>Lambda: Valida formato do CPF
    Lambda->>+KC: GET /admin/realms/garage/users?username=52998224725
    KC-->>-Lambda: Retorna status (ACTIVE / DISABLED, roles)
    Lambda-->>-Ator: 200 OK { exists: true, status: "ACTIVE" }

    %% 4. Gestão Autenticada de Veículos via API Gateway
    Note over Ator,DB: 4. Cadastro e Associação de Veículo via API Gateway (Bearer JWT)
    Ator->>+Kong: POST /api/v1/vehicles<br/>{ licensePlate: "BRA2E19", customerDocument, make, model }
    Kong->>+WO: Encaminha requisição autenticada
    WO->>+DB: Localiza cliente e persiste vínculo do veículo
    DB-->>-WO: Confirma persistência
    WO-->>-Kong: 201 Created (VehicleResponse)
    Kong-->>-Ator: 201 Created

    %% 5. Sad Paths de Veículo
    Note over Ator,DB: 5. Tratamento de Exceções de Domínio de Veículo
    Ator->>+Kong: POST /api/v1/vehicles<br/>(Placa duplicada ou cliente inexistente)
    Kong->>+WO: Encaminha requisição autenticada
    WO->>+DB: Consulta integridade cadastral
    DB-->>-WO: Registro não encontrado / Placa já existe
    WO-->>-Kong: Retorna 400/409 (ProblemDetail RFC 7807)
    Kong-->>-Ator: Retorna 400/409 Bad Request

    %% 6. Atualização Parcial de Cliente via PATCH RESTful
    Note over Ator,DB: 6. Atualização Parcial de Cliente via PATCH RESTful
    Ator->>+Kong: PATCH /api/v1/customers/{id}<br/>{ name, email }
    Kong->>+WO: Encaminha PATCH autenticado
    WO->>+DB: Atualiza campos cadastrais e renova updatedAt
    DB-->>-WO: Confirma atualização
    WO-->>-Kong: 200 OK (CustomerResponse com updatedAt)
    Kong-->>-Ator: 200 OK

    %% 7. Exclusão Segura por Identificador Canônico (DELETE /{id})
    Note over Ator,DB: 7. Exclusão de Recurso por Identificador Canônico (DELETE /{id})
    Ator->>+Kong: DELETE /api/v1/customers/{id}
    Kong->>+WO: Encaminha DELETE autenticado
    WO->>+DB: Valida ausência de veículos e ordens ativas
    DB-->>-WO: Sem impedimentos
    WO->>+DB: Remove cliente da base
    DB-->>-WO: Confirma exclusão
    WO-->>-Kong: 204 No Content
    Kong-->>-Ator: 204 No Content

    %% 8. Bloqueio de Exclusão por Integridade Referencial (422 Unprocessable Entity)
    Note over Ator,DB: 8. Bloqueio de Exclusão com Veículos Associados ou OS Ativas
    Ator->>+Kong: DELETE /api/v1/customers/{id}
    Kong->>+WO: Encaminha DELETE autenticado
    WO->>+DB: Identifica veículos ou ordens em andamento vinculadas
    DB-->>-WO: Violação de integridade de negócio
    WO-->>-Kong: 422 Unprocessable Entity (ProblemDetail RFC 7807)
    Kong-->>-Ator: 422 Unprocessable Entity
```

</div>
</div>
