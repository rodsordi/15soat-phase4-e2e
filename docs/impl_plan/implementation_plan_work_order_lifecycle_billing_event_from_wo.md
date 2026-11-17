# Plano de Implementação: Faturamento Consumindo Dados de Cobrança da API Work Order

> **Decisão do Usuário**: Opção 1 - Coreografia com Event-Carried State Transfer a partir da API Work Order
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

No diagrama de sequência do ciclo de vida da Ordem de Serviço ([`work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md)), a Seção 6 indicava que a `api-billing` consumia o evento `ExecutionCompletedEvent` diretamente do tópico `execution-events` publicado pela `api-exec`.

Sob a ótica de Domain-Driven Design (DDD) e Bounded Contexts:
- A `api-exec` gerencia exclusivamente o fluxo de oficina mecânica no chão de fábrica (MongoDB, baias e checklists). O evento `ExecutionCompletedEvent` não contém dados financeiros, catálogo de peças ou valores orçados.
- A `api-work-order` é o Aggregate Root comercial e detém o catálogo de serviços, materiais, snapshots de preço e o valor total consolidado (`totalAmount`).
- Para que a `api-billing` possa emitir a fatura e solicitar o QR Code Pix no Mercado Pago sem violação de fronteiras de domínio nem chamadas síncronas, a `api-work-order` deve publicar um evento enriquecido (`WorkOrderCompletedEvent`) no tópico `work-order-events` contendo os dados completos da cobrança (`workOrderId`, `customerDocument`, `totalAmount`).
- A `api-billing` passará a consumir este evento do tópico `work-order-events`.

---

## 2. Escopo das Alterações

1. **Atualização da Seção 6 do Diagrama Mermaid em `work_order_lifecycle.md`**:
   - Manter a publicação de `ExecutionCompletedEvent` pela `api-exec` no tópico `execution-events`.
   - Na `api-work-order`: ao consumir `ExecutionCompletedEvent`, atualizar status para `COMPLETED`, efetuar a baixa de estoque e publicar `WorkOrderCompletedEvent` no tópico `work-order-events` com o payload de cobrança (`{ workOrderId, customerDocument, totalAmount }`).
   - Na `api-billing`: substituir o consumo de `ExecutionCompletedEvent` pelo consumo de `WorkOrderCompletedEvent` a partir do tópico `work-order-events`.
   - A `api-billing` registra a fatura com o `totalAmount` real e solicita o QR Code Pix ao Mercado Pago.

2. **Validação**:
   - Sincronização de recursos (`process-test-resources`).
   - Compilação dos testes (`test-compile`).
   - Submissão ao Auditor Sentinela Independente (Maker-Checker).

---

## 3. Roteiro de Execução

1. Criar este plano de implementação.
2. Aplicar a edição no arquivo [`15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md).
3. Sincronizar e compilar com Maven.
4. Acionar o auditor `code-reviewer-sentinel-subagent`.
5. Apresentar o resultado e solicitar autorização antes de qualquer commit/push.
