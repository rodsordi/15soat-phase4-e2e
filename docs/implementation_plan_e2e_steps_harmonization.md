# Plano de Implementação: Harmonização Global dos Steps do Cucumber

> **Decisão do Usuário**: Opção 1 - RequestSpecification Isolada, givenAuth() Universal e Asserções Estritas
> **Status**: Aprovado para Execução

## 1. Contexto & Objetivos
Harmonizar integralmente todas as classes de step definitions do módulo `15soat-phase4-e2e` (`BillingSteps`, `ExecutionSteps`, `E2ESagaSteps`, `AuthLambdaSteps` e `WorkOrderSteps`), aplicando os padrões consolidados de engenharia de testes:
- Eliminar race conditions e mutação concorrente decorrentes de `RestAssured.baseURI` estático global, encapsulando a URI base diretamente na `RequestSpecification` retornada por helpers `givenAuth()`.
- Garantir envio sistemático de autenticação (`Authorization: Bearer <token>`) em todos os endpoints protegidos por RBAC.
- Eliminar asserções lenientes e defensivas (`if (response != null && ...)` e `isIn(expectedStatus, 200)`), adotando asserções estritas AssertJ com mensagens de falha inequívocas.
- Assegurar isolamento entre execuções de cenários via hook `@Before` em todas as classes de steps.

## 2. Detalhamento Técnico das Modificações

### 2.1. `WorkOrderSteps.java`
- Configurar `.baseUri(EnvironmentConfig.getWorkOrderBaseUrl())` diretamente em `givenAuth()`.
- Manter o hook `@Before` e asserções estritas AssertJ.

### 2.2. `BillingSteps.java`
- Adicionar hook `@Before public void resetState()`.
- Implementar `private RequestSpecification givenAuth()` apontando para `EnvironmentConfig.getBillingBaseUrl()`, com injeção de token Bearer de analista de faturamento (`valid_billing_token`).
- Substituir chamadas `RestAssured.given()` por `givenAuth()`.
- Eliminar condicionais `if (response != null)` e `if (invoiceId != null)`, substituindo por `assertThat(response).as("Billing response must not be null").isNotNull()`.

### 2.3. `ExecutionSteps.java`
- Adicionar hook `@Before public void resetState()`.
- Implementar `private RequestSpecification givenAuth()` apontando para `EnvironmentConfig.getExecBaseUrl()`, com injeção de token Bearer de mecânico (`valid_mechanic_token`).
- Substituir chamadas `RestAssured.given()` por `givenAuth()`.
- Substituir validações defensivas por asserções estritas AssertJ.

### 2.4. `E2ESagaSteps.java`
- Adicionar hook `@Before public void resetState()`.
- Criar helpers dedicados com `RequestSpecification` isolada para cada microsserviço (`workOrderRequest()`, `billingRequest()`, `execRequest()`), eliminando alternâncias de `RestAssured.baseURI`.
- Injetar tokens Bearer apropriados nas requisições da Saga.
- Substituir asserções defensivas por asserções estritas AssertJ.

### 2.5. `AuthLambdaSteps.java`
- Adicionar hook `@Before public void resetState()`.
- Criar helper `givenAuth()` com `baseUri(EnvironmentConfig.getAuthLambdaBaseUrl())`.
- Corrigir a asserção leniente de status code HTTP:
  - Substituir `assertThat(response.getStatusCode()).isIn(expectedStatus, 200)` por validação estrita do `expectedStatus`.
- Substituir condicionais defensivas por asserções estritas AssertJ.

## 3. Validação
- Compilar o projeto com `mvn test-compile` em `15soat-phase4-e2e`.
- Executar os testes em modo dry-run com `mvn test "-Dtest=CucumberTestRunner" "-Dcucumber.execution.dry-run=true"`.
- Garantir que todos os 37 cenários continuem passando sem qualquer regressão.
