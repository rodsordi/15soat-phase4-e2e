# Plano de Implementação: Aplicação das Melhores Práticas BDD no Ciclo de Vida da Ordem de Serviço

> **Decisão do Usuário**: Opção 1 - Best Practice & ADR Compliance Total (Voz Ativa, DataTable e Invariante de Máquina de Estados)
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

Este plano define a evolução da especificação `work_order_lifecycle.feature` e seus respectivos step definitions em `WorkOrderSteps.java` no módulo `15soat-phase4-e2e`, alinhando-os integralmente aos princípios de BDD e às diretrizes consolidadas nas ADRs do projeto:

1. **Voz Ativa com Papéis Explícitos do Domínio (ADR-003)**:
   - Eliminação de construções passivas nas Fases 3, 6 e 8.
   - Especificação clara dos atores responsáveis: `o mecânico`, `o atendente`, `o cliente`, `o serviço de faturamento`.
2. **Entrada Estruturada com Cucumber Data Tables (ADR-003)**:
   - Uso de `DataTable` na abertura da Ordem de Serviço (Fase 1) para passagem limpa dos atributos `customerDocument`, `licensePlate` e `description`.
3. **Proteção da Máquina de Estados e Integridade de Negócio (ADR-002)**:
   - Adição de cenário de exceção de domínio cobrindo a **tentativa de liberação de veículo sem pagamento confirmado**:
     - Quando o atendente tenta liberar (`DELIVERED`) uma Ordem de Serviço que ainda se encontra em `COMPLETED` (não quitada), o sistema deve rejeitar a transição com status HTTP 422 (Unprocessable Entity) ou 400 (Bad Request).
4. **Conformidade com Diretrizes Globais**:
   - Código Java, nomes de métodos, asserções e mensagens estritamente em **Inglês** (User Rule 2).
   - Textos de especificação Gherkin em português (`# language: pt`).
   - Nenhuma operação de commit ou push automática sem autorização explícita prévia (User Rule 1).

---

## 2. Etapas de Execução

1. **Atualizar `work_order_lifecycle.feature`**:
   - Refatorar a abertura da OS no Cenário Principal para utilizar `DataTable`.
   - Ajustar as frases das Fases 3, 6 e 8 para voz ativa com os papéis do domínio.
   - Adicionar o Cenário 2: `@excecao-liberacao-nao-quitada` validando a rejeição da liberação prematura do veículo.
2. **Atualizar `WorkOrderSteps.java`**:
   - Adicionar o método de step com suporte a `DataTable` para abertura da OS:
     `@Quando("o atendente abre uma nova Ordem de Serviço com os seguintes dados:")`
   - Adicionar as anotações e métodos para as frases em voz ativa:
     - `@Quando("o mecânico conclui o diagnóstico e o faturamento gera o orçamento com valor de {double}")`
     - `@Quando("o mecânico conclui todos os reparos técnicos na oficina")`
     - `@Quando("o atendente libera o veículo quitado para o cliente")`
   - Adicionar o step para tentativa de liberação do veículo não quitado:
     - `@Quando("o atendente tenta liberar o veículo para o cliente sem a confirmação do pagamento")`
3. **Validação da Suíte de Testes**:
   - Executar `mvn test-compile` em `15soat-phase4-e2e` para certificar ausência de erros de compilação ou inconsistências de sintaxe Cucumber.
