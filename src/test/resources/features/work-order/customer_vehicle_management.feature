# language: pt
@e2e @cliente @veiculo @ordem-de-servico
Funcionalidade: Gestão de Clientes e Veículos Vinculados
  Como atendente da oficina mecânica
  Quero cadastrar e consultar clientes e seus respectivos veículos
  Para manter a base cadastral atualizada e viabilizar a abertura de ordens de serviço

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço de Ordens de Serviço
  # ============================================================================
  Contexto:
    Dado que o serviço de Ordem de Serviço está em execução e operacional

  # ============================================================================
  # Cenário 1: Cadastro e Consulta de Cliente
  # ============================================================================
  @cadastro-cliente
  Cenário: Cadastro de cliente por documento único e validação cadastral
    # --- 1. Cadastro do Cliente ---
    Quando um novo cliente é cadastrado com documento "52998224725", nome "Rodrigo Sordi" e email "rodrigo@fiap.com.br"
    Então o cliente deve ser registrado com sucesso

    # --- 2. Consulta dos Dados Cadastrais ---
    E a consulta de cliente por documento "52998224725" deve retornar o nome "Rodrigo Sordi"

  # ============================================================================
  # Cenário 2: Cadastro e Consulta de Veículo Vinculado ao Cliente
  # ============================================================================
  @cadastro-veiculo
  Cenário: Cadastro de veículo associado ao cliente com placa única
    # --- 1. Cadastro de Veículo Vinculado ---
    Quando um novo veículo com placa "BRA2E19", marca "Toyota", modelo "Corolla" e ano 2023 é vinculado ao cliente "52998224725"
    Então o veículo deve ser registrado com sucesso

    # --- 2. Consulta de Veículo por Placa ---
    E a consulta de veículo pela placa "BRA2E19" deve confirmar o vínculo com o cliente "52998224725"
