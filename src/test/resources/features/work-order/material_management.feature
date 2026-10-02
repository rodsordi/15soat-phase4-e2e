# language: pt
@e2e @material @estoque @precificacao @crud
Funcionalidade: Gestão e Precificação de Materiais de Estoque
  Como operador de estoque e atendente de ordem de serviço
  Quero cadastrar, precificar e gerenciar peças e insumos
  Para garantir disponibilidade de itens e correta orçamentação das ordens de serviço

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço de Ordens de Serviço
  # ============================================================================
  Contexto:
    Dado que o serviço de Ordem de Serviço está em execução e operacional

  # ============================================================================
  # Cenário 1: Cadastro e Consulta de Material de Estoque com Sucesso
  # ============================================================================
  @cadastro-material
  Cenário: Cadastro de novo material de estoque com preço e quantidade válidos
    Quando um novo material com código "PART-BRK-01", descrição "Pastilha de Freio Dianteira", custo 45.00, preço 95.00 e quantidade 20 é cadastrado
    Então o material deve ser persistido com status 201 no catálogo
    E os dados do material devem ser consultados com sucesso pelo código "PART-BRK-01"

  # ============================================================================
  # Cenário 2: Atualização de Preço de Venda do Material
  # ============================================================================
  @atualizacao-preco-material
  Cenário: Atualização de preço de venda de material existente com sucesso
    Dado que o material com código "PART-BRK-01" já existe no catálogo
    Quando o preço de venda do material "PART-BRK-01" é atualizado para 105.00
    Então o novo preço 105.00 deve ser refletido com sucesso na consulta do material
