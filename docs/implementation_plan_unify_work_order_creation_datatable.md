# Plano de Implementação: Padronização da Abertura de Ordem de Serviço com DataTable no Gherkin

> **Decisão do Usuário**: Opção 1 - Uniformizar Todos os Cenários com DataTable Vertical (`Map<String, String>`)
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

O passo de abertura de Ordem de Serviço vinha sendo utilizado em dois formatos distintos em `work_order_lifecycle.feature`:
- No Cenário 1 (Happy Path), utilizava uma DataTable vertical declarativa (`Map<String, String>`).
- Nos Cenários 2, 3, 4, 5 e 6, utilizava uma frase inline longa com 3 parâmetros (`Parameter Bloat`).

Esta refatoração visa uniformizar todos os cenários da funcionalidade para o padrão tabular vertical, aumentando a legibilidade, a consistência visual da Documentação Viva e a extensibilidade futura.

---

## 2. Escopo das Alterações

### 2.1 Repositório `15soat-phase4-e2e`
- **Arquivo**: `src/test/resources/features/work-order/work_order_lifecycle.feature`
  - Substituir as ocorrências inline de abertura de OS nos 5 cenários alternativos e de exceção pela DataTable vertical:
    ```gherkin
    Dado que o atendente abre uma nova Ordem de Serviço com os seguintes dados:
      | customerDocument | 52998224725                       |
      | licensePlate     | BRA2E19                           |
      | description      | Troca de óleo e revisão de freios |
    ```
- **Arquivo**: `src/test/java/br/com/fiap/phase4/e2e/steps/WorkOrderSteps.java`
  - Adicionar a anotação `@Dado("que o atendente abre uma nova Ordem de Serviço com os seguintes dados:")` no método `attendantOpensWorkOrderWithDataTable(Map<String, String> data)`.

---

## 3. Plano de Execução Sequencial

1. **Atualização dos Step Definitions**:
   - Mapear a anotação `@Dado` no método `attendantOpensWorkOrderWithDataTable`.
2. **Atualização da Especificação BDD**:
   - Atualizar os cenários em `work_order_lifecycle.feature`.
3. **Validação do Quality Gate**:
   - Executar compilação de testes (`mvn test-compile`).
   - Executar suíte Cucumber dry-run (`mvn test "-Dtest=CucumberTestRunner" "-Dcucumber.execution.dry-run=true"`).
   - Validar que todos os 40 cenários continuam verdes e perfeitamente mapeados.
