# Plano de Implementação: Refatoração dos Steps do Cucumber com Text Blocks e Remoção do Amount

> **Decisão do Usuário**: Opção 1 - Refatoração Completa & Padronização Global em Todas as Steps
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

Esta demanda padroniza a construção de payloads JSON em toda a suíte de testes BDD do módulo `15soat-phase4-e2e` e refatora o método `updateWorkOrderStatus`:

1. **Remoção Definitiva do Parâmetro `amount` em `updateWorkOrderStatus`**:
   - Simplificar a assinatura para `updateWorkOrderStatus(String newStatus)`.
   - Atualizar todas as chamadas internas (`DIAGNOSING`, `WAITING_APPROVAL`, `APPROVED`, `EXECUTING`, `COMPLETED`, `PAID`, `DELIVERED`, `CANCELED`) para passar apenas o status.
   - Enviar estritamente o payload `{ "status": "%s" }` sem campos monetários arbitrários.
2. **Substituição de `Map.of` por Java Text Blocks (`"""..."""`)**:
   - Substituir todas as construções de payload HTTP via `Map.of` por blocos de texto nativos do Java 17/21 (`"""..."""`) com formatação JSON explícita.
   - Utilizar `Locale.US` nas formatações de valores numéricos de ponto flutuante para assegurar separador decimal por ponto (`.`).
   - Aplicar a padronização em todas as classes de steps:
     - `WorkOrderSteps.java`
     - `BillingSteps.java`
     - `ExecutionSteps.java`
     - `E2ESagaSteps.java`
     - `AuthLambdaSteps.java`
3. **Conformidade com Diretrizes Globais**:
   - Todo o código e comentários em Java estritamente em **Inglês** (User Rule 2).
   - Nenhuma operação de commit ou push automática sem autorização expressa prévia (User Rule 1).

---

## 2. Etapas de Execução

1. **`WorkOrderSteps.java`**:
   - Simplificar `updateWorkOrderStatus(String newStatus)` e ajustar todas as chamadas.
   - Substituir os payloads de cadastro de cliente, veículo, catálogo de serviços, materiais, abertura de OS e atualização de preço por Text Blocks com 3 aspas.
2. **`BillingSteps.java`**:
   - Substituir os payloads de emissão de fatura e webhook Mercado Pago por Text Blocks.
3. **`ExecutionSteps.java`**:
   - Substituir os payloads de transição de status, técnico e checklist por Text Blocks.
4. **`E2ESagaSteps.java`**:
   - Substituir os payloads dos eventos da Saga por Text Blocks.
5. **`AuthLambdaSteps.java`**:
   - Substituir os payloads do authorizer Lambda por Text Blocks.
6. **Validação**:
   - Executar `mvn test-compile` e o runner `CucumberTestRunner` em modo dry-run para assegurar 100% de integridade e ausência de regressões.
