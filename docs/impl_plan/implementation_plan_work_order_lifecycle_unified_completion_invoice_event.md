# Plano de Implementação: Coreografia Assíncrona com E-mail Único Consolidado via `InvoiceIssuedEvent`

> **Decisão do Usuário**: Opção 1 - Coreografia Assíncrona com E-mail Único Consolidado via `InvoiceIssuedEvent`
> **Status**: Aprovado para Execução

---

## 1. Contexto e Motivação

Na conclusão dos serviços técnicos (Fase 6 do ciclo de vida da OS), a abordagem anterior realizava dois disparos de e-mail ao cliente quase simultâneos:
1. Um e-mail da `api-work-order` informando que o veículo estava finalizado (`COMPLETED`).
2. Um e-mail subsequente da `api-billing` enviando o QR Code PIX para pagamento.

Além da experiência do usuário (UX) fragmentada, a OS passava para o status `COMPLETED` antes de a fatura/título financeiro existir de fato no Mercado Pago.

Para solucionar essa questão sem introduzir acoplamento temporal síncrono (HTTP REST) que degradaria a disponibilidade composta e colocaria em risco as threads consumidoras do Kafka (`max.poll.interval.ms`), a solução arquitetural canônica adota a **Coreografia Reativa com Event-Carried State Transfer**:
1. Ao consumir `ExecutionCompletedEvent` da `api-exec`, a `api-work-order` baixa os materiais e emite o evento `WorkOrderCompletedEvent` (com os dados de cobrança) no tópico `work-order-events`. **Nenhum e-mail prematuro é enviado ao cliente.**
2. A `api-billing` consome `WorkOrderCompletedEvent`, registra a fatura, gera a ordem de pagamento/PIX no Mercado Pago e publica o evento canônico `InvoiceIssuedEvent` no tópico `billing-events` contendo `{ workOrderId, invoiceId, pixQrCode, ticketUrl }`.
3. A `api-work-order` consome `InvoiceIssuedEvent`, atualiza formalmente o status da OS para `COMPLETED` e **dispara um e-mail único e consolidado ao cliente** informando que o veículo está pronto para retirada com o link e QR Code PIX para quitação imediata.

---

## 2. Escopo das Alterações

1. **Atualização da Seção 6 do Diagrama Mermaid em `work_order_lifecycle.md`**:
   - `WO` consome `ExecutionCompletedEvent`, efetua a baixa de materiais do estoque e publica `WorkOrderCompletedEvent` no tópico `work-order-events`.
   - `Billing` consome `WorkOrderCompletedEvent`, emite a fatura com o Mercado Pago e publica `InvoiceIssuedEvent` no tópico `billing-events`.
   - `WO` consome `InvoiceIssuedEvent`, atualiza o status para `COMPLETED` e dispara o e-mail único consolidado ao cliente.
   - Paridade estrita das caixas de ativação (`+WO`, `deactivate WO`, `+Billing`, `deactivate Billing`, `+WO`, `deactivate WO`).

2. **Harmonização da Narrativa em `work_order_lifecycle.md`**:
   - Atualizar a descrição dos papéis do Cliente (item 1) e da API Billing (item 4) para refletir o e-mail único consolidado e o evento `InvoiceIssuedEvent`.

3. **Validação**:
   - Sincronização de recursos via Maven (`process-test-resources`).
   - Compilação dos testes (`test-compile`).
   - Avaliação e parecer do Auditor Sentinela Independente (Maker-Checker).

---

## 3. Roteiro de Execução

1. Criar este plano de implementação.
2. Aplicar a edição no arquivo [`15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md).
3. Executar o Maven para sincronizar recursos e validar compilação.
4. Acionar o auditor `code-reviewer-sentinel-subagent`.
5. Apresentar os resultados ao usuário e solicitar autorização antes de qualquer commit/push.
