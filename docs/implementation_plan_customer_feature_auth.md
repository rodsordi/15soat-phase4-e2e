# Plano de Implementação: Autenticação Declarativa no Contexto do BDD de Clientes e Veículos

> **Decisão do Usuário**: Opção 1 - Autenticação Declarativa no Contexto da Feature (`customer_vehicle_management.feature`)
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivo
Refletir formalmente nos cenários BDD de `customer_vehicle_management.feature` que o operador é um atendente autenticado da oficina (com perfil/role `EMPLOYEE`), mantendo a simetria com a `ADR-005` e o diagrama de sequência `customer_vehicle_management.md`.

---

## 2. Mudanças a Serem Aplicadas

### 2.1. Arquivo: `src/test/resources/features/work-order/customer_vehicle_management.feature`
- Atualizar a seção `Contexto:` para incluir a precondição de autenticação:
  ```gherkin
  Contexto:
    Dado que o serviço de Ordem de Serviço está em execução e operacional
    E que o atendente da oficina está devidamente autenticado com perfil "EMPLOYEE"
  ```
- Adicionar cenário de segurança demonstrando a proteção de acesso (tentativa de cadastro sem autenticação):
  ```gherkin
  # ============================================================================
  # Cenário 9: Tentativa de Cadastro sem Autenticação (Segurança / RBAC)
  # ============================================================================
  @seguranca @nao-autenticado
  Cenário: Tentativa de cadastro de cliente por operador não autenticado é negada
    Dado que o operador não possui token de autenticação válido
    Quando o cliente com documento "52998224725", nome "Anônimo" e email "anonimo@fiap.com.br" tenta ser cadastrado
    Então o código de status HTTP da resposta deve ser 401
  ```

### 2.2. Arquivo: `src/test/java/br/com/fiap/phase4/e2e/steps/WorkOrderSteps.java`
- Implementar o step definition para:
  `@Dado("que o atendente da oficina está devidamente autenticado com perfil {string}")`
  - Configurando o header `Authorization: Bearer <employee_token>` no contexto do RestAssured.
- Implementar o step definition para:
  `@Dado("que o operador não possui token de autenticação válido")`
  `@Quando("o cliente com documento {string}, nome {string} e email {string} tenta ser cadastrado")`

---

## 3. Validação
- Compilar os testes com `mvn test-compile`.
- Validar conformidade de BDD e boas práticas.
- Consultar o usuário para autorização de commit e push conforme a Diretriz Global 1.
