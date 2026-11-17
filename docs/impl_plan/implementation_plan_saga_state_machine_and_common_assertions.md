# Plano de Implementação: Alinhamento da Máquina de Estados da Saga, Injeção Global de Contexto e Asserções Comuns

> **Decisão do Usuário**: Opção 1 - Correção Integral da Máquina de Estados na Saga + Unificação do `ScenarioContext` & `CommonAssertionSteps`
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

A auditoria completa da suíte E2E identificou três pontos críticos de melhoria arquitetural:
1. **Salto de Estado Inválido na Saga**: Os cenários de Saga pulavam diretamente de `RECEIVED` para `APPROVED`, violando as guardas de transição de domínio implementadas em `WorkOrder.java`.
2. **Duplicação e Acoplamento de Asserções HTTP de Erro**: O step universal `Então o erro retornado deve corresponder a:` residia dentro de `WorkOrderSteps.java`, criando acoplamento com testes de IAM, clientes, materiais e catálogo.
3. **Isolamento de Estado em Billing, Execução e Auth**: As classes `BillingSteps`, `ExecutionSteps`, `AuthLambdaSteps` e `E2ESagaSteps` continuavam com campos mutáveis isolados e métodos `@Before resetState()` manuais, gerando UUIDs aleatórios desconectados da Ordem de Serviço aberta.

---

## 2. Escopo das Alterações

### 2.1 Repositório `15soat-phase4-e2e`
- **Arquivo**: `src/test/java/br/com/fiap/phase4/e2e/context/ScenarioContext.java`
  - Enriquecer com `invoiceId`, `executionId`, `technicianId` e helpers de base URL (`givenBillingAuth()`, `givenExecAuth()`, `givenAuthLambda()`).
- **Novo Arquivo**: `src/test/java/br/com/fiap/phase4/e2e/steps/CommonAssertionSteps.java`
  - Centralizar asserções de erro RFC 7807 (`errorResponseShouldMatchTable`) e validações genéricas de status HTTP consumindo o `ScenarioContext`.
- **Arquivos**: `src/test/resources/features/saga/complete_choreographed_saga.feature` e `src/test/resources/features/saga/compensating_saga_rollback.feature`
  - Decompor a abertura e aprovação para incluir a sequência legítima do ciclo de vida: `RECEIVED` $\rightarrow$ `DIAGNOSING` $\rightarrow$ `WAITING_APPROVAL` $\rightarrow$ `APPROVED`.
- **Arquivos**: `src/test/java/br/com/fiap/phase4/e2e/steps/E2ESagaSteps.java`, `BillingSteps.java`, `ExecutionSteps.java`, `AuthLambdaSteps.java`, `WorkOrderSteps.java`
  - Injetar `ScenarioContext` via construtor com PicoContainer.
  - Eliminar os métodos manuais `@Before resetState()`.
  - Remover asserções genéricas duplicadas.
- **Arquivos k6**: Alinhar os scripts de carga da Saga (`complete_choreographed_saga.js`, `compensating_saga_rollback.js`) com a sequência legítima.

---

## 3. Plano de Execução Sequencial

1. **Expansão do `ScenarioContext.java`**.
2. **Criação de `CommonAssertionSteps.java` e desduplicação em `WorkOrderSteps` e `AuthLambdaSteps`**.
3. **Refatoração de `BillingSteps.java` e `ExecutionSteps.java` com `ScenarioContext`**.
4. **Refatoração de `E2ESagaSteps.java` e arquivos de feature da Saga**.
5. **Alinhamento dos scripts k6 da Saga**.
6. **Quality Gate & Verificação**:
   - `mvn test-compile`
   - `mvn test "-Dtest=CucumberTestRunner" "-Dcucumber.execution.dry-run=true"`
   - `mvn test` em `api-work-order`
