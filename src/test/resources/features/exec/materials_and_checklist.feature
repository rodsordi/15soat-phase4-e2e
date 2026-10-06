# language: pt
@e2e @material @checklist @oficina
Funcionalidade: Gestão de Materiais de Estoque e Checklists Técnicos
  Como mecânico e chefe de oficina
  Quero registrar insumos de manutenção e validar os itens de inspeção veicular
  Para garantir controle de peças utilizadas e qualidade nos procedimentos da oficina

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço e Mecânico Autenticado
  # ============================================================================
  Contexto:
    Dado que o serviço de Execução da Oficina está em execução e operacional
    E que o mecânico responsável está devidamente autenticado com perfil "EMPLOYEE"
    E uma ordem de execução criada com identificador válido

  # ============================================================================
  # Cenário 1: Cadastro e Vínculo de Material / Peça de Estoque
  # ============================================================================
  @cadastro-material
  Cenário: Registro de material e insumo utilizado no reparo
    Quando o material com os seguintes dados é registrado na manutenção:
      | materialCode | PART-BRK-01                |
      | description  | Pastilha de Freio Dianteira|
      | quantity     | 2                          |
    Então o material deve ser computado com sucesso na manutenção

  # ============================================================================
  # Cenário 2: Preenchimento de Checklist de Inspeção
  # ============================================================================
  @checklist-inspecao
  Cenário: Preenchimento e validação de checklist de inspeção veicular
    Quando o mecânico registra os seguintes itens de checklist:
      | task                                | completed |
      | Verificar fluido de freio           | true      |
      | Inspecionar suspensão e amortecedor | true      |
    Então os itens de checklist devem ser persistidos com sucesso no MongoDB
