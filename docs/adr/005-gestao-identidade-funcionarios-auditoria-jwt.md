# ADR-005: Gestão de Identidade de Funcionários Centralizada no IAM (Keycloak) e Auditoria por Claims JWT

## Status
**Aprovado**

## Contexto
Na operação diária da oficina mecânica, clientes e veículos são cadastrados presencialmente por um **funcionário da oficina** (atendente/recepcionista).
Durante a modelagem da Fase 4, levantou-se a questão sobre a necessidade de criar uma nova tabela relacional `employee` no banco de dados relacional (`work_order_db`) para persistir o atendente, espelhando a abordagem da Fase 3.

A criação de uma tabela relacional de funcionários no `work_order_db` geraria os seguintes problemas:
1. **Duplicação de Identidade**: O funcionário já é cadastrado obrigatoriamente no Keycloak (com credenciais, hash de senha, e-mail e roles de segurança). Replicar esses dados em uma tabela `employee` cria o problema de sincronização de dados cadastrais entre dois bancos distintos.
2. **Complexidade Excessiva de Saga**: Qualquer atualização cadastral ou exclusão de funcionário exigiria orquestração distribuída entre Keycloak e PostgreSQL.
3. **Ausência de Requisitos de Domínio para Funcionários**: No microsserviço de Ordens de Serviço, o funcionário não possui regras de negócio complexas (como comissões, hierarquia salarial ou histórico de ponto); ele atua unicamente como o **operador autenticado** que executa transações de atendimento. A execução mecânica foi segregada para o microsserviço `15soat-phase4-api-exec`.

## Decisão
Decidiu-se **não criar uma nova tabela de funcionários no PostgreSQL**, estabelecendo que:

1. **Keycloak como Única Fonte da Verdade para Funcionários**:
   - O provisionamento de funcionários é realizado no Keycloak com a role corporativa `EMPLOYEE` (ou `ADMIN`).
   - A autenticação gera um token OpenID Connect (OIDC) / JWT contendo as claims de identidade (`sub` - UUID único, `preferred_username` e `roles: ["EMPLOYEE"]`).
2. **Controle de Acesso Baseado em Papéis (RBAC)**:
   - Os endpoints administrativos e de cadastro na API Work Order (ex: `POST /api/v1/customers`, `POST /api/v1/vehicles`, `POST /api/v1/work-orders`) exigem a permissão `ROLE_EMPLOYEE` ou `ROLE_ADMIN` validada pelo Kong API Gateway e pelo Spring Security.
3. **Auditoria Transacional por Injeção de Claims JWT**:
   - A rastreabilidade de quem realizou a operação é garantida gravando o identificador do funcionário extraído do token JWT (`sub` ou `preferred_username`) diretamente nas colunas de auditoria já existentes nas tabelas do banco de dados:
     - `created_by` (ex: `VARCHAR(100)`)
     - `last_modified_by` (ex: `VARCHAR(100)`)
   - Isso garante rastreabilidade forense completa sem a sobrecarga de gerenciar entidades relacionais extras.

## Consequências

### Positivas
- **Zero Redundância de Dados**: Não há duplicidade de registros entre Keycloak e PostgreSQL.
- **Arquitetura Cloud-Native e Desacoplada**: A gestão de ciclo de vida de colaboradores (admissão, bloqueio de acesso, reset de senha) é tratada nativamente no IdP (Keycloak).
- **Simplicidade de Banco de Dados**: O schema do `work_order_db` permanece limpo e focado no core business (clientes, veículos, ordens de serviço, catálogo e itens).
- **Auditoria Nativa**: O Spring Data JPA / Spring Security Auditing (`@CreatedBy`, `AuditorAware`) preenche automaticamente o usuário sem chamadas de rede adicionais.

### Trade-offs
- Consultas analíticas que requeiram nomes de exibição amigáveis de funcionários antigos dependem de preservar o username na coluna `created_by` ou realizar lookup pontual no Keycloak se apenas o UUID `sub` for armazenado.
