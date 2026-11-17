#language: pt
@e2e @nao-funcional @security @performance @observabilidade @exec @iso-25010
Funcionalidade: Requisitos Não-Funcionais - Fila de Execução e Reparo Mecânico
  Como engenheiro de operações de oficina
  Quero garantir a proteção da fila de trabalho da oficina contra acessos externos indevidos e auditar a performance
  Para que mecânicos acessem a fila com baixa latência e clientes não visualizem a ordem interna de produção

  # ============================================================================
  # Cenário NF-1: Rejeição de Acesso Sem Autenticação (Segurança)
  # ============================================================================
  @seguranca @autenticacao @missing-token
  Cenário: Bloqueio de acesso para requisição sem autenticação à fila de execução
    Dado que a requisição não possui cabeçalho de autenticação
    Quando uma requisição "GET" for enviada para a API "exec" no endpoint "/api/v1/executions/queue"
    Então o código de status HTTP da resposta deve ser 401

  # ============================================================================
  # Cenário NF-2: Bloqueio de Acesso para Perfil Cliente (Segurança - RBAC)
  # ============================================================================
  @seguranca @rbac @customer-forbidden
  Cenário: Cliente é proibido de consultar a fila técnica de reparos dos mecânicos
    Dado que a requisição é autenticada com perfil "CUSTOMER"
    Quando uma requisição "GET" for enviada para a API "exec" no endpoint "/api/v1/executions/queue"
    Então o código de status HTTP da resposta deve ser 403

  # ============================================================================
  # Cenário NF-3: SLA de Performance e Rastreabilidade Distribuída na Fila de Execução
  # ============================================================================
  @performance @sla @observabilidade
  Cenário: SLA de consulta à fila de execução com cabeçalho de rastreabilidade
    Dado que o serviço de Execução da Oficina está em execução e operacional
    E que o mecânico responsável está devidamente autenticado com perfil "EMPLOYEE"
    Quando o supervisor consulta a fila de execução da oficina
    Então o código de status HTTP da resposta deve ser 200
    E o tempo de resposta da requisição deve ser inferior a 2500 ms
    E a resposta deve conter o cabeçalho de rastreabilidade "X-Trace-Id"
