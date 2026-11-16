# Plano de Implementação: Criação do Diagrama de Emissão de Tokens (auth_token_issuance.md) e Atualização das Referências de JWT

> **Decisão do Usuário**: Opção 1 - Criação de auth_token_issuance.md, Purificação de user_identity_management.md e Atualização Universal das Referências
> **Status**: Aprovado para Execução

---

## 🎯 Objetivo
Isolar a responsabilidade de autenticação e emissão de tokens JWT (OIDC / OAuth 2.0) em um artefato dedicado (`auth_token_issuance.md`), purificar o documento de gestão de identidades (`user_identity_management.md`) mantendo apenas o provisionamento de usuários e atualizar todas as notas perimetrais de segurança dos diagramas de sequência da suíte para apontarem para o novo artefato de emissão de tokens.

---

## 🏗️ Especificações Técnicas

### 1. Novo Diagrama `auth_token_issuance.md`
- **Localização**: `15soat-phase4-e2e/src/test/resources/features/auth-lambda/auth_token_issuance.md`
- **Padrão Visual**: ADR-004 (container responsivo, largura `1250px`, autonumber, quebras verticais com `<br/>`, sem threads de ativação em atores humanos).
- **Atores e Participantes**:
  - `actor Admin as Administrador /<br/>RH (ADMIN)`
  - `actor Ator as Atendente<br/>(Funcionário: EMPLOYEE)`
  - `actor Cliente as Cliente<br/>(CUSTOMER)`
  - `participant Lambda as Auth API / Gateway<br/>(Endpoint /auth/login)`
  - `participant KC as Keycloak (IdP / OIDC)<br/>(Realm: garage)`
- **Fluxos Modelados**:
  1. Autenticação de Administrador (`POST /auth/login` $\rightarrow$ Keycloak $\rightarrow$ `200 OK` com token `role: ADMIN`).
  2. Autenticação de Funcionário / Atendente (`POST /auth/login` $\rightarrow$ Keycloak $\rightarrow$ `200 OK` com token `role: EMPLOYEE`).
  3. Autenticação de Cliente (`POST /auth/login` $\rightarrow$ Keycloak $\rightarrow$ `200 OK` com token `role: CUSTOMER`).
  4. Falha de Autenticação por Credenciais Inválidas (`POST /auth/login` $\rightarrow$ Keycloak `401 Unauthorized` $\rightarrow$ `401 Unauthorized` com `ProblemDetail`).

### 2. Purificação de `user_identity_management.md`
- Remoção do fluxo 4 (login/autenticação), restringindo o documento ao provisionamento:
  1. Provisionamento de Funcionário pelo Administrador (IAM Puro no Keycloak).
  2. Cadastro de Cliente pelo Atendente (Saga Orquestrada Keycloak + Work Order).
  3. Rollback Compensatório em Falha de Cadastro do Cliente.
- Adição de callout `[!NOTE]` direcionando para `auth_token_issuance.md`.

### 3. Atualização Universal das Referências nos Demais Diagramas
- Atualização das notas de sequência `🔒` e callouts Markdown externos para apontar formalmente para a emissão em `auth_token_issuance.md` e autorização perimetral em `auth_token_authorizer.md`:
  - `billing/invoice_generation.md`
  - `billing/payment_webhook_processing.md`
  - `exec/execution_queue_and_repair.md`
  - `exec/materials_and_checklist.md`
  - `work-order/customer_vehicle_management.md`
  - `work-order/material_management.md`
  - `work-order/service_catalog_management.md`
  - `work-order/work_order_lifecycle.md`
  - `saga/choreographed_saga.md` (Cenários 1, 2 e 3)
  - `auth-lambda/auth_token_authorizer.md`

### 4. Atualização de Documentações Centrais
- `docs/adr/004-padrao-visual-diagramas-sequencia-mermaid.md`
- `README.md`

---

## 📋 Passos de Execução
1. Criar `auth_token_issuance.md` com os 4 fluxos e conformidade estrita com ADR-004.
2. Refatorar `user_identity_management.md` removendo o fluxo de login.
3. Atualizar as referências de token nos 10 diagramas restantes.
4. Atualizar ADR-004 e README.md.
5. Executar `mvn process-test-resources test-compile -f 15soat-phase4-e2e/pom.xml`.
6. Submeter ao duplo crivo do subagente sentinela.
7. Solicitar autorização do usuário para commit e push.
