# language: pt
@e2e @auth @lambda @seguranca
Funcionalidade: Autenticação e Autorização Serverless via Lambda Token Authorizer
  Como API Gateway e microsserviços da oficina
  Quero validar tokens JWT através de um Lambda Authorizer
  Para proteger os endpoints públicos contra acessos não autorizados

  # ============================================================================
  # Contexto: Disponibilidade da Infraestrutura de Autenticação
  # ============================================================================
  Contexto:
    Dado que o serviço de autenticação e autorização está em execução e operacional

  # ============================================================================
  # Cenário 1: Autorização com Token Válido
  # ============================================================================
  @token-valido
  Cenário: Requisição com Token Bearer válido é autorizada com sucesso
    Quando uma requisição é enviada para o API Gateway com o token de autorização "Bearer customer_valid_token_12345"
    Então o Lambda Authorizer deve retornar uma política com efeito "Allow"

  # ============================================================================
  # Cenário 2: Recusa com Token Inválido
  # ============================================================================
  @token-invalido
  Cenário: Requisição com Token inválido ou revogado é negada
    Quando uma requisição é enviada para o API Gateway com o token de autorização "Bearer deny"
    Então o Lambda Authorizer deve retornar uma política com efeito "Deny"
