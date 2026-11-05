# Plano de Implementação: Decomposição de Steps Mágicos em Transições Explícitas de Estado

> **Decisão do Usuário**: Opção 1 - Decomposição Explícita da Jornada no Gherkin (Steps Componíveis e Rastreáveis em todo o ecossistema)
> **Status**: Aprovado para Execução

## 1. Contexto & Objetivos
Eliminar o anti-pattern de *Magic Steps* (passos monolíticos que ocultam transições procedurais da máquina de estados em chamadas Java) em toda a suíte E2E BDD do projeto `15soat-phase4-e2e`.
- Garantir que cada cenário de teste expresse a sequência real e rastreável de eventos e ações de negócio que levam uma entidade ao estado desejado.
- Reutilizar steps atômicos e canônicos já existentes nos próprios arquivos Gherkin, aumentando a reusabilidade e clareza da Living Documentation.

## 2. Diagnóstico & Escopo de Aplicação

### 2.1. `work_order_lifecycle.feature`
- **Cenário 2 (`@rejeicao-orcamento`)**:
  - Substituir o step mágico `Dado uma Ordem de Serviço com diagnóstico concluído aguardando aprovação do cliente` pela sequência obrigatória de estados da OS:
    ```gherkin
    Dado que o atendente abre uma nova Ordem de Serviço para o cliente "52998224725" e veículo "BRA2E19" com a descrição "Troca de óleo e revisão de freios"
    E o mecânico inicia o diagnóstico da Ordem de Serviço
    E o mecânico conclui o diagnóstico e o faturamento gera o orçamento
    ```
- **Cenário 3 (`@excecao-execucao-sem-aprovacao`)**:
  - Aplicar a mesma sequência explícita: abertura (`RECEIVED`) ➔ diagnóstico (`DIAGNOSING`) ➔ orçamento (`WAITING_APPROVAL`), antes da tentativa de execução.
- **Cenário 4 (`@excecao-liberacao-nao-quitada`)**:
  - Substituir o step mágico `Dado uma Ordem de Serviço com reparos concluídos aguardando quitação` pela cadeia canônica completa de reparos:
    ```gherkin
    Dado que o atendente abre uma nova Ordem de Serviço para o cliente "52998224725" e veículo "BRA2E19" com a descrição "Troca de óleo e revisão de freios"
    E o mecânico inicia o diagnóstico da Ordem de Serviço
    E o mecânico conclui o diagnóstico e o faturamento gera o orçamento
    E o próprio cliente aprova o orçamento da Ordem de Serviço
    E o mecânico inicia a execução dos reparos na oficina
    E o mecânico conclui todos os reparos técnicos na oficina
    ```

### 2.2. `billing/payment_webhook_processing.feature`
- **Contexto**:
  - Substituir o step opaco `E uma fatura gerada com status "PENDING"` pela ação explícita de emissão de fatura:
    ```gherkin
    E uma fatura é criada para a Ordem de Serviço com o valor 350.00 e cliente "52998224725"
    ```

### 2.3. `exec/materials_and_checklist.feature`
- **Contexto**:
  - Substituir o step opaco `E uma ordem de execução criada com identificador válido` pela ação explícita de enfileiramento na oficina:
    ```gherkin
    E uma ordem de execução é enfileirada para a Ordem de Serviço com o técnico "TECH-101"
    ```

### 2.4. Limpeza no Código Java (`WorkOrderSteps.java`, `BillingSteps.java`, `ExecutionSteps.java`)
- Remover os métodos procedurais legados de bypass/transição oculta (`transitionToStatus`, `existingWorkOrderWithStatus`, etc.), simplificando o código das classes de steps.

## 3. Validação
- Compilar o módulo com `mvn test-compile`.
- Executar a suíte completa no `CucumberTestRunner` em modo dry-run e certificar que todos os 38 cenários continuem passando 100% verdes.
