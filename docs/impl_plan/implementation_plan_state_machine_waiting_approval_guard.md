# Plano de Implementação: Blindagem do Motor de Estados e Validação de WAITING_APPROVAL

> **Decisão do Usuário**: Opção 1 - Blindagem Bidirecional (Matriz Formal de Estados na API + Cobertura BDD Sad-Path de Transição Ilegal)
> **Status**: Aprovado para Execução

## 1. Contexto & Objetivos
Garantir a integridade da máquina de estados do ciclo de vida da Ordem de Serviço na Fase 4:
- Na **API Work Order (`15soat-phase4-api-work-order`)**: Implementar a matriz formal de transições de estados no modelo de domínio (`WorkOrder.java` / `UpdateWorkOrderStatusUseCaseImpl.java`), impedindo que transições ilegais (como pular de `WAITING_APPROVAL` direto para `EXECUTING`, ou de `RECEIVED` direto para `WAITING_APPROVAL`/`COMPLETED`) ocorram sem validação de domínio, lançando `DomainException("INVALID_STATUS_TRANSITION", ...)`.
- No **BDD E2E (`15soat-phase4-e2e`)**:
  - Manter o step de preparação sequencial `Dado uma Ordem de Serviço com diagnóstico concluído aguardando aprovação do cliente` que respeita a máquina de estados (`RECEIVED` -> `DIAGNOSING` -> `WAITING_APPROVAL`).
  - Adicionar um cenário de Sad-Path na `Regra: O cliente possui autoridade soberana para decidir sobre a aprovação ou recusa do orçamento` em `work_order_lifecycle.feature` demonstrando a rejeição (`HTTP 422 INVALID_STATUS_TRANSITION`) ao tentar iniciar reparos sem que o orçamento tenha sido aprovado pelo cliente.

## 2. Detalhamento Técnico das Modificações

### 2.1. `15soat-phase4-api-work-order`
1. Em `WorkOrder.java`:
   - Definir a matriz de transições permitidas:
     - `RECEIVED` -> `DIAGNOSING`, `CANCELED`
     - `DIAGNOSING` -> `WAITING_APPROVAL`, `CANCELED`
     - `WAITING_APPROVAL` -> `APPROVED`, `CANCELED`
     - `APPROVED` -> `EXECUTING`, `CANCELED`
     - `EXECUTING` -> `COMPLETED`, `CANCELED`
     - `COMPLETED` -> `DELIVERED`, `PAID` (se liquidado)
     - `PAID` -> `DELIVERED`
     - `CANCELED` / `DELIVERED` -> Estados terminais (sem transições)
   - Adicionar método de validação de transição formal `validateTransition(WorkOrderStatus newStatus)` que dispara `DomainException("INVALID_STATUS_TRANSITION", "...")` caso a transição seja inválida.
   - Atualizar `transitionTo(...)` para acionar a validação antes de alterar o estado.
2. Executar suíte de testes unitários da API para certificar que os testes existentes permanecem 100% verdes e adicionar testes para a matriz de transição.

### 2.2. `15soat-phase4-e2e`
1. Em `work_order_lifecycle.feature`:
   - Sob a `Regra: O cliente possui autoridade soberana para decidir sobre a aprovação ou recusa do orçamento`, adicionar o cenário:
     ```gherkin
     @sad-path @excecao-execucao-sem-aprovacao
     Cenário: Rejeição do início de execução de reparos sem aprovação prévia do cliente
       Dado uma Ordem de Serviço com diagnóstico concluído aguardando aprovação do cliente
       Quando o mecânico tenta iniciar os reparos sem a aprovação do orçamento
       Então o erro retornado deve corresponder a:
         | status    | 422                                                         |
         | errorCode | INVALID_STATUS_TRANSITION                                   |
         | title     | Business Rule Violation                                     |
         | detail    | Cannot execute work order before budget is approved         |
     ```
2. Em `WorkOrderSteps.java`:
   - Implementar o step correspondente:
     ```java
     @Quando("o mecânico tenta iniciar os reparos sem a aprovação do orçamento")
     public void mechanicTriesToStartRepairsWithoutApproval() {
         updateWorkOrderStatus("EXECUTING");
     }
     ```
3. Validar a execução com `mvn test-compile` e `CucumberTestRunner` (dry-run).
