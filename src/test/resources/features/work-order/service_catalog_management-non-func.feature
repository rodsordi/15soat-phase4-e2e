#language: pt
@e2e @nao-funcional @security @performance @work-order @catalogo @iso-25010
Funcionalidade: Requisitos Não-Funcionais - Catálogo de Serviços e Mão de Obra
  Como diretor de operações da oficina
  Quero assegurar que a definição da tabela de serviços e valor/hora seja restrita exclusivamente a administradores
  Para impedir alterações não autorizadas na precificação e garantir estabilidade operacional

  # ============================================================================
  # Cenário NF-1: Rejeição de Acesso Sem Autenticação (Segurança)
  # ============================================================================
  @seguranca @autenticacao @missing-token
  Cenário: Bloqueio de cadastro de serviço sem token de autenticação
    Dado que a requisição não possui cabeçalho de autenticação
    Quando uma requisição "POST" for enviada para a API "work-order" no endpoint "/api/v1/services"
    Então o código de status HTTP da resposta deve ser 401

  # ============================================================================
  # Cenário NF-2: Bloqueio de Acesso para Perfil Cliente (Segurança - RBAC)
  # ============================================================================
  @seguranca @rbac @customer-forbidden
  Cenário: Cliente é proibido de cadastrar ou editar serviços no catálogo
    Dado que a requisição é autenticada com perfil "CUSTOMER"
    Quando uma requisição "POST" for enviada para a API "work-order" no endpoint "/api/v1/services"
    Então o código de status HTTP da resposta deve ser 403

  # ============================================================================
  # Cenário NF-3: SLA de Performance no Cadastro de Serviço
  # ============================================================================
  @performance @sla
  Cenário: Cumprimento do SLA de resposta no cadastro de serviço de oficina
    Dado que o administrador da oficina está devidamente autenticado com perfil "ADMIN"
    Dado o serviço com os seguintes dados:
      | code  | SRV-SLA-01             |
      | name  | Alinhamento a Laser 3D |
      | price | 180.00                 |
    Quando o serviço é cadastrado no catálogo
    Então o serviço deve ser registrado com sucesso no catálogo
    E o tempo de resposta da requisição deve ser inferior a 2000 ms
