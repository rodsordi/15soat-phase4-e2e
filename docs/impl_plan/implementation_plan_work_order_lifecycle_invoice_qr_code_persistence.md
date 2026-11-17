# Plano de Implementação: Persistência dos Dados do QR Code na Fatura Após Retorno do Mercado Pago

> **Decisão do Usuário**: Opção 1 - Adicionar Etapa de Atualização da Fatura no Banco de Dados com os Dados do QR Code PIX
> **Status**: Aprovado para Execução

---

## 1. Contexto e Motivação

No diagrama de sequência do ciclo de vida da Ordem de Serviço ([`work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md)), na Seção 6 (Conclusão e Faturamento), após o Mercado Pago responder à chamada de criação de checkout PIX no passo 64 (`MP-->>Billing: Retorna QR Code PIX e Ticket URL`), a `api-billing` publicava imediatamente o evento `InvoiceIssuedEvent` no Apache Kafka sem explicitar a etapa de persistência dos dados retornados.

Para assegurar a integridade transacional, rastreabilidade e consistência dos registros financeiros em repouso (*Database Persistence / ACID*):
- Imediatamente após receber a resposta do gateway Mercado Pago contendo o payload do QR Code PIX e o link seguro de liquidação (Ticket URL), a `api-billing` deve atualizar a entidade de fatura (`invoice`) no PostgreSQL.
- Essa etapa garante que a fatura detenha o identificador externo da transação (`externalPaymentId`) e os dados para geração do PIX antes de emitir a notificação de evento para a `api-work-order`.

---

## 2. Escopo das Alterações

1. **Atualização da Seção 6 do Diagrama Mermaid em `work_order_lifecycle.md`**:
   - Inserir a etapa de processamento interno da `api-billing` logo após o passo 64:
     ```mermaid
     MP-->>Billing: Retorna QR Code PIX e Ticket URL
     Billing->>Billing: Atualiza fatura no PostgreSQL<br/>com dados do QR Code PIX e Ticket URL
     Billing->>Kafka: Publica InvoiceIssuedEvent<br/>(Tópico: billing-events)<br/>{ workOrderId, pixQrCode, ticketUrl }
     ```
   - Preservar o balanceamento rigoroso de activation boxes e as diretrizes da ADR-004.

2. **Validação**:
   - Sincronização de recursos via Maven (`process-test-resources`).
   - Compilação dos testes (`test-compile`).
   - Avaliação e parecer do Auditor Sentinela Independente (Maker-Checker).

---

## 3. Roteiro de Execução

1. Criar este plano de implementação.
2. Aplicar a edição no arquivo [`15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md).
3. Executar o Maven para validar a compilação e integridade dos testes.
4. Acionar o auditor `code-reviewer-sentinel-subagent`.
5. Apresentar os resultados ao usuário e solicitar autorização antes de qualquer commit/push.
