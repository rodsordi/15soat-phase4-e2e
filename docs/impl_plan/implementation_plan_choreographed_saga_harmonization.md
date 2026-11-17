# Plano de Implementação: Harmonização Arquitetural da Saga Distribuída Coreografada

> **Decisão do Usuário**: Opção 1 - Harmonização Arquitetural Completa da Saga com a Nova Topologia e Gestão de Estoque
> **Status**: Aprovado para Execução

---

## 1. Contexto e Motivação

O arquivo [`choreographed_saga.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/saga/choreographed_saga.md) consolida a especificação visual dos 4 cenários da **Saga Distribuída Coreografada** da Fase 4:
1. Caminho Feliz (Happy Path).
2. Rollback Compensatório por Recusa de Pagamento.
3. Rollback Compensatório com Estorno Financeiro por Falha na Oficina.
4. Idempotência do Estorno Compensatório perante Notificações Repetidas.

Após as consolidações realizadas no [`work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md), a especificação da Saga precisa ser harmonizada para assegurar coerência em toda a suíte:
- **Topologia Limpa dos Participantes**: Reordenar os participantes para que o `Kafka` atue como barramento central logo após a API Ordem de Serviço (`WO`), e a API Faturamento (`Billing`) fique à direita da API Execução (`Exec`), adjacente ao gateway externo `Mercado Pago (MP)`, eliminando linhas longas de cruzamento.
- **Consistência de Domínio de Estoque nos Rollbacks**: Explicitar que a transição para `CANCELED` em ações compensatórias (Cenários 2 e 3) efetua o estorno e liberação da reserva preventiva de materiais de volta ao estoque disponível (`releaseReservedQuantity`).
- **Rastreabilidade e Hiperconectividade**: Conectar os cenários da Saga ao Hub Operacional de [`work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md).
- **Padronização Visual ADR-004**: Tópicos Kafka declarados explicitamente, payloads e retornos com quebras verticais (`<br/>`) e paridade estrita de ativação de threads.

---

## 2. Escopo das Alterações

1. **Atualização dos 4 Cenários em `choreographed_saga.md`**:
   - **Cenário 1**: Reordenar participantes (`Atendente`, `Cliente`, `WO`, `Kafka`, `Exec`, `Billing`, `MP`), registrar reserva preventiva na criação e manter ativações balanceadas.
   - **Cenário 2**: Reordenar participantes (`Cliente`, `WO`, `Kafka`, `Billing`, `MP`), detalhar estorno de materiais no cancelamento da OS.
   - **Cenário 3**: Reordenar participantes (`Mecanico`, `WO`, `Kafka`, `Exec`, `Billing`, `MP`), detalhar estorno financeiro no Mercado Pago e estorno de materiais no cancelamento da OS.
   - **Cenário 4**: Reordenar participantes (`Kafka`, `WO`, `Billing`, `MP`), consolidando o comportamento defensivo e seletivo contra entregas repetidas (*At-Least-Once Delivery*).
2. **Harmonização Textual e Cross-References**:
   - Inserir links de cross-reference conectando a Saga ao ciclo completo de ordens de serviço.
3. **Validação**:
   - Sincronização via Maven (`process-test-resources`).
   - Compilação dos testes (`test-compile`).
   - Avaliação e parecer do Auditor Sentinela Independente (Maker-Checker).

---

## 3. Roteiro de Execução

1. Criar este plano de implementação.
2. Aplicar a harmonização completa no arquivo [`15soat-phase4-e2e/src/test/resources/features/saga/choreographed_saga.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/saga/choreographed_saga.md).
3. Executar o Maven para validar integridade sintática e compilação.
4. Acionar o auditor `code-reviewer-sentinel-subagent`.
5. Apresentar os resultados ao usuário e solicitar confirmação prévia antes de qualquer commit/push.
