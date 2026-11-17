# Plano de Implementação: Atualização do Fluxo de Conclusão do Diagnóstico via Kafka na Documentação Viva

> **Decisão do Usuário**: Opção 1 - Coreografia via Evento Especializado `DiagnosisCompletedEvent`
> **Status**: Aprovado para Execução

---

## 1. Contexto e Motivação

No diagrama de sequência do ciclo de vida da Ordem de Serviço ([`work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md)), a Seção 3 apresentava uma chamada direta do Mecânico para a API Work Order (`Mec -> WO: PATCH /api/v1/work-orders/{id} { status: "WAITING_APPROVAL" }`).

Sob a ótica de Domain-Driven Design (DDD), Bounded Contexts e Princípio do Menor Privilégio (PoLP):
- O Mecânico opera exclusivamente no contexto de execução técnica da oficina (`api-exec`).
- O Mecânico não possui privilégios de acesso direto para alteração de status no contexto comercial de ordens de serviço (`api-work-order`).
- Ao concluir o diagnóstico técnico e o preenchimento de checklists no MongoDB através da `api-exec`, esta publica de forma assíncrona o evento de domínio `DiagnosisCompletedEvent` no tópico `execution-events` do Apache Kafka.
- A `api-work-order` consome este evento, calcula o valor orçado total (`totalAmount`) com base no catálogo de serviços e estoque de materiais, transiciona o status da OS para `WAITING_APPROVAL` e notifica o cliente por e-mail com o link de decisão soberana.

---

## 2. Escopo das Alterações

1. **Atualização da Seção 3 do Diagrama Mermaid em `work_order_lifecycle.md`**:
   - Ajustar o passo em que o mecânico registra a conclusão do diagnóstico na `api-exec`.
   - Adicionar a publicação do evento `DiagnosisCompletedEvent` da `Exec` para o `Kafka` (`Tópico: execution-events`).
   - Retornar `200 OK (ExecutionDetailedResponse)` para o `Mec`.
   - Adicionar o consumo do evento pelo `WO` (`Kafka -->> +WO: Consome DiagnosisCompletedEvent`).
   - Manter as ações autônomas da `WO` (cálculo de `totalAmount`, transição de status para `WAITING_APPROVAL` e envio de e-mail ao Cliente).
   - Fechar a ativação de thread (`deactivate WO`).

2. **Validação**:
   - Garantir a integridade da sintaxe Mermaid do diagrama.
   - Executar os testes automatizados do módulo `15soat-phase4-e2e`.

---

## 3. Roteiro de Execução

1. Aplicar a edição em [`15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md).
2. Sincronizar em [`15soat-phase4-e2e/target/test-classes/features/work-order/work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/target/test-classes/features/work-order/work_order_lifecycle.md) se aplicável.
3. Executar o Quality Gate (`mvn test-compile` e testes relevantes).
4. Submeter ao auditor de qualidade independente (Maker-Checker).
5. Apresentar o resultado ao usuário e solicitar autorização antes de qualquer commit/push (Diretriz Global #1).
