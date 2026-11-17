# Plano de Implementação: Saga Orquestrada de Identidade e Acesso (Clientes e Funcionários)

> **Decisão do Usuário**: Opção 1 - Diagrama Unificado da Saga Orquestrada com Rollback & IAM Puro
> **Status**: Aprovado para Execução

---

## 1. Contexto & Objetivos

Mapear a arquitetura real da função serverless AWS Lambda (`garage-auth-handler`) no documento `features/auth-lambda/employee_identity_management.md`.
A Lambda atua como orquestradora de transação distribuída (Saga Orquestrada) no onboarding de clientes e como autoridade de provisionamento corporativo (IAM) para colaboradores:

1. **Jornada do Cliente (`CUSTOMER`) — Saga Orquestrada em 2 Etapas**:
   - Criação de credenciais no Keycloak com `role: CUSTOMER` obtendo o `userId` (UUID).
   - Propagação síncrona para a **API Work Order** (`POST /api/v1/customers`), persistindo no banco relacional `work_order_db`.
   - **Rollback Compensatório**: Em caso de falha ou timeout na API Work Order, a Lambda executa imediatamente `DELETE /admin/realms/garage/users/{uuid}` no Keycloak e retorna erro 502 ao cliente.
2. **Jornada do Funcionário (`EMPLOYEE` / `ADMIN`) — IAM Corporativo Puro**:
   - Provisionamento administrativo estritamente no Keycloak. A API Work Order não possui catálogo de funcionários e não é acionada.
3. **Autenticação OIDC & Emissão de JWT**:
   - Login via `POST /auth/login` gerando tokens Bearer JWT com claims segregadas (`roles: ["CUSTOMER"]` vs `roles: ["EMPLOYEE"]`).

O cliente ocupará mandatoriamente a primeira coluna (extremo esquerdo) do diagrama, e a nota de borda perimetral manterá a referência explícita a `auth_token_authorizer.md`.

---

## 2. Estrutura do Diagrama Mermaid (7 Participantes)

- **Participantes**:
  - `actor Cliente as Cliente<br/>(CUSTOMER)` (Coluna 1)
  - `actor Func as Funcionário<br/>(Atendente: EMPLOYEE)` (Coluna 2)
  - `actor Admin as Administrador /<br/>RH (ADMIN)` (Coluna 3)
  - `participant Lambda as AWS Lambda<br/>(garage-auth-handler)` (Coluna 4 — Orquestradora da Saga)
  - `participant KC as Keycloak IdP<br/>(Realm: garage)` (Coluna 5)
  - `participant WO as API Work Order<br/>(Spring Boot 4 / PG)` (Coluna 6)
  - `participant DB as PostgreSQL<br/>(work_order_db)` (Coluna 7)

- **Cenários do Diagrama**:
  - `1. Onboarding do Cliente (Saga Orquestrada: Keycloak + API Work Order)`
  - `2. Falha na API: Execução de Rollback Compensatório (Saga Rollback no Keycloak)`
  - `3. Provisionamento Administrativo do Funcionário (IAM Corporativo Puro)`
  - `4. Autenticação OIDC & Emissão de JWT com Segregação de Papéis (CUSTOMER & EMPLOYEE)`

- **Dimensionamento ADR-004**:
  - Largura mínima do SVG configurada em `1550px`.
  - Preservado o callout `[!NOTE]` com link navegável para `auth_token_authorizer.md`.
  - Preservada a nota perimetral `🔒 Sessão Autenticada via Token Authorizer (vide auth_token_authorizer.md)`.

---

## 3. Critérios de Aceite

1. Paridade exata de activation boxes (`+` e `-`), sem ativações em atores humanos.
2. `Cliente` na primeira coluna (extremo esquerdo).
3. Saga orquestrada com propagação para `api-work-order` e compensação via `DELETE` no Keycloak explicitamente mapeadas.
4. `mvn process-test-resources test-compile` finalizado com **BUILD SUCCESS**.
5. Homologação com veredicto **APROVADO** pelo `code-reviewer-sentinel-subagent`.
