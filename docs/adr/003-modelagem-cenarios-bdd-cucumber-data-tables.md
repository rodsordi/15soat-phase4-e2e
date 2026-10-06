# ADR-003: Modelagem e Sintaxe de Cenários BDD com Cucumber Data Tables e Voz Ativa

## Status
**Aprovado**

## Contexto
Vários arquivos `.feature` do projeto apresentavam estilos heterogêneos de escrita herdados de iterações anteriores, incluindo:
1. **Passos com Sintaxe de Primeira Pessoa Passiva**: Ex: `Given I am an attendant`, `When I send a request to...`, `Then I should receive...`. Essa abordagem foca no testador e não na intenção do sistema ou na ação do ator do domínio.
2. **Parâmetros Espalhados na Linha do Passo**: Passagem de parâmetros inline (ex: `When the attendant creates a customer with document "52998224725" and name "John Doe" and email "john@example.com"`), tornando a frase longa, de difícil leitura e propensa a quebras quando novos campos são adicionados.
3. **Falta de Estrutura Tabular para Entidades**: Ausência de tabelas descritivas claras para entidades ricas (Clientes, Veículos, Itens de Catálogo, Materiais, Invoices).

## Decisão
Decidiu-se **padronizar a modelagem e sintaxe de todos os arquivos `.feature` do projeto sob três pilares obrigatórios**:

1. **Voz Ativa com Papéis de Domínio**:
   - Eliminação da primeira pessoa (`I`, `I am`, `I send`).
   - Uso de papéis claros do domínio como sujeito da ação: `the attendant`, `the mechanic`, `the system`, `the customer`.
2. **Cucumber Data Tables para Entradas e Saídas Estruturadas**:
   - Toda criação ou envio de entidade com mais de dois atributos deve utilizar tabelas de dados (`DataTable`) com cabeçalhos padronizados em *camelCase*.
   - Exemplo:
     ```gherkin
     When the attendant creates a new customer with the following details:
       | document    | name       | email               |
       | 52998224725 | João Silva | joao.silva@fiap.com |
     ```
3. **Simetria entre Criação e Consulta**:
   - As assertivas de consulta e verificação de resposta utilizam o mesmo formato tabular para assegurar consistência visual e facilidade de comparação:
     ```gherkin
     Then the customer details should be returned as follows:
       | document    | name       | email               |
       | 52998224725 | João Silva | joao.silva@fiap.com |
     ```

## Consequências

### Positivas
- **Legibilidade Superior**: A especificação lê-se como documentação executável limpa, compreensível por analistas de negócio e desenvolvedores.
- **Extensibilidade**: Inclusão de novos atributos em entidades exige apenas adicionar uma nova coluna na tabela, sem quebrar os regexes dos steps existentes.
- **Padronização em Todo o Repositório**: Aplicado em `customer_vehicle_management.feature`, `service_catalog_management.feature`, `material_management.feature`, `materials_and_checklist.feature`, `execution_queue_and_repair.feature` e `invoice_generation.feature`.

### Trade-offs
- Os step definitions em Java (`Cucumber steps`) necessitam de métodos capazes de desserializar `DataTable` diretamente para Mapas ou DTOs usando `@DataTableType`.
