#language: pt
@e2e @nao-funcional @security @rbac @performance @observabilidade @work-order @iso-25010
Funcionalidade: Requisitos Não-Funcionais - Ciclo de Vida da Ordem de Serviço
  Como arquiteto corporativo de segurança e governança
  Quero validar a segregação estrita de funções (SoD), a soberania do cliente na aprovação e os SLAs do ciclo de vida da OS
  Para impedir conflito de interesses na oficina mecânica e garantir rastreabilidade completa das operações

  # ============================================================================
  # Cenário NF-1: Rejeição de Abertura de Ordem de Serviço Sem Autenticação
  # ============================================================================
  @seguranca @autenticacao @missing-token
  Cenário: Bloqueio de abertura de OS para requisição anônima sem token
    Dado que a requisição não possui cabeçalho de autenticação
    Quando uma requisição "POST" for enviada para a API "work-order" no endpoint "/api/v1/work-orders"
    Então o código de status HTTP da resposta deve ser 401

  # ============================================================================
  # Cenário NF-2: Bloqueio de Aprovação de Orçamento por Funcionário (SoD / RBAC Soberano)
  # ============================================================================
  @seguranca @rbac @soberania-cliente @employee-forbidden
  Cenário: Funcionário da oficina é proibido de aprovar orçamento em nome do cliente
    Dado que a requisição é autenticada com perfil "EMPLOYEE"
    Quando uma requisição "PATCH" for enviada para a API "work-order" no endpoint "/api/v1/work-orders/00000000-0000-0000-0000-000000000000"
    Então o código de status HTTP da resposta deve ser 403

  # ============================================================================
  # Cenário NF-3: Bloqueio de Abertura de OS Diretamente por Perfil Cliente (RBAC)
  # ============================================================================
  @seguranca @rbac @customer-forbidden
  Cenário: Cliente é proibido de registrar abertura direta de OS sem passar pela recepção
    Dado que a requisição é autenticada com perfil "CUSTOMER"
    Quando uma requisição "POST" for enviada para a API "work-order" no endpoint "/api/v1/work-orders"
    Então o código de status HTTP da resposta deve ser 403

  # ============================================================================
  # Cenário NF-4: SLA de Abertura de OS e Rastreabilidade Distribuída (Performance & Tracing)
  # ============================================================================
  @performance @sla @observabilidade
  Cenário: SLA de abertura de Ordem de Serviço com cabeçalho de correlação X-Trace-Id
    Dado que o atendente da oficina está devidamente autenticado com perfil "EMPLOYEE"
    Quando o atendente abre uma nova Ordem de Serviço para o cliente "52998224725" e veículo "BRA2E19" com a descrição "Troca de pastilhas de freio e alinhamento"
    Então o código de status HTTP da resposta deve ser 201
    E o tempo de resposta da requisição deve ser inferior a 2000 ms
    E a resposta deve conter o cabeçalho de rastreabilidade "X-Trace-Id"
