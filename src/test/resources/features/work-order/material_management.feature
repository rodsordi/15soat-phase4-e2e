# language: pt
@e2e @material @estoque @precificacao @crud
Funcionalidade: Gestão e Precificação de Materiais de Estoque
  Como operador de estoque e atendente de ordem de serviço
  Quero cadastrar, precificar e gerenciar peças e insumos
  Para garantir disponibilidade de itens e correta orçamentação das ordens de serviço

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço e Operador Autenticado
  # ============================================================================
  Contexto:
    Dado que o serviço de Ordem de Serviço está em execução e operacional
    E que o operador de estoque está devidamente autenticado com perfil "EMPLOYEE"

  # ============================================================================
  # Cenário 1: Cadastro de Material de Estoque
  # ============================================================================
  @cadastro-material
  Cenário: Cadastro de material de estoque
    Dado o material com os seguintes dados de estoque e precificação:
      | sku         | PART-BRK-01                |
      | description | Pastilha de Freio Dianteira|
      | unitCost    | 45.00                      |
      | unitPrice   | 95.00                      |
      | quantity    | 20                         |
    Quando o material é cadastrado no catálogo
    Então o material deve ser persistido com status 201 no catálogo

  # ============================================================================
  # Cenário 2: Consulta de Material no Catálogo por Código
  # ============================================================================
  @consulta-material
  Cenário: Consulta de material no catálogo por código
    Dado o material com código "PART-BRK-01" cadastrado no catálogo
    Quando o material com código "PART-BRK-01" é consultado
    Então a consulta deve retornar os seguintes dados do material:
      | sku         | PART-BRK-01                 |
      | description | Pastilha de Freio Dianteira |
      | unitPrice   | 95.00                       |

  # ============================================================================
  # Cenário 3: Atualização de Preço de Venda do Material
  # ============================================================================
  @atualizacao-preco-material
  Cenário: Atualização de preço de venda de material
    Dado o material com código "PART-BRK-01" cadastrado no catálogo
    Quando o preço de venda do material "PART-BRK-01" é atualizado para 105.00
    Então o novo preço 105.00 deve ser refletido com sucesso na consulta do material

  # ============================================================================
  # Cenário 4: Tentativa de Cadastro de Material com SKU Duplicado (Sad Path)
  # ============================================================================
  @excecao @material-duplicado
  Cenário: Rejeição ao cadastrar material com SKU já existente
    Dado o material com código "PART-BRK-01" cadastrado no catálogo
    E o material com os seguintes dados de estoque e precificação:
      | sku         | PART-BRK-01                 |
      | description | Pastilha de Freio Repetida  |
      | unitCost    | 50.00                       |
      | unitPrice   | 100.00                      |
      | quantity    | 10                          |
    Quando o material é cadastrado no catálogo
    Então o erro retornado deve corresponder a:
      | status    | 409                                           |
      | errorCode | RESOURCE_ALREADY_EXISTS                       |
      | title     | Resource Conflict                             |
      | detail    | Material with SKU PART-BRK-01 already exists  |
