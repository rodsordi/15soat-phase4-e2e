# Plano de Implementação: Expansão de Cenários BDD e Diagramas de Sequência para Execução da Oficina

> **Decisão do Usuário**: Opção 1 - Recomendada (Alinhada às ADRs Canônicas do Projeto)
> **Status**: Aprovado para Execução

---

## 1. Visão Geral do Escopo
Expandir a suíte de testes BDD de execução da oficina (`api-exec`) no projeto `15soat-phase4-e2e`, alinhando os arquivos `.feature` com as regras de negócio de domínio, os contratos RESTful e os diagramas de sequência arquiteturais em Mermaid.

---

## 2. Itens a Serem Implementados

### 2.1. Arquivo `execution_queue_and_repair.feature`
Adicionar três novos cenários de teste:
1. `@fila-operacional`: Consulta e listagem paginada da fila de execução ativa (`GET /api/v1/executions/queue`).
2. `@falha-tecnica`: Registro de interrupção técnica no reparo com status `FAILED` e justificativa técnica (`notes`).
3. `@imutabilidade-finalizacao`: Cenário negativo de bloqueio de alteração em ordem já finalizada (`HTTP 400 Bad Request` conforme RFC 7807 e regra `EXECUTION_ALREADY_FINALIZED`).

### 2.2. Arquivo `materials_and_checklist.feature`
Adicionar um novo cenário de teste:
1. `@consulta-consolidada`: Consulta detalhada da ordem de execução por ID (`GET /api/v1/executions/{id}`), validando o agregado documental persistido com checklist e materiais.

### 2.3. Step Definitions em `ExecutionSteps.java`
Implementar os novos steps em inglês com asserções estritas (AssertJ) e integração com `ScenarioContext`:
* `supervisorQueriesExecutionQueue()`
* `queueShouldListExecutionOrder()`
* `mechanicRecordsTechnicalFailure(String status, String notes)`
* `mechanicAttemptsToUpdateStatus(String status)`
* `systemRejectsWithBusinessRuleViolation(int expectedStatusCode)`
* `mechanicQueriesConsolidatedExecutionDetails()`
* `consolidatedDataShouldContainMaterialsAndChecklist()`

### 2.4. Atualização dos Diagramas de Sequência (`.md`)
1. **`execution_queue_and_repair.md`**: Atualizar o diagrama Mermaid com a consulta de fila (`GET /queue`), o fluxo alternativo de falha (`FAILED`) e a validação de bloqueio pós-finalização (`400 Bad Request`).
2. **`materials_and_checklist.md`**: Atualizar o diagrama Mermaid incluindo a consulta consolidada do agregado por identificador (`GET /executions/{id}`).

---

## 3. Critérios de Aceite e Validação
1. Suíte de testes Cucumber executada com Maven (`mvn test -Dcucumber.filter.tags=@oficina`).
2. 100% dos cenários (novos e existentes) com veredicto verde.
3. Código de produção e testes estritamente em inglês.
4. Diagramas Mermaid com sintaxe válida e visualização limpa.
