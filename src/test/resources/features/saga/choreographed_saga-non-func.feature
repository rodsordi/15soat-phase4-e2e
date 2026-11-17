#language: pt
@e2e @nao-funcional @resiliencia @observabilidade @performance @saga @kafka @iso-25010
Funcionalidade: Requisitos Não-Funcionais - Coreografia da Saga Distribuída
  Como arquiteto de sistemas distribuídos
  Quero validar a resiliência assíncrona, a idempotência a reprocessamento e o rastreamento ponta a ponta da Saga
  Para assegurar consistência eventual entre Work Order, Billing e Execução sem inconsistência de estado

  # ============================================================================
  # Cenário NF-1: Rastreabilidade Distribuída Ponta a Ponta (Observabilidade & SLA)
  # ============================================================================
  @observabilidade @tracing @sla
  Cenário: Preservação do identificador de correlação e rastreabilidade na Saga
    Dado que todos os microsserviços da plataforma estão em execução e operacionais
    E que o atendente da oficina está devidamente autenticado com perfil "EMPLOYEE"
    Quando o atendente abre uma nova Ordem de Serviço para o veículo "BRA2E19"
    Então o código de status HTTP da resposta deve ser 201
    E o tempo de resposta da requisição deve ser inferior a 2500 ms
    E a resposta deve conter o cabeçalho de rastreabilidade "X-Trace-Id"

  # ============================================================================
  # Cenário NF-2: Proteção de Estado sob Tentativa Espúria em Saga Compensada
  # ============================================================================
  @resiliencia @idempotencia @estado-imutavel
  Cenário: Proteção de integridade sob tentativa de transição em Ordem de Serviço compensada
    Dado que todos os microsserviços da plataforma estão em execução e operacionais
    E que o atendente da oficina está devidamente autenticado com perfil "EMPLOYEE"
    Dado que a Ordem de Serviço foi previamente compensada com fatura estornada
    Quando uma tentativa de transição na Ordem de Serviço já cancelada é submetida
    Então a tentativa deve ser rejeitada com código 400
