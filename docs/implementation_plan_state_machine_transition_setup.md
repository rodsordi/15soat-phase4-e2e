# Plano de Implementação: Orquestração Transicional Transparente da Máquina de Estados para Cenários de Sad Path

> **Decisão do Usuário**: Opção 1 - Orquestração Transicional Transparente no Step Definition (Black-Box e Respeito à State Machine)
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

O microsserviço de Ordens de Serviço implementa um motor de Máquina de Estados Finita (State Machine) com transições sequenciais estritas:
`RECEIVED` -> `DIAGNOSING` -> `WAITING_APPROVAL` -> `APPROVED` -> `EXECUTING` -> `COMPLETED` -> `PAID` -> `DELIVERED`.

Tentar forçar a transição direta de `RECEIVED` para `COMPLETED` gera uma violação da máquina de estados (`INVALID_STATUS_TRANSITION`). 

Este plano estabelece:
1. **Refatoração Declarativa no Gherkin (`work_order_lifecycle.feature`)**:
   - Ajustar o Cenário 3 (`@sad-path @excecao-liberacao-nao-quitada`) para expressar em alto nível de negócio que a ordem de serviço possui todos os reparos concluídos aguardando quitação:
     `Dado uma Ordem de Serviço com reparos concluídos aguardando quitação`
2. **Orquestração Transicional no Java (`WorkOrderSteps.java`)**:
   - Implementar o método helper `workOrderWithRepairsCompleted()` que orquestra a máquina de estados através de requisições HTTP públicas legítimas:
     1. Cria a OS (`RECEIVED`).
     2. Inicia o diagnóstico (`DIAGNOSING`).
     3. Conclui o diagnóstico com geração de orçamento (`WAITING_APPROVAL`).
     4. Aprova o orçamento (`APPROVED`).
     5. Inicia a execução dos reparos (`EXECUTING`).
     6. Conclui todos os reparos técnicos (`COMPLETED`).
   - Ajustar também o cenário de cancelamento de orçamento para transicionar legitimamente de `RECEIVED` para `DIAGNOSING` e `WAITING_APPROVAL` antes de rejeitar.
3. **Conformidade Global**:
   - Código Java, métodos e assertivas estritamente em **Inglês** (User Rule 2).
   - Zero operações de commit ou push automáticos sem autorização prévia expressa (User Rule 1).

---

## 2. Etapas de Execução

1. Atualizar o Cenário 3 de `work_order_lifecycle.feature` com o passo declarativo `Dado uma Ordem de Serviço com reparos concluídos aguardando quitação`.
2. Em `WorkOrderSteps.java`:
   - Implementar `workOrderWithRepairsCompleted()` com a cadeia de transições legítimas.
   - Atualizar `existingWorkOrderWithStatus(String status)` para avançar legitimamente pelas etapas necessárias da máquina de estados.
3. Executar `mvn test-compile` e o dry-run do `CucumberTestRunner` para validação global.
