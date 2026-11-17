# language: pt
@e2e @auth @lambda @usuario @iam
Funcionalidade: Gestão e Provisionamento de Identidades de Usuários via Serverless & Keycloak
  Como operador ou cliente da oficina mecânica
  Quero gerenciar identidades e provisionar usuários no IAM e no catálogo
  Para conceder acesso seguro e controlado às operações da plataforma

  # ============================================================================
  # Contexto: Disponibilidade da Infraestrutura de Autenticação e IAM
  # ============================================================================
  Contexto:
    Dado que a função serverless de autenticação e o Keycloak estão em execução e operacionais

  # ============================================================================
  # Cenário 1: Cadastro de Novo Funcionário com Perfil EMPLOYEE
  # ============================================================================
  @cadastro-funcionario
  Cenário: Administrador cadastra novo funcionário com sucesso
    Dado um administrador autenticado no sistema
    Quando o administrador cadastra um novo funcionário com os seguintes dados:
      | name            | Carlos Atendente     |
      | email           | carlos@garage.com.br |
      | document        | 86266070087          |
      | role            | EMPLOYEE             |
      | password        | StrongP@ss2026       |
    Então o funcionário deve ser provisionado com sucesso no Keycloak
    E a resposta deve confirmar a criação com status 201 e papel "EMPLOYEE"

  # ============================================================================
  # Cenário 2: Recusa de Cadastro com CPF Inválido
  # ============================================================================
  @cadastro-funcionario-invalido
  Cenário: Tentativa de cadastro de funcionário com documento inválido é rejeitada
    Dado um administrador autenticado no sistema
    Quando o administrador tenta cadastrar um funcionário com os seguintes dados:
      | name            | Funcionário Teste    |
      | email           | teste@garage.com.br  |
      | document        | 11111111111          |
      | role            | EMPLOYEE             |
      | password        | StrongP@ss2026       |
    Então o erro retornado deve corresponder a:
      | status    | 400                                                          |
      | errorCode | INVALID_DOCUMENT                                             |
      | title     | Invalid Document                                             |
      | detail    | CPF com formato inválido ou dígitos verificadores incorretos |

  # ============================================================================
  # Cenário 3: Atendente Cadastra Cliente na Recepção (Saga Orquestrada)
  # ============================================================================
  @cadastro-cliente-saga
  Cenário: Atendente cadastra novo cliente via saga orquestrada com sucesso
    Dado um atendente autenticado no sistema com perfil "EMPLOYEE"
    Quando o atendente cadastra um novo cliente com os seguintes dados:
      | name     | Rodrigo Cliente      |
      | email    | rodrigo@fiap.com.br  |
      | document | 52998224725          |
      | role     | CUSTOMER             |
      | password | CustomerPass123!     |
    Então o cliente deve ser provisionado com sucesso na Saga Orquestrada
    E a resposta deve confirmar a criação com status 201 e papel "CUSTOMER"

  # ============================================================================
  # Cenário 4: Falha na API Work Order Dispara Rollback Compensatório no Keycloak
  # ============================================================================
  @cadastro-cliente-rollback
  Cenário: Falha de persistência no catálogo aciona rollback compensatório da saga
    Dado um atendente autenticado no sistema com perfil "EMPLOYEE"
    Quando o atendente tenta cadastrar um cliente que causa falha no catálogo:
      | name     | Rodrigo Falha        |
      | email    | falha@garage.com.br  |
      | document | 52998224725          |
      | role     | CUSTOMER             |
      | password | CustomerPass123!     |
    Então o erro retornado deve corresponder a:
      | status    | 502                                                         |
      | errorCode | REGISTRATION_ROLLBACK                                       |
      | title     | Registration Rollback                                       |
      | detail    | Falha na sincronização cadastral compensada no Keycloak     |
