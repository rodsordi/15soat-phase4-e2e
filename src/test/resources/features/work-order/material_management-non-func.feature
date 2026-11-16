#language: pt
@e2e @nao-funcional @security @performance @work-order @estoque @iso-25010
Funcionalidade: Requisitos Não-Funcionais - Gestão de Materiais e Estoque de Peças
  Como gerente de suprimentos e segurança
  Quero garantir que a gestão de inventário e precificação de peças seja restrita ao perfil operador de estoque
  Para evitar fraude ou adulteração de preços de peças por usuários externos ou clientes

  # ============================================================================
  # Cenário NF-1: Rejeição de Acesso Sem Autenticação (Segurança)
  # ============================================================================
  @seguranca @autenticacao @missing-token
  Cenário: Bloqueio de cadastro de peças sem token de autenticação
    Dado que a requisição não possui cabeçalho de autenticação
    Quando uma requisição "POST" for enviada para a API "work-order" no endpoint "/api/v1/materials"
    Então o código de status HTTP da resposta deve ser 401

  # ============================================================================
  # Cenário NF-2: Bloqueio de Acesso para Perfil Cliente (Segurança - RBAC)
  # ============================================================================
  @seguranca @rbac @customer-forbidden
  Cenário: Cliente é proibido de cadastrar peças ou alterar preços no estoque
    Dado que a requisição é autenticada com perfil "CUSTOMER"
    Quando uma requisição "POST" for enviada para a API "work-order" no endpoint "/api/v1/materials"
    Então o código de status HTTP da resposta deve ser 403

  # ============================================================================
  # Cenário NF-3: SLA de Performance no Cadastro de Peças
  # ============================================================================
  @performance @sla
  Cenário: Cumprimento do SLA de resposta no cadastro de material de reposição
    Dado que o operador de estoque está devidamente autenticado com perfil "EMPLOYEE"
    Dado o material com os seguintes dados de estoque e precificação:
      | sku         | PART-SLA-01                |
      | description | Filtro de Ar de Performance|
      | unitCost    | 40.00                      |
      | unitPrice   | 85.00                      |
      | quantity    | 25                         |
    Quando o material é cadastrado no catálogo
    Então o material deve ser persistido com status 201 no catálogo
    E o tempo de resposta da requisição deve ser inferior a 2000 ms
