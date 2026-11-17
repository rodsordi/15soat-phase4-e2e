# Plano de Implementação: Hub Central de Navegação da Documentação Viva com Cross-References e Refinamentos

> **Decisão do Usuário**: Opção 1 - Hub Central de Navegação da Documentação Viva com Cross-References e Refinamentos
> **Status**: Aprovado para Execução

---

## 1. Contexto e Motivação

O diagrama de sequência do ciclo de vida da Ordem de Serviço ([`work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md)) orquestra de ponta a ponta todas as 8 fases do Tech Challenge da Fase 4.

Embora o fluxo principal esteja completo e funcionalmente validado, ele pode ser elevado ao padrão de **Hub Central de Navegação da Documentação Viva** (Living Documentation System), integrando e referenciando diretamente todos os subfluxos especializados da suíte:
1. **Fase 1 (Abertura da OS)**: Desambiguação dos pré-requisitos entre catálogo de serviços e catálogo de materiais/estoque.
2. **Fase 2 (Diagnóstico Técnico)**: Conexão direta com a fila de oficina (`../exec/execution_queue_and_repair.md`).
3. **Fase 5 (Execução dos Reparos)**: Conexão com o subfluxo técnico de checklists e consumo de materiais (`../exec/materials_and_checklist.md`).
4. **Fase 6 (Conclusão e Faturamento)**: Conexão com a geração de faturas e QR Code PIX (`../billing/invoice_generation.md`).
5. **Fase 7 (Liquidação e Webhook)**: Conexão com o subfluxo de conciliação e garantia de idempotência (`../billing/payment_webhook_processing.md`).

---

## 2. Escopo das Alterações

1. **Atualização do Bloco de Pré-requisitos e Notas Introdutórias**:
   - Enriquecer a caixa de dicas com os links de navegação cruzada para os subfluxos de execução e faturamento.
2. **Refinamento das Notas `Note over` no Diagrama de Sequência**:
   - **Fase 1**: Ajustar a rotulagem de `service_catalog_management.md` para "Catálogo de Serviços (Mão de Obra)" e `material_management.md` para "Catálogo e Estoque de Peças e Insumos".
   - **Fase 2**: Inserir nota de cross-reference para `../exec/execution_queue_and_repair.md`.
   - **Fase 5**: Inserir nota de cross-reference para `../exec/materials_and_checklist.md`.
   - **Fase 6**: Inserir nota de cross-reference para `../billing/invoice_generation.md` e harmonizar comentário de seção.
   - **Fase 7**: Inserir nota de cross-reference para `../billing/payment_webhook_processing.md` e explicitar a validação de idempotência no processamento do webhook.
3. **Preservação das Invariantes**:
   - Manter rigorosamente intacta a ordem dos participantes (`Cli`, `Mec`, `Ator`, `WO`, `Kafka`, `Exec`, `Billing`, `MP`), o balanceamento de activation boxes e a sintaxe Mermaid compatível com a ADR-004.

---

## 3. Roteiro de Execução

1. Criar este plano de implementação.
2. Aplicar as edições no arquivo [`15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md).
3. Executar o Maven (`process-test-resources` e `test-compile`).
4. Submeter a validação ao Auditor Sentinela Independente (Maker-Checker).
5. Apresentar os resultados ao usuário e solicitar confirmação prévia antes de qualquer commit/push.
