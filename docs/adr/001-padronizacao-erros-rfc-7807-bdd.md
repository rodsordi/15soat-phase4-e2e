# ADR-001: Padronização Contratual de Tratamento de Erros nos Cenários BDD com RFC 7807

## Status
**Aprovado**

## Contexto
Anteriormente, os cenários de falha de negócio e validação nos arquivos `.feature` do Cucumber continham assertivas genéricas e frágeis sobre o retorno das APIs, tipicamente apenas checando o status HTTP:
```gherkin
Then the response status should be 400
```
Essa abordagem gerava problemas significativos:
1. **Ambiguidade Contratual**: Qualquer erro que retornasse `400 Bad Request` passaria no teste, mesmo se a causa raiz fosse incorreta (ex: payload malformado versus cliente duplicado).
2. **Ausência de Padrão nas APIs**: O ecossistema Spring Boot 3/4 adota nativamente o padrão internacional **RFC 7807 (Problem Details for HTTP APIs)**, expondo atributos estruturados (`status`, `title`, `detail`, `instance`, além de extensões como `errorCode`).
3. **Divergência entre BDD e API**: O contrato esperado pelo negócio e documentado no BDD não refletia a estrutura real devolvida pelas APIs aos clientes/consumidores.

## Decisão
Decidiu-se que **todos os cenários de fluxo de exceção (Sad Paths) nos arquivos `.feature` do Cucumber devem validar a resposta de erro através de uma Cucumber DataTable estrita baseada na RFC 7807**, exigindo as seguintes propriedades canônicas:
- `status`: Código de status HTTP numérico (ex: `400`, `404`, `409`, `422`).
- `errorCode`: Código de erro de negócio padronizado em caixa alta (ex: `CUSTOMER_ALREADY_EXISTS`, `INVALID_DOCUMENT`, `VEHICLE_ALREADY_EXISTS`).
- `title`: Título resumido da categoria do erro (ex: `Customer Already Exists`, `Invalid Customer Document`).
- `detail`: Mensagem detalhada contextual da violação (ex: `Customer with document 52998224725 is already registered`).

Exemplo canônico adotado:
```gherkin
Then the response status code should be 409
And the response should contain the following RFC 7807 problem details:
  | status | errorCode               | title                   | detail                                                 |
  | 409    | CUSTOMER_ALREADY_EXISTS | Customer Already Exists | Customer with document 52998224725 is already registered |
```

## Consequências

### Positivas
- **Contrato Inequívoco**: A especificação executável (BDD) torna-se uma documentação viva irrefutável do payload de erro devolvido pela API.
- **Prevenção de Falsos Positivos**: Impede que exceções não tratadas ou genéricas façam o teste passar acidentalmente.
- **Simetria com o Spring Boot**: Alinhamento direto com o `ProblemDetail` do Spring Framework e handlers globais `@RestControllerAdvice`.

### Trade-offs
- Exige que qualquer alteração textual no `detail` ou `errorCode` na API seja refletida no cenário BDD, aumentando o acoplamento do teste ao contrato exato (o que é intencional para uma suíte de integração e aceitação E2E).
