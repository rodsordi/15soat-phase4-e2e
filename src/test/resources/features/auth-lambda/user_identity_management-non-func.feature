#language: pt
@e2e @nao-funcional @security @resiliencia @performance @auth @lambda @iso-25010
Funcionalidade: Requisitos Não-Funcionais - Gestão de Identidade e Onboarding de Usuários
  Como engenheiro de confiabilidade (SRE)
  Quero assegurar a resiliência distribuída, tempos de resposta aceitáveis e proteção na borda do provisionamento de identidades
  Para garantir tolerância a falhas na integração Keycloak e APIs de negócio

  # ============================================================================
  # Cenário NF-1: Proteção de Borda contra Injeção de Documentos Inválidos (Segurança)
  # ============================================================================
  @seguranca @validacao-defensiva
  Cenário: Bloqueio precoce de onboarding com CPF matematicamente inválido
    Dado que a função serverless de autenticação e o Keycloak estão em execução e operacionais
    Dado um administrador autenticado no sistema
    Quando o administrador tenta cadastrar um funcionário com os seguintes dados:
      | name     | Funcionário Teste    |
      | email    | teste@garage.com.br  |
      | document | 11111111111          |
      | role     | EMPLOYEE             |
      | password | StrongP@ss2026       |
    Então a requisição deve ser rejeitada com status 400
    E a mensagem de erro deve indicar o código "INVALID_DOCUMENT"

  # ============================================================================
  # Cenário NF-2: SLA de Performance no Provisionamento de Usuário Administrativo
  # ============================================================================
  @performance @sla
  Cenário: Cumprimento do SLA de tempo de resposta no provisionamento administrativo
    Dado que a função serverless de autenticação e o Keycloak estão em execução e operacionais
    Dado um administrador autenticado no sistema
    Quando o administrador cadastra um novo funcionário com os seguintes dados:
      | name     | Operador SLA               |
      | email    | operador_sla@garage.com.br |
      | document | 86266070087                |
      | role     | EMPLOYEE                   |
      | password | StrongP@ss2026             |
    Então o código de status HTTP da resposta deve ser 201
    E o tempo de resposta da requisição deve ser inferior a 3000 ms
