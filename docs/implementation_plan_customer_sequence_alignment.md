# Plano de Implementação: Harmonização de Clientes e Veículos com Diagrama de Sequência

> **Decisão do Usuário**: Opção 1 - Harmonização Completa (Alinhamento Estrito ao Diagrama e RFC 7807 / RFC 4918)
> **Status**: Aprovado para Execução

## Contexto e Objetivo
Garantir total fidelidade ao diagrama de sequência de Gestão de Clientes e Veículos (`customer_vehicle_management.md`), à especificação viva em BDD (`customer_vehicle_management.feature`) e à RFC 4918 / RFC 7807:
1. O step de consulta por documento deve carregar a representação detalhada canônica (`GET /api/v1/customers/{id}`) para validar todos os atributos do cliente, incluindo `email`.
2. O step de preparação de dados (`prepareCustomerDataFromTable`) não deve executar deleção acidental de clientes quando um fixture tiver acabado de ser provisionado pelo step anterior (`Dado o cliente com documento ... cadastrado`), garantindo que o teste de duplicidade rejeite com `400 CUSTOMER_ALREADY_EXISTS`.
3. Preservar a assertiva contratual de `422 Unprocessable Entity` para `CUSTOMER_HAS_VEHICLES` no cenário de bloqueio de exclusão por integridade referencial.

## Escopo de Alterações no Repositório E2E

### 1. `ScenarioContext.java`
Adicionar flag de controle `preserveCustomer` para sinalizar quando um cliente acabou de ser provisionado como fixture no cenário:
```java
private boolean preserveCustomer;
```

### 2. `CustomerSteps.java`
1. Em `customerAlreadyRegistered(String doc)`:
   - Marcar `context.setPreserveCustomer(true);`.
2. Em `prepareCustomerDataFromTable(Map<String, String> data)`:
   - Se `context.isPreserveCustomer()` for verdadeiro, consumir o estado sem deletar (`context.setPreserveCustomer(false)`).
   - Se for falso, executar `cleanUpExistingCustomerIfExists(doc)`.
3. Em `executeCustomerQuery(String doc)`:
   - Consultar primeiro por documento para obter o ID (`GET /api/v1/customers?document=` + doc).
   - Se o ID existir, carregar a entidade completa (`GET /api/v1/customers/{id}`), permitindo validar `email`, `name`, `document` e `vehicles`.
