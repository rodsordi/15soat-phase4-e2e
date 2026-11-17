#language: pt
@e2e @nao-funcional @security @rbac @performance @observabilidade @billing @estimate @iso-25010
Funcionalidade: Requisitos Não-Funcionais - Ciclo de Vida do Orçamento no Faturamento
  Como arquiteto corporativo de segurança e observabilidade
  Quero validar a soberania do cliente (SoD / RBAC), proteção de rotas e SLAs na aprovação do orçamento
  Para assegurar a conformidade regulatória, prevenir conflito de interesses e garantir rastreabilidade completa

  # ============================================================================
  # Cenário NF-1: Rejeição de Consulta/Aprovação de Orçamento Sem Autenticação
  # ============================================================================
  @seguranca @autenticacao @missing-token
  Cenário: Bloqueio de aprovação de orçamento para requisição anônima sem token
    Dado que a requisição não possui cabeçalho de autenticação
    Quando uma requisição "PATCH" for enviada para a API "billing" no endpoint "/api/v1/estimates/work-order/00000000-0000-0000-0000-000000000000"
    Então o código de status HTTP da resposta deve ser 401

  # ============================================================================
  # Cenário NF-2: Bloqueio de Aprovação de Orçamento por Funcionário (SoD / RBAC Soberano)
  # ============================================================================
  @seguranca @rbac @soberania-cliente @employee-forbidden
  Cenário: Funcionário da oficina é proibido de aprovar orçamento na API Billing em nome do cliente
    Dado que a requisição é autenticada com perfil "EMPLOYEE"
    Quando uma requisição "PATCH" for enviada para a API "billing" no endpoint "/api/v1/estimates/work-order/00000000-0000-0000-0000-000000000000"
    Então o código de status HTTP da resposta deve ser 403

  # ============================================================================
  # Cenário NF-3: SLA de Consulta de Orçamento e Rastreabilidade Distribuída (Performance & Tracing)
  # ============================================================================
  @performance @sla @observabilidade
  Cenário: SLA de consulta de orçamento com cabeçalho de correlação X-Trace-Id
    Dado que o atendente abre uma nova Ordem de Serviço para o cliente "52998224725" e veículo "BRA2E19" com a descrição "Revisão geral"
    E o mecânico inicia o diagnóstico da Ordem de Serviço
    E o mecânico conclui o diagnóstico e o faturamento gera o orçamento
    Quando o cliente consulta o orçamento da Ordem de Serviço na API Billing
    Então o código de status HTTP da resposta deve ser 200
    E o tempo de resposta da requisição deve ser inferior a 2000 ms
    E a resposta deve conter o cabeçalho de rastreabilidade "X-Trace-Id"
