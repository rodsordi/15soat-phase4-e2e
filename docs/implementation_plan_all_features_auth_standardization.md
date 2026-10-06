# Plano de Implementação: Padronização Completa de Autenticação RBAC e Contratos BDD em Todos os Módulos

> **Decisão do Usuário**: Opção 1 - Atualização Completa e Padronizada em Todos os Módulos
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivo
Propagar sistematicamente para todos os arquivos `.feature` do repositório `15soat-phase4-e2e` as boas práticas consolidadas na `ADR-003`, `ADR-005` e em `customer_vehicle_management.feature`:
1. **Precondição de Autenticação RBAC no Contexto**:
   - `work-order/*`: Atendente/Administrador autenticado (`EMPLOYEE` / `ADMIN`).
   - `exec/*`: Mecânico responsável autenticado (`EMPLOYEE`).
   - `billing/*`: Analista de faturamento autenticado (`EMPLOYEE`).
   - `saga/*`: Operação orquestrada por atendente autenticado.
2. **Abertura de OS Assistida pelo Atendente**:
   - Substituição de "o cliente abre a OS" por "o atendente abre a Ordem de Serviço para o cliente" na recepção da oficina.
3. **Compatibilidade dos Step Definitions em Java**:
   - Atualizar `WorkOrderSteps.java`, `ExecutionSteps.java` e `BillingSteps.java` garantindo compilação limpa com `mvn test-compile`.

---

## 2. Arquivos a Serem Modificados

### 2.1. Features
1. `src/test/resources/features/work-order/work_order_lifecycle.feature`
2. `src/test/resources/features/work-order/material_management.feature`
3. `src/test/resources/features/work-order/service_catalog_management.feature`
4. `src/test/resources/features/exec/execution_queue_and_repair.feature`
5. `src/test/resources/features/exec/materials_and_checklist.feature`
6. `src/test/resources/features/billing/invoice_generation.feature`
7. `src/test/resources/features/saga/compensating_saga_rollback.feature`
8. `src/test/resources/features/saga/complete_choreographed_saga.feature`

### 2.2. Step Definitions (Java)
1. `src/test/java/br/com/fiap/phase4/e2e/steps/WorkOrderSteps.java`
2. `src/test/java/br/com/fiap/phase4/e2e/steps/ExecutionSteps.java`
3. `src/test/java/br/com/fiap/phase4/e2e/steps/BillingSteps.java`

---

## 3. Validação
- Compilar o projeto com `mvn test-compile`.
- Validar `git status -s`.
- Consultar o usuário para autorização de commit e push conforme a Diretriz Global 1.
