#language: pt
@e2e @nao-funcional @security @performance @observabilidade @billing @iso-25010
Funcionalidade: Requisitos Não-Funcionais - Faturamento e Geração de Faturas
  Como arquiteto de segurança e observabilidade
  Quero validar o controle de acesso RBAC, rastreabilidade distribuída e SLAs de emissão de faturas
  Para assegurar a conformidade financeira, auditoria e resiliência do microsserviço de cobrança

  # ============================================================================
  # Cenário NF-1: Bloqueio de Acesso Sem Autenticação (Segurança - Migrado da Feature Funcional)
  # ============================================================================
  @seguranca @autenticacao @missing-token
  Cenário: Bloqueio de acesso para requisição sem token de autenticação
    Dado que a requisição não possui cabeçalho de autenticação
    Quando uma tentativa de emissão de fatura é submetida
    Então o código de status HTTP da resposta deve ser 401

  # ============================================================================
  # Cenário NF-2: Controle de Acesso Baseado em Papéis - RBAC (Segurança)
  # ============================================================================
  @seguranca @rbac @customer-forbidden
  Cenário: Cliente é proibido de emitir faturas administrativas de balcão diretamente
    Dado que a requisição é autenticada com perfil "CUSTOMER"
    Quando uma requisição "POST" for enviada para a API "billing" no endpoint "/api/v1/invoices"
    Então o código de status HTTP da resposta deve ser 403

  # ============================================================================
  # Cenário NF-3: SLA de Performance e Rastreabilidade Distribuída no Checkout Pix
  # ============================================================================
  @performance @sla @observabilidade @pix
  Cenário: SLA de latência e cabeçalho de rastreabilidade no checkout Pix
    Dado uma fatura criada para a Ordem de Serviço com o valor 450.00 e cliente "52998224725"
    Quando uma requisição de inicialização de checkout é enviada para a fatura
    Então o código de status HTTP da resposta deve ser 200
    E o tempo de resposta da requisição deve ser inferior a 2500 ms
    E a resposta deve conter o cabeçalho de rastreabilidade "X-Trace-Id"
