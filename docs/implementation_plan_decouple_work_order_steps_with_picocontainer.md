# Plano de Implementação: Decomposição de Steps por Subdomínio e Contexto Compartilhado via PicoContainer

> **Decisão do Usuário**: Opção 1 - Decomposição por Domínio + Contexto Compartilhado via `cucumber-picocontainer`
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

A classe `WorkOrderSteps.java` acumulava mais de 800 linhas e 15 variáveis de instância mutáveis, atuando como um "God Object" que misturava quatro subdomínios distintos da oficina mecânica:
- Gestão de Clientes e Veículos
- Catálogo de Serviços
- Catálogo e Estoque de Materiais/Peças
- Ciclo de Vida da Ordem de Serviço

O objetivo desta refatoração é:
1. Adicionar o framework padrão `cucumber-picocontainer` para injeção de dependência nativa do Cucumber.
2. Criar uma classe de contexto compartilhado por cenário (`ScenarioContext`), eliminando o reset manual de estado (`@Before resetState()`).
3. Decompor `WorkOrderSteps.java` em 4 classes coesas e aderentes ao Princípio da Responsabilidade Única (SRP):
   - `CustomerVehicleSteps.java`
   - `ServiceCatalogSteps.java`
   - `MaterialCatalogSteps.java`
   - `WorkOrderSteps.java`

---

## 2. Escopo das Alterações

### 2.1 Repositório `15soat-phase4-e2e`
- **Arquivo**: `pom.xml`
  - Adicionar a dependência `io.cucumber:cucumber-picocontainer:${cucumber.version}` com escopo `test`.
- **Novo Arquivo**: `src/test/java/br/com/fiap/phase4/e2e/context/ScenarioContext.java`
  - Encapsular estado compartilhado entre steps: `response`, `currentAuthToken`, `workOrderId`, `customerDoc`, `vehiclePlate`, `currentMaterialId` e o helper `givenAuth(String baseUrl)`.
- **Novo Arquivo**: `src/test/java/br/com/fiap/phase4/e2e/steps/CustomerVehicleSteps.java`
  - Recebe `ScenarioContext` via construtor e gerencia passos de clientes e veículos (`customer_vehicle_management.feature`).
- **Novo Arquivo**: `src/test/java/br/com/fiap/phase4/e2e/steps/ServiceCatalogSteps.java`
  - Recebe `ScenarioContext` via construtor e gerencia passos de catálogo de serviços (`service_catalog_management.feature`).
- **Novo Arquivo**: `src/test/java/br/com/fiap/phase4/e2e/steps/MaterialCatalogSteps.java`
  - Recebe `ScenarioContext` via construtor e gerencia passos de materiais e estoque (`material_management.feature`).
- **Arquivo**: `src/test/java/br/com/fiap/phase4/e2e/steps/WorkOrderSteps.java`
  - Recebe `ScenarioContext` via construtor, focado estritamente no ciclo de vida da OS (`work_order_lifecycle.feature`), autenticação de atores e asserções de erro da API.

---

## 3. Plano de Execução Sequencial

1. **Configuração Maven**:
   - Adicionar `cucumber-picocontainer` ao `pom.xml`.
2. **Criação de `ScenarioContext`**:
   - Implementar `ScenarioContext` no pacote `br.com.fiap.phase4.e2e.context`.
3. **Criação das Classes Especializadas**:
   - Implementar `CustomerVehicleSteps.java`.
   - Implementar `ServiceCatalogSteps.java`.
   - Implementar `MaterialCatalogSteps.java`.
4. **Refatoração de `WorkOrderSteps.java`**:
   - Remover código migrado para as classes especializadas e injetar `ScenarioContext`.
5. **Quality Gate & Verificação**:
   - Compilar o projeto de testes (`mvn test-compile`).
   - Executar suíte Cucumber dry-run (`mvn test "-Dtest=CucumberTestRunner" "-Dcucumber.execution.dry-run=true"`).
   - Validar que todos os 40 cenários continuam 100% verdes sem steps duplicados ou indefinidos.
