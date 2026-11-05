# Plano de Implementação: Saneamento de Contexto e Complementação de Regras Críticas no Ciclo de Vida da Ordem de Serviço

> **Decisão do Usuário**: Opção 1 - Saneamento de Contexto & Complementação de Regras Críticas da Máquina de Estados
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

O arquivo de especificação BDD `work_order_lifecycle.feature` é a documentação viva central do microsserviço de Ordem de Serviço. A presente evolução visa:
1. **Eliminar poluição de fixtures no `Contexto:`**: Remover os passos de cadastro de catálogo de serviços e catálogo de materiais que não são consumidos pelos cenários de ciclo de vida (eliminando overhead de rede e detalhes incidentais).
2. **Harmonizar o Cenário 1 (E2E Happy Path)**: Uniformizar o passo de abertura de OS para a linguagem declarativa consistente.
3. **Formalizar Regras de Negócio Críticas Faltantes**:
   - **Regra de Inviolabilidade de Orçamento**: O cliente não pode aprovar uma Ordem de Serviço antes da formulação e cálculo do orçamento (`WAITING_APPROVAL`).
   - **Regra de Inviolabilidade de OS Finalizada/Quitada**: O cliente não pode cancelar uma Ordem de Serviço após confirmação de pagamento (`PAID`) ou entrega (`DELIVERED`).

---

## 2. Escopo das Alterações

### 2.1 Repositório `15soat-phase4-e2e`
- **Arquivo**: `src/test/resources/features/work-order/work_order_lifecycle.feature`
  - Limpar `Contexto:` mantendo apenas a verificação de saúde do serviço, as autenticações dos perfis (`EMPLOYEE` e `CUSTOMER`) e o registro base do cliente/veículo.
  - No Cenário 1 (`@happy-path @ciclo-completo`), uniformizar a asserção declarativa de criação de OS.
  - Adicionar o cenário de exceção `@excecao-aprovacao-sem-orcamento` sob uma regra explícita de autoridade orçamentária.
  - Adicionar o cenário de exceção `@excecao-cancelamento-pos-quitacao` sob uma regra explícita de cancelamento de OS.
- **Arquivo**: `src/test/java/br/com/fiap/phase4/e2e/steps/WorkOrderSteps.java`
  - Implementar os passos necessários para submeter aprovação antecipada e cancelamento após quitação, mapeando as asserções de erro 422 (`INVALID_STATUS_TRANSITION`).

### 2.2 Repositório `15soat-phase4-api-work-order`
- **Arquivo**: `domain/src/main/java/br/com/fiap/phase4/workorder/domain/model/WorkOrder.java`
  - Reforçar a guarda do método `approve()` para exigir explicitamente o status `WAITING_APPROVAL`:
    ```java
    if (this.status != WorkOrderStatus.WAITING_APPROVAL) {
        throw new DomainException("INVALID_STATUS_TRANSITION",
                "Cannot approve work order before budget is generated. Current status: " + this.status);
    }
    ```
- **Arquivo**: `domain/src/test/java/br/com/fiap/phase4/workorder/domain/WorkOrderDomainTest.java`
  - Atualizar os testes unitários do domínio para refletir a transição legítima antes da aprovação (`RECEIVED` -> `DIAGNOSING` -> `WAITING_APPROVAL` -> `APPROVED`).
  - Adicionar testes unitários específicos para validar as novas restrições de transição de estado.

---

## 3. Plano de Execução Sequencial

1. **Domínio e Testes Unitários (`15soat-phase4-api-work-order`)**:
   - Atualizar a guarda de transição `approve()` em `WorkOrder.java`.
   - Atualizar e complementar `WorkOrderDomainTest.java`.
   - Executar `mvn test` no projeto de domínio e validar 100% verde.
2. **Especificação BDD e Step Definitions (`15soat-phase4-e2e`)**:
   - Editar `work_order_lifecycle.feature` com as melhorias acordadas.
   - Atualizar `WorkOrderSteps.java` para suportar os novos steps.
   - Executar dry-run e compilação do Cucumber (`mvn test-compile` e `mvn test "-Dtest=CucumberTestRunner" "-Dcucumber.execution.dry-run=true"`).
3. **Validação do Quality Gate & Padrão Maker-Checker**:
   - Assegurar zero regressão em ambas as bases de código.
