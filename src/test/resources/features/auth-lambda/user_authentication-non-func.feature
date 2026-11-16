#language: pt
@e2e @nao-funcional @security @performance @auth @lambda @iso-25010
Funcionalidade: Requisitos Não-Funcionais - Autenticação de Usuários e Emissão de Tokens JWT
  Como arquiteto de segurança da informação
  Quero validar os requisitos não-funcionais de autenticação OIDC, latência de emissão e segurança de credenciais
  Para garantir a integridade da borda de identidade e a conformidade com as políticas corporativas (ADR-005)

  # ============================================================================
  # Cenário NF-1: Rejeição de Requisição com Credenciais Inválidas (Segurança)
  # ============================================================================
  @seguranca @autenticacao @credenciais-invalidas
  Cenário: Rejeição de tentativa de login com credenciais incorretas com HTTP 401
    Dado que a função serverless de autenticação e o Keycloak estão em execução e operacionais
    Dado um funcionário cadastrado com documento "86266070087"
    Quando o funcionário tenta realizar autenticação com senha inválida:
      | username | 86266070087    |
      | password | SenhaErrada123 |
    Então o código de status HTTP da resposta deve ser 401

  # ============================================================================
  # Cenário NF-2: SLA de Latência na Emissão de Token JWT (Performance)
  # ============================================================================
  @performance @sla @latencia
  Cenário: Validação de SLA de tempo de resposta na emissão de token OIDC
    Dado que a função serverless de autenticação e o Keycloak estão em execução e operacionais
    Dado um administrador cadastrado com usuário "admin_user" e senha "AdminPassword123!"
    Quando o administrador realiza a autenticação com as credenciais:
      | username | admin_user        |
      | password | AdminPassword123! |
    Então a autenticação deve ser realizada com sucesso com código HTTP 200
    E o tempo de resposta da requisição deve ser inferior a 3000 ms
