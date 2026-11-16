#language: pt
@e2e @nao-funcional @security @performance @observabilidade @work-order @iso-25010
Funcionalidade: Requisitos Não-Funcionais - Gestão de Clientes e Veículos
  Como auditor de segurança e desempenho da API Work Order
  Quero garantir o bloqueio de requisições anônimas ou com papéis inadequados e verificar o tempo de resposta das consultas
  Para assegurar a integridade dos dados cadastrais e a melhor experiência de atendimento

  # ============================================================================
  # Cenário NF-1: Bloqueio de Acesso Sem Autenticação (Segurança - Migrado da Feature Funcional)
  # ============================================================================
  @seguranca @autenticacao @missing-token
  Cenário: Tentativa de cadastro de cliente por operador não autenticado é negada com HTTP 401
    Dado que o operador não possui token de autenticação válido
    Quando o cliente com documento "52998224725", nome "Anônimo" e email "anonimo@fiap.com.br" tenta ser cadastrado
    Então o código de status HTTP da resposta deve ser 401

  # ============================================================================
  # Cenário NF-2: Bloqueio de Acesso para Perfil Cliente (Segurança - RBAC)
  # ============================================================================
  @seguranca @rbac @customer-forbidden
  Cenário: Cliente é proibido de cadastrar clientes ou veículos na recepção da oficina
    Dado que a requisição é autenticada com perfil "CUSTOMER"
    Quando uma requisição "POST" for enviada para a API "work-order" no endpoint "/api/v1/customers"
    Então o código de status HTTP da resposta deve ser 403

  # ============================================================================
  # Cenário NF-3: SLA de Performance e Rastreabilidade Distribuída no Cadastro de Veículo
  # ============================================================================
  @performance @sla @observabilidade
  Cenário: SLA de consulta de veículo com cabeçalho de rastreabilidade
    Dado um cliente cadastrado com documento "52998224725" e veículo "BRA2E19"
    Quando o veículo com placa "BRA2E19" é consultado
    Então o código de status HTTP da resposta deve ser 200
    E o tempo de resposta da requisição deve ser inferior a 2000 ms
    E a resposta deve conter o cabeçalho de rastreabilidade "X-Trace-Id"
