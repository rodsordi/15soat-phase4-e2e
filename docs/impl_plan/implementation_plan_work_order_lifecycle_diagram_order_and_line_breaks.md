# Plano de Implementação: Reordenação dos Participantes e Otimização Visual de Quebras de Linha no Diagrama de Sequência

> **Decisão do Usuário**: Opção 1 - Reordenação Topológica (Kafka após Work Order e Billing à Direita de Exec) com Otimização de Quebras de Linha
> **Status**: Aprovado para Execução

---

## 1. Contexto e Motivação

No diagrama de sequência do ciclo de vida da Ordem de Serviço ([`work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md)), os participantes estavam declarados na seguinte sequência:
`Cli` ➔ `Mec` ➔ `Ator` ➔ `WO` ➔ `Billing` ➔ `Kafka` ➔ `Exec` ➔ `MP`.

Essa ordenação gerava cruzamentos visuais desnecessários de mensagens longas:
- A `api-billing` ficava posicionada à esquerda do `Kafka`, enquanto o gateway `Mercado Pago` ficava no extremo direito, forçando as setas de integração financeira a cruzarem toda a extensão do diagrama.
- O broker `Kafka` ficava intercalado entre `Billing` e `Exec`, distante da `api-work-order`.

Com a nova topologia:
1. **Posicionamento do Kafka**: Imediatamente após a `api-work-order` (`WO` ➔ `Kafka` ➔ `Exec`), refletindo fielmente o canal central de mensageria assíncrona entre o contexto comercial e o chão de oficina.
2. **Posicionamento do Billing**: À direita da `api-exec` e adjacente ao `Mercado Pago` (`Exec` ➔ `Billing` ➔ `MP`), tornando as interações de checkout PIX e webhooks imediatas e compactas.
3. **Quebras de Linhas (`<br/>`)**: Inclusão sistemática de quebras verticais nos títulos de seções `Note over`, payloads e descrições de processamento, garantindo ergonomia e legibilidade sem compressão de colunas em monitores ou IDEs.

---

## 2. Escopo das Alterações

1. **Reordenação dos Participantes**:
   - `actor Cli as Cliente<br/>(CUSTOMER)`
   - `actor Mec as Mecânico<br/>(Oficina)`
   - `actor Ator as Atendente<br/>(Recepção)`
   - `participant WO as API Work Order<br/>(PostgreSQL)`
   - `participant Kafka as Apache Kafka<br/>Broker`
   - `participant Exec as API Exec<br/>(MongoDB)`
   - `participant Billing as API Billing<br/>(PostgreSQL)`
   - `participant MP as Mercado Pago<br/>Gateway`
2. **Atualização da Nota de Borda Segura**:
   - `Note over Cli,Billing: 🔒 Borda Segura via Kong Gateway: Todas as requisições autenticadas utilizam Bearer JWT`
3. **Quebras de Linhas (`<br/>`) Sistemáticas**:
   - Títulos das 8 fases operacionais nos delimitadores `Note over`.
   - Mensagens de processamento interno e payloads de integração.
   - Especificação do estado transitório da etapa 59 (`WO->>WO: Transiciona status para COMPLETED,<br/>baixa materiais do estoque<br/>e aguarda fatura (invoiceStatus: PENDING)`).
4. **Validação**:
   - Sincronização via Maven (`process-test-resources`).
   - Compilação dos testes (`test-compile`).
   - Avaliação e parecer do Auditor Sentinela Independente (Maker-Checker).

---

## 3. Roteiro de Execução

1. Criar este plano de implementação.
2. Aplicar a reordenação e quebras de linha em [`15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.md).
3. Sincronizar recursos e compilar via Maven.
4. Acionar o auditor `code-reviewer-sentinel-subagent`.
5. Apresentar os resultados ao usuário e solicitar autorização antes de qualquer commit/push.
