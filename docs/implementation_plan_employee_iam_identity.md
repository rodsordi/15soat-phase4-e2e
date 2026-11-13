# Plano de Implementação: Funcionário como Identidade IAM (Keycloak) e Auditoria por Claims JWT

> **Decisão do Usuário**: Opção 1 - Funcionário como Identidade IAM (Keycloak) + Auditoria por Claims JWT
> **Status**: Aprovado para Execução

---

## 1. Contexto e Declaração do Problema
No fluxo operacional da oficina mecânica, os clientes não realizam autoatendimento desassistido na recepção; eles são atendidos e cadastrados presencialmente por um **funcionário da oficina** (atendente/recepcionista).

Para suportar esse fluxo de forma moderna e desacoplada:
1. **Evitar duplicação desnecessária de tabelas**: O funcionário não precisa de uma tabela relacional de RH própria no banco `work_order_db`, pois não possui atributos de faturamento ou catálogo. Ele é uma **identidade autenticada** gerenciada pelo Identity Provider (**Keycloak**).
2. **Controle de Acesso RBAC**: Apenas usuários com perfil/role `EMPLOYEE` ou `ADMIN` têm permissão para cadastrar clientes, veículos e abrir Ordens de Serviço.
3. **Rastreabilidade e Auditoria Transacional**: Ao persistir entidades como `customer` ou `work_order`, o identificador do funcionário que operou o sistema é extraído das claims do token Bearer JWT (`sub` ou `preferred_username`) e registrado nas colunas de auditoria (`created_by`, `last_modified_by`).

---

## 2. Desenho Arquitetural da Solução

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Administrador da Oficina
    actor Func as Funcionário (Atendente)
    participant Lambda as AWS Lambda<br/>(garage-auth-handler)
    participant KC as Keycloak IdP<br/>(Realm: garage)
    participant Kong as Kong API Gateway
    participant WO as API Work Order<br/>(Spring Boot 4 / Hexagonal)
    participant DB as PostgreSQL<br/>(work_order_db)

    %% 1. Cadastro do Funcionário no IAM
    Note over Admin,KC: 1. Provisionamento do Funcionário no Keycloak (IAM)
    Admin->>+Lambda: POST /users<br/>Header: Authorization: Bearer Admin_JWT<br/>{ role: "EMPLOYEE", name, email, document, password }
    Lambda->>Lambda: Valida role ADMIN do solicitante<br/>e valida CPF (Módulo 11)
    Lambda->>+KC: POST /admin/realms/garage/users<br/>(Cria usuário com atributo role: EMPLOYEE)
    KC-->>-Lambda: 201 Created (userId: uuid)
    Lambda-->>-Admin: 201 Created (Funcionário provisionado)

    %% 2. Login do Funcionário
    Note over Func,KC: 2. Autenticação e Emissão de Token JWT
    Func->>+Lambda: POST /auth/login<br/>{ username: "CPF", password: "***" }
    Lambda->>+KC: POST /realms/garage/protocol/openid-connect/token
    KC-->>-Lambda: Retorna Access Token JWT<br/>(claims: sub, preferred_username, roles: ["EMPLOYEE"])
    Lambda-->>-Func: 200 OK com Access Token JWT

    %% 3. Cadastro do Cliente pelo Funcionário Autenticado
    Note over Func,DB: 3. Atendente Cadastra Cliente na Recepção
    Func->>+Kong: POST /api/v1/customers<br/>Header: Authorization: Bearer Func_JWT<br/>{ document: "52998224725", name: "João", email: "joao@email.com" }
    Kong->>Kong: Valida assinatura JWT via JWKS do Keycloak
    Kong->>+WO: Proxy Request com cabeçalhos de contexto (X-User-Id / Claims)
    WO->>WO: Spring Security checa autoridade ROLE_EMPLOYEE
    WO->>WO: Extrai sub/username (ex: "attendant_cpf")
    WO->>+DB: INSERT INTO customer (id, name, email, document, created_by)<br/>VALUES (uuid, 'João', 'joao@email.com', '52998224725', 'attendant_cpf')
    DB-->>-WO: Confirmação de persistência
    WO-->>-Kong: 201 Created (CustomerResponse)
    Kong-->>-Func: 201 Created
```

---

## 3. Mudanças a Serem Aplicadas no Repositório

### 3.1. Documentação Viva & Diagramas de BDD (`15soat-phase4-e2e`)
- **Arquivo**: `src/test/resources/features/work-order/customer_vehicle_management.md`
  - Incorporar o fluxo onde o ator principal é explicitamente o `Atendente (Funcionário Autenticado com Role EMPLOYEE)`.
  - Documentar a passagem do header `Authorization: Bearer Employee_JWT`.
  - Explicar nas notas técnicas que a identificação do funcionário é auditada no banco via claim JWT sem requerer tabela `employee`.

### 3.2. Registro de Decisão Arquitetural (`ADR-005`)
- **Arquivo [NEW]**: `docs/adr/005-gestao-identidade-funcionarios-auditoria-jwt.md`
  - **Contexto**: Avaliação entre criar uma tabela relacional `employee` vs. centralizar no IAM Keycloak.
  - **Decisão**: Adoção do Keycloak como única fonte da verdade para identidades e permissões de funcionários com auditoria de claims (`created_by`) no PostgreSQL.
  - **Consequências**: Eliminação de redundâncias, menor acoplamento e suporte nativo a RBAC.
- **Arquivo [MODIFY]**: `docs/adr/README.md`
  - Atualizar a matriz de decisões incluindo a ADR-005.

### 3.3. Cenários de BDD (`customer_vehicle_management.feature`)
- Garantir que os cenários expressem que as requisições administrativas/atendimento são emitidas por um atendente autenticado:
  ```gherkin
  Given an authenticated attendant with "EMPLOYEE" role
  When the attendant creates a new customer with the following details:
  ...
  ```

---

## 4. Plano de Verificação e Testes
1. **Verificação de Documentação**: Validar se o Mermaid em `customer_vehicle_management.md` compila perfeitamente sem falhas de sintaxe e com activation boxes (`+`/`-`).
2. **Rastreabilidade Git**: Confirmar status com `git status -s`.
3. **Aprovação Prévia**: Consultar o usuário para autorização formal antes de qualquer commit ou push.
