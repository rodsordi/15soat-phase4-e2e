# Plano de Implementação: Admin na Primeira Coluna e Provisionamento de Funcionário como Primeiro Fluxo no user_identity_management

> **Decisão do Usuário**: Opção 1 - Sequenciamento Cronológico Canônico de IAM
> **Status**: Aprovado para Execução

---

## 1. Contexto & Objetivos

Reordenar os participantes e fluxos operacionais em `features/auth-lambda/user_identity_management.md` para estabelecer a ordem cronológica canônica do ciclo de vida de identidades e acessos (IAM) na oficina mecânica:

1. **Posicionamento do Administrador / RH na Coluna 1**:
   - `actor Admin as Administrador /<br/>RH (ADMIN)` (Coluna 1 — extremo esquerdo)
   - `actor Ator as Atendente<br/>(Funcionário: EMPLOYEE)` (Coluna 2)
   - `participant Lambda as AWS Lambda<br/>(garage-auth-handler)` (Coluna 3 — Orquestradora da Saga)
   - `participant KC as Keycloak IdP<br/>(Realm: garage)` (Coluna 4)
   - `participant WO as API Work Order<br/>(Spring Boot 4 / PG)` (Coluna 5)
   - `participant DB as PostgreSQL<br/>(work_order_db)` (Coluna 6)

2. **Reordenação dos Cenários**:
   - **Cenário 1 (Primeiro Fluxo — Nascimento da Identidade Corporativa)**: O Administrador provisiona o funcionário/atendente via `POST /users` (role `EMPLOYEE`) estritamente no Keycloak (**IAM Corporativo Puro**, sem acionar a API Work Order).
   - **Cenário 2 (Operação de Recepção — Onboarding de Cliente)**: O Atendente, já provisionado, cadastra o cliente no balcão via `POST /users` (role `CUSTOMER`), executando a **Saga Orquestrada em 2 Etapas** (Keycloak + API Work Order no PostgreSQL).
   - **Cenário 3 (Falha na API Work Order — Saga Rollback Compensatório)**: Cenário de falha durante o onboarding de cliente disparando `DELETE /admin/realms/garage/users/{uuid}` no Keycloak e retornando 502 ao Atendente.
   - **Cenário 4 (Autenticação OIDC & Emissão de JWT)**: Login do Administrador (`role: ADMIN`) e login do Atendente (`role: EMPLOYEE`) via `POST /auth/login`.

3. **Conformidade ADR-004**:
   - Eliminação de cruzamento de setas (Admin na coluna 1 acionando a Lambda diretamente).
   - Largura mínima do container SVG mantida em `1400px`.
   - Paridade perfeita de activation boxes (`+` e `-`), sem caixas de ativação em atores humanos e quebras verticais `<br/>`.
   - Preservados o callout `[!NOTE]` e a nota perimetral `🔒 Sessão Autenticada via Token Authorizer (vide auth_token_authorizer.md)`.

---

## 2. Critérios de Aceite

1. `Admin` é o primeiro participante declarado (Coluna 1), seguido por `Ator` (Coluna 2).
2. O Cenário 1 é estritamente o provisionamento de funcionário pelo Administrador.
3. O Cenário 2 é o onboarding de cliente pelo Atendente (Saga Orquestrada).
4. O Cenário 3 é o rollback compensatório no Keycloak.
5. O Cenário 4 é a autenticação OIDC.
6. `mvn process-test-resources test-compile` finaliza com **BUILD SUCCESS**.
7. Parecer de homologação **APROVADO** pelo subagente `code-reviewer-sentinel-subagent`.
