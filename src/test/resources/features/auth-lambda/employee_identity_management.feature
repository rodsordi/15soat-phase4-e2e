# language: pt
@e2e @auth @lambda @funcionario @iam
Funcionalidade: Gestão de Identidade e Autenticação de Funcionários via Serverless & Keycloak
  Como administrador da oficina mecânica
  Quero cadastrar e autenticar funcionários da oficina
  Para conceder acesso seguro e controlado às operações de atendimento e gestão

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
  # Cenário 3: Autenticação de Funcionário e Emissão de Token JWT
  # ============================================================================
  @login-funcionario
  Cenário: Funcionário realiza login e recebe token JWT com perfil EMPLOYEE
    Dado um funcionário cadastrado com documento "86266070087" e senha "StrongP@ss2026"
    Quando o funcionário realiza a autenticação com as credenciais:
      | username | 86266070087    |
      | password | StrongP@ss2026 |
    Então a autenticação deve ser realizada com sucesso com código HTTP 200
    E o token JWT emitido deve conter a claim de papéis com "EMPLOYEE"

  # ============================================================================
  # Cenário 4: Falha de Autenticação com Senha Incorreta
  # ============================================================================
  @login-funcionario-falha
  Cenário: Tentativa de login com senha incorreta é negada
    Dado um funcionário cadastrado com documento "86266070087"
    Quando o funcionário tenta realizar autenticação com senha inválida:
      | username | 86266070087    |
      | password | SenhaErrada123 |
    Então o erro retornado deve corresponder a:
      | status    | 401                                       |
      | errorCode | INVALID_CREDENTIALS                       |
      | title     | Invalid Credentials                       |
      | detail    | Usuário ou senha inválidos para o domínio |
