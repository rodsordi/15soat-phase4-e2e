# Plano de Implementação: Refinamento Completo e Padronização dos Diagramas de Sequência

> **Decisão do Usuário**: Opção 1 - Pacote Completo de Refinamento UML, Padronização e Purificação
> **Status**: Aprovado para Execução

---

## 🎯 Objetivo
Elevar a qualidade técnica, semântica e visual dos diagramas de sequência Mermaid na suíte `15soat-phase4-e2e`, assegurando 100% de conformidade com a **ADR-004** e as diretrizes de modelagem UML de Martin Fowler.

---

## 🏗️ Especificações Técnicas

### 1. Ativação Estrita de Threads UML (MongoDB)
- **`src/test/resources/features/exec/execution_queue_and_repair.md`**:
  - Adicionar operadores `+` e `-` e setas de resposta (`Mongo-->>-Exec: Confirma...`) em todas as interações de persistência com o MongoDB (entrada na fila, busca de fila, alteração de status para IN_REPAIR, COMPLETED e FAILED).
- **`src/test/resources/features/exec/materials_and_checklist.md`**:
  - Adicionar operadores `+` e `-` e setas de resposta (`Exec->>+Mongo:...` / `Mongo-->>-Exec:...`) nas operações de insumos, checklist e consulta agregada.

### 2. Padronização de Nomenclatura e Aliases (DDD & Ubiquitous Language)
- **`src/test/resources/features/work-order/material_management.md`**:
  - Substituir `participant WorkOrder as Work Order Service<br/>(PostgreSQL)` por `participant WO as API Work Order<br/>(Spring Boot 4 / PG)`.
  - Atualizar todas as setas correspondentes de `WorkOrder` para `WO`.
  - Adicionar introdução estruturada e numerada (1. Cadastro de Material, 2. Consulta por SKU, 3. Atualização de Preço).
- **`src/test/resources/features/work-order/service_catalog_management.md`**:
  - Substituir `participant WorkOrder as Work Order Service<br/>(PostgreSQL)` por `participant WO as API Work Order<br/>(Spring Boot 4 / PG)`.
  - Atualizar todas as setas correspondentes de `WorkOrder` para `WO`.
  - Adicionar introdução estruturada e numerada (1. Cadastro de Serviço, 2. Consulta por Código).

### 3. Purificação de Resíduos Textuais do Kong API Gateway
- **`src/test/resources/features/work-order/customer_vehicle_management.md`**:
  - Ajustar linha 8 para referenciar a borda autenticada direta via Bearer JWT (Keycloak OIDC).
- **`src/test/resources/features/work-order/work_order_lifecycle.md`**:
  - Ajustar linhas 8 e 9 para remover a menção intermediária a "via Kong API Gateway", preservando estritamente `zoom: 1.5;`.
- **`src/test/resources/features/saga/choreographed_saga.md`**:
  - Ajustar linha 5 para refletir a borda segura Bearer JWT / Keycloak.

---

## 📋 Passos de Execução
1. Atualizar `execution_queue_and_repair.md` com ativações completas no MongoDB.
2. Atualizar `materials_and_checklist.md` com ativações completas no MongoDB.
3. Padronizar `material_management.md` e `service_catalog_management.md` com `WO as API Work Order<br/>(Spring Boot 4 / PG)` e introdução numerada.
4. Purificar menções textuais ao Kong em `customer_vehicle_management.md`, `work_order_lifecycle.md` e `choreographed_saga.md`, preservando `zoom: 1.5;`.
5. Validar compilação e integridade via `mvn clean process-test-resources test-compile`.
6. Submeter ao Code Reviewer Sentinel para auditoria autônoma.
7. Solicitar autorização explícita do usuário para commit e push.
