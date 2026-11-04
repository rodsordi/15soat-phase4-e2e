# Plano de Implementação: Fluxo Alternativo de Rejeição do Orçamento pelo Cliente

> **Decisão do Usuário**: Opção 1 - Ciclo de Decisão do Cliente: Rejeição Soberana do Orçamento e Atualização no Diagrama de Sequência
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

Este plano define a formalização do fluxo de recusa de orçamento pelo cliente (`CUSTOMER`) no ciclo de vida da Ordem de Serviço:
1. **Diagrama de Sequência Canônico (`work_order_lifecycle.md`)**:
   - Modelar a bifurcação na Fase 4 usando o bloco `alt ... else ... end`:
     - **Ramo 4a (Aprovação)**: O cliente aprova o orçamento de R$ 450,00 (`APPROVED`), disparando a liberação na fila da oficina (`api-exec`).
     - **Ramo 4b (Rejeição Soberana)**: O cliente rejeita a proposta (`CANCELED`), cancelando a OS na `api-work-order`, publicando `WorkOrderCanceledEvent` via Apache Kafka, notificando o cliente por e-mail e desocupando a baia/fila física na `api-exec`.
2. **Especificação Gherkin BDD (`work_order_lifecycle.feature`)**:
   - Adicionar o cenário `@fluxo-alternativo @rejeicao-orcamento`:
     - Quando a OS está em `WAITING_APPROVAL` e o próprio cliente rejeita o orçamento, o status da OS deve ser atualizado para `CANCELED`.
3. **Step Definitions Java (`WorkOrderSteps.java`)**:
   - Adicionar o step `@Quando("o próprio cliente rejeita o orçamento da Ordem de Serviço")` mapeado para o método `customerRejectsBudget` atualizando o status para `CANCELED`.
4. **Conformidade com Diretrizes Globais**:
   - Manter código e assertivas estritamente em **Inglês** (User Rule 2).
   - Zero commits/pushes automáticos sem autorização expressa prévia (User Rule 1).

---

## 2. Etapas de Execução

1. Atualizar `work_order_lifecycle.md` com a narrativa e o bloco `alt` no diagrama Mermaid.
2. Atualizar `work_order_lifecycle.feature` com o Cenário 2 de fluxo alternativo de rejeição e renumerar o cenário de exceção para Cenário 3.
3. Adicionar o método `customerRejectsBudget` em `WorkOrderSteps.java`.
4. Executar `mvn test-compile` e o runner Cucumber dry-run para validar compilação e matching de todos os passos.
