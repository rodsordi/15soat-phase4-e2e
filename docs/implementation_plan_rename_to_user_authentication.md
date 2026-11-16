# Plano de Implementação: Renomeação para user_authentication.md e Alinhamento a DDD

> **Decisão do Usuário**: Opção 1 - Renomeação para user_authentication.md
> **Status**: Aprovado para Execução

---

## 🎯 Objetivo
Renomear o arquivo `auth_token_issuance.md` para `user_authentication.md` na pasta `features/auth-lambda/`, estabelecendo uma simetria perfeita orientada a Domain-Driven Design (DDD) com `user_identity_management.md`, e atualizar de forma universal todas as referências cruzadas nos diagramas de sequência, ADR-004 e README.md.

---

## 🏗️ Especificações Técnicas

### 1. Par de Gestão de Usuários e Autenticação
- `user_identity_management.md` $\rightarrow$ Provisionamento, cadastro e gestão de identidades no IAM / Catálogo.
- `user_authentication.md` $\rightarrow$ Autenticação OIDC, login unificado (`POST /auth/login`) e emissão de tokens de acesso JWT segregados por perfis (`ADMIN`, `EMPLOYEE`, `CUSTOMER`).

### 2. Atualização Universal de Referências
- Substituir todas as ocorrências de `auth_token_issuance.md` por `user_authentication.md`:
  - `src/test/resources/features/auth-lambda/user_identity_management.md`
  - `src/test/resources/features/billing/invoice_generation.md`
  - `src/test/resources/features/exec/execution_queue_and_repair.md`
  - `src/test/resources/features/exec/materials_and_checklist.md`
  - `src/test/resources/features/work-order/customer_vehicle_management.md`
  - `src/test/resources/features/work-order/material_management.md`
  - `src/test/resources/features/work-order/service_catalog_management.md`
  - `src/test/resources/features/work-order/work_order_lifecycle.md`
  - `src/test/resources/features/saga/choreographed_saga.md`
  - `docs/adr/004-padrao-visual-diagramas-sequencia-mermaid.md`
  - `README.md`

---

## 📋 Passos de Execução
1. Renomear `auth_token_issuance.md` para `user_authentication.md`.
2. Atualizar o texto interno e links nos diagramas de sequência.
3. Atualizar ADR-004 e README.md.
4. Executar `mvn clean process-test-resources test-compile`.
5. Submeter ao auditor independente (Code Reviewer Sentinel).
6. Solicitar permissão do usuário para commit e push.
