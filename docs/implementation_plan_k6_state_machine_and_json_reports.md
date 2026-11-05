# Plano de Implementação: Alinhamento de Scripts k6 da Saga, Relatórios JSON de CI/CD e Limpeza de Dead Code

> **Decisão do Usuário**: Opção 1 - Alinhamento dos Scripts k6 da Saga + Métricas JSON de CI + Limpeza de Dead Code
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

Esta melhoria consolida a qualidade técnica e operacional do projeto `15soat-phase4-e2e`:
1. **Prevenção de Falhas em Testes de Estresse k6**: Adequar os scripts `complete_choreographed_saga.js` e `compensating_saga_rollback.js` à máquina de estados legítima do backend (`DIAGNOSING` e `WAITING_APPROVAL` antes de `APPROVED`), evitando respostas HTTP 422 (`INVALID_STATUS_TRANSITION`) sob carga.
2. **Emissão de Relatórios Machine-Readable para CI/CD**: Habilitar a geração do artefato padrão `json:target/cucumber-reports/cucumber.json` em `CucumberTestRunner.java`.
3. **Limpeza de Métodos Mortos (*Dead Code*)**: Remover o método `registerVehicle(plate, make, model, year, doc)` de 5 argumentos inline não utilizado em `CustomerVehicleSteps.java`.

---

## 2. Escopo das Alterações

### 2.1 Repositório `15soat-phase4-e2e`
- **Arquivo**: `src/test/resources/features/saga/complete_choreographed_saga.js`
  - Inserir as requisições `PATCH` com status `DIAGNOSING` e `WAITING_APPROVAL` antes da aprovação do orçamento (`APPROVED`).
- **Arquivo**: `src/test/resources/features/saga/compensating_saga_rollback.js`
  - Inserir as requisições `PATCH` com status `DIAGNOSING` e `WAITING_APPROVAL` antes da aprovação do orçamento (`APPROVED`).
- **Arquivo**: `src/test/java/br/com/fiap/phase4/e2e/CucumberTestRunner.java`
  - Adicionar o plugin `json:target/cucumber-reports/cucumber.json` aos parâmetros de configuração do runner.
- **Arquivo**: `src/test/java/br/com/fiap/phase4/e2e/steps/CustomerVehicleSteps.java`
  - Remover o método morto `registerVehicle(...)` de 5 argumentos inline.

---

## 3. Plano de Execução Sequencial

1. **Atualização dos Scripts k6 da Saga**:
   - Ajustar `complete_choreographed_saga.js`.
   - Ajustar `compensating_saga_rollback.js`.
2. **Atualização da Configuração do Runner**:
   - Atualizar `CucumberTestRunner.java` com o plugin JSON.
3. **Limpeza em `CustomerVehicleSteps.java`**:
   - Remover método não utilizado.
4. **Validação do Quality Gate**:
   - Compilar testes (`mvn test-compile`).
   - Executar suíte Cucumber dry-run (`mvn test "-Dtest=CucumberTestRunner" "-Dcucumber.execution.dry-run=true"`).
   - Validar a existência do arquivo `target/cucumber-reports/cucumber.json`.
   - Executar suíte de testes unitários em `15soat-phase4-api-work-order`.
