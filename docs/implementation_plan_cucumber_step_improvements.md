# Plano de Implementação: Melhorias Arquiteturais e de Qualidade nos Steps do Cucumber

> **Decisão do Usuário**: Opção 1 - Request Specification Centralizada (DRY), Eliminação de Falsos Positivos e Hook @Before
> **Status**: Aprovado para Execução

## 1. Contexto & Objetivos
Refatorar a classe `WorkOrderSteps.java` no módulo `15soat-phase4-e2e` para:
- Centralizar a construção de requisições HTTP via método auxiliar `givenAuth()` com injeção padronizada de `ContentType.JSON` e cabeçalho `Authorization: Bearer <token>`.
- Eliminar o anti-pattern de asserções condicionais defensivas (`if (response != null && ...)`), substituindo por asserções estritas do AssertJ (`assertThat(response).as("HTTP response must not be null").isNotNull()`), garantindo falha imediata caso a resposta seja nula.
- Adicionar hook `@Before` do Cucumber para reinicializar o estado interno da classe a cada cenário (`response`, `workOrderId`, `customerDoc`, `vehiclePlate`, `currentMaterialId`, `currentAuthToken`), assegurando isolamento completo entre execuções.

## 2. Alterações Detalhadas em `WorkOrderSteps.java`
1. **Imports**:
   - Adicionar `io.cucumber.java.Before` e `io.restassured.specification.RequestSpecification`.
2. **Hook `@Before`**:
   - Resetar campos de estado para valores limpos antes de cada cenário:
     - `response = null;`
     - `workOrderId = null;`
     - `customerDoc = null;`
     - `vehiclePlate = null;`
     - `currentMaterialId = null;`
     - `currentAuthToken = "Bearer mock_employee_jwt_token";`
     - `pendingCustomerName = null;`
     - `pendingCustomerEmail = null;`
     - `pendingServiceCode = null;`
     - `pendingServiceName = null;`
     - `pendingServicePrice = 0.0;`
     - `pendingMaterialSku = null;`
     - `pendingMaterialDescription = null;`
     - `pendingMaterialCost = 0.0;`
     - `pendingMaterialPrice = 0.0;`
     - `pendingMaterialQty = 0;`
3. **Helper `givenAuth()`**:
   - Construir e retornar `RequestSpecification` contendo `ContentType.JSON` e, quando `currentAuthToken != null`, o cabeçalho `Authorization`.
4. **Substituição de `RestAssured.given()`**:
   - Atualizar todos os métodos mutadores e consultas para utilizarem `givenAuth()` (ou `givenAuth()` para mutações/consultas autenticadas).
5. **Substituição de Verificações `if (response != null)`**:
   - Substituir `if (response != null && ...)` por `assertThat(response).as("HTTP response must not be null").isNotNull();` seguido das validações de status code e payload.

## 3. Validação
- Compilar o módulo com `mvn test-compile`.
- Executar os testes em modo dry-run com `mvn test "-Dtest=CucumberTestRunner" "-Dcucumber.execution.dry-run=true"`.
- Garantir que todos os 37 cenários continuem passando sem regressão.
