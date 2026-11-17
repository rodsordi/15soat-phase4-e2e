# Plano de Implementação: Ciclo Completo de Reserva Preventiva de Materiais no Diagrama de Sequência

> **Decisão do Usuário**: Opção 1 - Ciclo Completo de Reserva Preventiva de Materiais no Diagrama de Sequência
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

No diagrama de sequência do ciclo de vida da Ordem de Serviço ([`work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md)), a reserva de materiais ocorria tardiamente no momento da aprovação do orçamento pelo cliente (Fase 4a, passo 29).

Essa dinâmica introduzia o risco de concorrência e indisponibilidade de peças (*Stockout Race Condition*). Para garantir a integridade do inventário e refletir com precisão os métodos de domínio da entidade [`Material.java`](file:///c:/git/fiap/phase4/15soat-phase4-api-work-order/domain/src/main/java/br/com/fiap/phase4/workorder/domain/model/Material.java):
1. **Reserva Preventiva**: A reserva dos materiais deve ser realizada logo na abertura e criação da OS (`reserveQuantity`), retendo o estoque de insumos e peças na recepção do veículo.
2. **Aprovação**: Na aprovação do orçamento pelo cliente (Fase 4a), a OS transiciona para `APPROVED`, mantendo a reserva previamente assegurada.
3. **Liberação / Estorno por Cancelamento**: Caso o cliente rejeite o orçamento ou a OS seja cancelada (Fase 4b), a reserva de materiais é formalmente estornada e devolvida ao estoque disponível (`releaseReservedQuantity`).
4. **Baixa Definitiva**: Ao concluir os reparos técnicos na oficina (Fase 6), a OS transiciona para `COMPLETED` e os materiais reservados são baixados em definitivo do estoque físico (`concludeReservedQuantity`).

---

## 2. Escopo das Alterações

1. **Atualização do Diagrama Mermaid em `work_order_lifecycle.md`**:
   - **Fase 1 (Criação da OS)**: Atualizar a nota de processamento interno da API Work Order para registrar a reserva de materiais no estoque.
   - **Fase 4a (Aprovação)**: Atualizar o passo interno da API Work Order para validar a autorização e confirmar a aprovação com materiais já reservados.
   - **Fase 4b (Rejeição / Cancelamento)**: Atualizar o passo interno da API Work Order para estornar e liberar a reserva de materiais de volta ao estoque disponível.
   - **Fase 6 (Conclusão dos Serviços)**: Garantir clareza da baixa definitiva dos materiais consumidos no estoque físico.

2. **Validação**:
   - Sincronização dos recursos de teste (`process-test-resources`).
   - Compilação do módulo `15soat-phase4-e2e`.
   - Submissão ao Auditor Sentinela Independente (Maker-Checker).

---

## 3. Roteiro de Execução

1. Criar este plano de implementação.
2. Aplicar a edição no arquivo [`15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md).
3. Executar o Maven para sincronizar os recursos de teste em `target/test-classes`.
4. Validar o Quality Gate.
5. Acionar o auditor `code-reviewer-sentinel-subagent`.
6. Apresentar o resultado ao usuário e solicitar confirmação prévia antes de qualquer commit/push.
