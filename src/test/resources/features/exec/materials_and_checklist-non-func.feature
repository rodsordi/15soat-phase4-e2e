#language: pt
@e2e @nao-funcional @security @performance @exec @iso-25010
Funcionalidade: Requisitos Não-Funcionais - Apontamento de Materiais e Checklist Dinâmico
  Como auditor de qualidade de serviços mecânicos
  Quero garantir que checklists técnicos e baixas de materiais sejam restritos exclusivamente a colaboradores autorizados
  Para evitar manipulação indevida de dados operacionais e garantir alta performance em bancada

  # ============================================================================
  # Cenário NF-1: Rejeição de Acesso Sem Autenticação (Segurança)
  # ============================================================================
  @seguranca @autenticacao @missing-token
  Cenário: Bloqueio de inserção de itens de checklist sem token de autenticação
    Dado que a requisição não possui cabeçalho de autenticação
    Quando uma requisição "POST" for enviada para a API "exec" no endpoint "/api/v1/executions/00000000-0000-0000-0000-000000000000/checklist"
    Então o código de status HTTP da resposta deve ser 401

  # ============================================================================
  # Cenário NF-2: Bloqueio de Acesso para Perfil Cliente (Segurança - RBAC)
  # ============================================================================
  @seguranca @rbac @customer-forbidden
  Cenário: Cliente é proibido de registrar itens de checklist técnico da oficina
    Dado que a requisição é autenticada com perfil "CUSTOMER"
    Quando uma requisição "POST" for enviada para a API "exec" no endpoint "/api/v1/executions/00000000-0000-0000-0000-000000000000/checklist"
    Então o código de status HTTP da resposta deve ser 403

  # ============================================================================
  # Cenário NF-3: SLA de Performance no Registro de Checklist
  # ============================================================================
  @performance @sla
  Cenário: Cumprimento de SLA no apontamento de item de checklist dinâmico
    Dado que o serviço de Execução da Oficina está em execução e operacional
    E que o mecânico responsável está devidamente autenticado com perfil "EMPLOYEE"
    E uma ordem de execução é enfileirada para a Ordem de Serviço com o técnico "TECH-101"
    Quando o mecânico registra os seguintes itens de checklist:
      | task                      | completed |
      | Verificar fluido de freio | true      |
    Então os itens de checklist devem ser persistidos com sucesso no MongoDB
    E o tempo de resposta da requisição deve ser inferior a 2500 ms
