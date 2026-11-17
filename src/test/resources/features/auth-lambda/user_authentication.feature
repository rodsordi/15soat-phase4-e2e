# language: pt
@e2e @auth @lambda @autenticacao @oidc
Funcionalidade: Autenticação de Usuários e Emissão de Tokens JWT via Serverless & Keycloak
  Como ator da plataforma (Administrador, Funcionário ou Cliente)
  Quero autenticar minhas credenciais através do endpoint seguro
  Para obter um token de acesso Bearer JWT com meus papéis autorizados

  # ============================================================================
  # Contexto: Disponibilidade da Infraestrutura de Autenticação OIDC
  # ============================================================================
  Contexto:
    Dado que a função serverless de autenticação e o Keycloak estão em execução e operacionais

  # ============================================================================
  # Cenário 1: Autenticação de Administrador e Emissão de Token ADMIN
  # ============================================================================
  @login-admin
  Cenário: Administrador realiza login e recebe token JWT com papel ADMIN
    Dado um administrador cadastrado com usuário "admin_user" e senha "AdminPassword123!"
    Quando o administrador realiza a autenticação com as credenciais:
      | username | admin_user        |
      | password | AdminPassword123! |
    Então a autenticação deve ser realizada com sucesso com código HTTP 200
    E o token JWT emitido deve conter a claim de papéis com "ADMIN"

  # ============================================================================
  # Cenário 2: Autenticação de Funcionário e Emissão de Token EMPLOYEE
  # ============================================================================
  @login-funcionario
  Cenário: Funcionário realiza login e recebe token JWT com papel EMPLOYEE
    Dado um funcionário cadastrado com documento "86266070087" e senha "StrongP@ss2026"
    Quando o funcionário realiza a autenticação com as credenciais:
      | username | 86266070087    |
      | password | StrongP@ss2026 |
    Então a autenticação deve ser realizada com sucesso com código HTTP 200
    E o token JWT emitido deve conter a claim de papéis com "EMPLOYEE"

  # ============================================================================
  # Cenário 3: Autenticação de Cliente e Emissão de Token CUSTOMER
  # ============================================================================
  @login-cliente
  Cenário: Cliente realiza login e recebe token JWT com papel CUSTOMER
    Dado um cliente cadastrado com documento "43768298096" e senha "CustomerPass123!"
    Quando o cliente realiza a autenticação com as credenciais:
      | username | 43768298096      |
      | password | CustomerPass123! |
    Então a autenticação deve ser realizada com sucesso com código HTTP 200
    E o token JWT emitido deve conter a claim de papéis com "CUSTOMER"

  # ============================================================================
  # Cenário 4: Falha de Autenticação com Senha Incorreta
  # ============================================================================
  @login-falha
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
