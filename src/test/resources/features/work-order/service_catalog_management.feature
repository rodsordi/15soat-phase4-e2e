# language: pt
@e2e @servico @catalogo @crud
Funcionalidade: Gestão do Catálogo de Serviços
  Como administrador da oficina
  Quero cadastrar e consultar serviços do catálogo da oficina
  Para precificar e padronizar as manutenções solicitadas nas ordens de serviço

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço de Ordens de Serviço
  # ============================================================================
  Contexto:
    Dado que o serviço de Ordem de Serviço está em execução e operacional

  # ============================================================================
  # Cenário 1: Cadastro e Consulta de Serviço no Catálogo
  # ============================================================================
  @cadastro-servico
  Cenário: Cadastro de novo serviço no catálogo da oficina com sucesso
    Quando um novo serviço com código "SRV-OIL-01", nome "Troca de Óleo e Filtro" e preço 150.00 é cadastrado
    Então o serviço deve ser registrado com sucesso no catálogo
    E a consulta do serviço pelo código "SRV-OIL-01" deve retornar o preço 150.00
