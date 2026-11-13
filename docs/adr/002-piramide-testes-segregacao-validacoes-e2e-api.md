# ADR-002: Pirâmide de Testes e Segregação de Validações de Entrada (E2E vs. Módulo da API)

## Status
**Aprovado**

## Contexto
Durante o desenvolvimento das especificações BDD, levantou-se o questionamento sobre a conveniência de incluir testes exaustivos de validação de campos nos arquivos `.feature` do projeto E2E — tais como:
- Validação de CPF com dígitos verificadores inválidos (Módulo 11).
- Validação de formato de e-mail sintaticamente incorreto.
- Limite mínimo e máximo de caracteres em nomes de clientes, placas de veículos ou descrições.
- Caracteres especiais em campos estritamente alfanuméricos.

No nível E2E, cada cenário envolve a subida ou comunicação com múltiplos contêineres reais (Kong API Gateway, microsserviço Spring Boot, banco de dados PostgreSQL). Executar permutações combinatórias de validação de input nesse nível acarretaria:
1. **Lentidão Severa da Pipeline (Pipeline Bloat)**: Testes E2E são lentos por natureza devido à rede, I/O e serialização JSON.
2. **Poluição do BDD de Negócio**: O Cucumber deve expressar regras de negócio e jornadas do usuário, e não atuar como validador de anotações `@NotNull` ou `@Size` da camada web.
3. **Violação da Pirâmide de Testes**: A base da pirâmide (testes unitários e de integração de slice) deve concentrar a maior densidade de testes combinatórios rápidos e baratos.

## Decisão
Decidiu-se **manter a suíte E2E focada exclusivamente nos fluxos críticos de negócio e exceções de domínio de alto nível**, segregando as validações de input para a camada de testes do próprio módulo da API:

1. **Escopo dos Testes E2E (`15soat-phase4-e2e`)**:
   - Jornadas de ponta a ponta (Happy Paths).
   - Validação de contratos gerais e orquestrações cross-service (Sagas e Gateway).
   - Exceções críticas de integridade de negócio (ex: tentativa de duplicidade cadastral, entidade inexistente).
2. **Escopo dos Testes da API (`15soat-phase4-api-work-order`)**:
   - **Testes Unitários de Domínio (`CustomerTest.java`)**:
     - Validação exaustiva do algoritmo de CPF (Módulo 11), valores nulos, em branco, caracteres repetidos (`00000000000`, `11111111111`) e formato inválido.
     - Validação de constraints em entidades/Value Objects com execução instantânea (milissegundos).
   - **Testes de Integração de Controller (`CustomerControllerTest.java`)**:
     - Validação de DTOs de entrada com Bean Validation (`@Valid`, `@NotBlank`, `@Email`) usando `@WebMvcTest`.
     - Confirmação do retorno de `400 Bad Request` com ProblemDetail para e-mails malformados e nomes vazios, sem onerar o banco de dados.

## Consequências

### Positivas
- **Pipeline Rápida e Confiável**: Testes E2E permanecem enxutos, focados e rápidos de executar.
- **Feedback Imediato ao Desenvolvedor**: Erros de validação de input são capturados em menos de 1 segundo durante o `mvn test` local do desenvolvedor no microsserviço.
- **BDD com Alto Valor de Negócio**: Os arquivos `.feature` tornam-se compreensíveis para stakeholders e Product Owners, sem dezenas de cenários técnicos repetitivos.

### Trade-offs
- Desenvolvedores precisam manter a disciplina de implementar testes de unidade e `@WebMvcTest` nos microsserviços correspondentes sempre que novas restrições de schema forem adicionadas.
