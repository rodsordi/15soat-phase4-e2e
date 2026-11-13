# Plano de Implementação: Refatoração do Step de Disponibilidade Operacional e Saneamento de Chamadas Redundantes

> **Decisão do Usuário**: Opção 1 - Health Check Real & Limpeza das Chamadas Espalhadas
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

O step `Dado que o serviço de Ordem de Serviço está em execução e operacional` vinculado ao método `theWorkOrderServiceIsAvailable()` em `WorkOrderSteps.java` apresentava duas falhas arquiteturais:
1. **Falso Positivo de Disponibilidade**: Apenas mutava a variável global `RestAssured.baseURI`, sem executar qualquer requisição HTTP ou verificação de integridade operacional contra o endpoint de saúde (`/actuator/health`).
2. **Boilerplate e Violação de DRY**: O método `theWorkOrderServiceIsAvailable()` vinha sendo invocado manualmente em 32 métodos diferentes de negócio dentro de `WorkOrderSteps.java`, apesar do helper `givenAuth()` já atribuir o `baseUri` de forma segura e contextual em cada requisição.

---

## 2. Escopo das Alterações

### 2.1 Repositório `15soat-phase4-e2e`
- **Arquivo**: `src/test/java/br/com/fiap/phase4/e2e/steps/WorkOrderSteps.java`
  - Refatorar `theWorkOrderServiceIsAvailable()` para realizar a checagem de integridade contra `/actuator/health` com timeout defensivo curto (2 segundos) e tratamento elegante para cenários offline/dry-run.
  - Eliminar todas as 32 chamadas defensivas internas a `theWorkOrderServiceIsAvailable()` dentro dos métodos de negócio (`attendantIsAuthenticatedWithRole`, `registerCustomer`, `registerMaterial`, `customerRequestsWorkOrder`, etc.).
  - Centralizar a configuração de baseUri no `givenAuth()` e no hook `@Before`, eliminando mutação de estado estático global.

---

## 3. Plano de Execução Sequencial

1. **Refatoração de `WorkOrderSteps.java`**:
   - Atualizar a implementação de `theWorkOrderServiceIsAvailable()`.
   - Limpar as 32 chamadas residuais redundantes no corpo dos métodos.
2. **Validação do Quality Gate**:
   - Compilar os testes: `mvn test-compile`.
   - Executar suíte Cucumber: `mvn test "-Dtest=CucumberTestRunner" "-Dcucumber.execution.dry-run=true"`.
   - Garantir 100% dos cenários (40/40) verdes.
