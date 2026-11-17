# Plano de Implementação: Segregação de Steps (Customer & Vehicle) e Expansão do ArchUnit

> **Decisão do Usuário**: Opção 1 - Segregação Canônica de Steps (`CustomerSteps` e `VehicleSteps`) com `ScenarioContext` + Expansão do ArchUnit para `Billing` e `Exec`
> **Status**: Aprovado para Execução

---

## 1. Contexto e Motivação
- A classe `CustomerVehicleSteps.java` violava o Princípio da Responsabilidade Única (SRP) ao acumular regras de negócio de clientes e veículos, além de manter variáveis mutáveis de instância fora do ciclo de vida seguro do `ScenarioContext`.
- Os microsserviços `15soat-phase4-api-billing` e `15soat-phase4-api-exec` careciam de salvaguardas arquiteturais automatizadas com ArchUnit, diferentemente do `api-work-order`.

---

## 2. Escopo das Alterações

### A. Repositório `15soat-phase4-e2e`
1. **Evolução de `ScenarioContext.java`**:
   - Adicionar atributos `pendingCustomerName` e `pendingCustomerEmail` para isolar totalmente o estado entre cenários.
2. **Segregação Canônica de Classes**:
   - Criar `CustomerSteps.java` para tratar exclusivamente cadastro, consulta, atualização e exclusão de clientes.
   - Criar `VehicleSteps.java` para tratar exclusivamente vinculação de veículos, consulta por placa e exclusão veicular.
   - Remover `CustomerVehicleSteps.java`.

### B. Repositório `15soat-phase4-api-billing`
1. **Configuração de Dependência**:
   - Declarar `com.tngtech.archunit:archunit-junit5` em `domain/pom.xml`.
2. **Testes Arquiteturais**:
   - Criar `HexagonalArchitectureArchTest.java` validando isolamento de frameworks web e convenções de interfaces em portas de entrada/saída.

### C. Repositório `15soat-phase4-api-exec`
1. **Configuração de Dependência**:
   - Declarar `com.tngtech.archunit:archunit-junit5` em `domain/pom.xml`.
2. **Testes Arquiteturais**:
   - Criar `HexagonalArchitectureArchTest.java` validando barreiras de domínio puro e convenções hexagonais.

---

## 3. Critérios de Aceite e Verificação
- Todos os 40 cenários BDD do `15soat-phase4-e2e` executando e passando 100% verdes.
- Suíte `mvn test` em `api-work-order`, `api-billing` e `api-exec` 100% verde com todos os testes ArchUnit passando.
- Proibição estrita de commits automáticos sem autorização prévia por escrito.
