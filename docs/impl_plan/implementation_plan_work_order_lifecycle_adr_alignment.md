# Plano de Implementação: Alinhamento Estrito com ADR-001, ADR-003 e Tagging Canônico

> **Decisão do Usuário**: Opção 1 - ADR-001 + ADR-003 + Tagging Canônico
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

Este plano formaliza o alinhamento de `work_order_lifecycle.feature` e `WorkOrderSteps.java` no projeto `15soat-phase4-e2e` às diretrizes canônicas das ADRs do repositório:

1. **Aderência Contratual à RFC 7807 (ADR-001)**:
   - Validação do fluxo de exceção (Sad Path) no Cenário 2 através de `DataTable` estrita com as propriedades canônicas: `status`, `errorCode`, `title` e `detail`.
2. **Simetria entre Criação e Consulta (ADR-003)**:
   - Padronização da consulta da Ordem de Serviço por ID na Fase 1 com `DataTable` declarativa em formato chave-valor (`Map<String, String>`).
3. **Classificação e Tagging Canônico Cucumber**:
   - Inclusão das tags semânticas `@happy-path` no Cenário 1 e `@sad-path` no Cenário 2 para viabilizar filtragem precisa nas pipelines de CI/CD.
4. **Conformidade com Diretrizes Globais**:
   - Todo código e comentários em Java estritamente em **Inglês** (User Rule 2).
   - Proibição absoluta de commit/push automáticos sem aprovação explícita (User Rule 1).

---

## 2. Etapas de Execução

1. **Atualizar `work_order_lifecycle.feature`**:
   - Adicionar `@happy-path` ao Cenário 1.
   - Refatorar o passo de consulta por ID na Fase 1 para usar `DataTable` com as propriedades `customerDocument`, `licensePlate` e `status`.
   - Adicionar `@sad-path` ao Cenário 2.
   - Substituir as assertivas isoladas de erro no Cenário 2 pela `DataTable` RFC 7807 (`status: 422`, `errorCode: INVALID_STATUS_TRANSITION`, `title: Business Rule Violation`, `detail: Work order cannot be delivered before payment is confirmed`).
2. **Atualizar `WorkOrderSteps.java`**:
   - Adicionar o método `@E("a consulta da Ordem de Serviço por ID deve retornar os seguintes dados:")` recebendo `Map<String, String> expectedFields` e asserindo cada propriedade na resposta.
3. **Validação Automatizada**:
   - Executar `mvn test-compile` e o runner `CucumberTestRunner` em modo dry-run para certificar 100% de paridade entre steps e métodos Java.
