# language: pt
@e2e @material @checklist @oficina
Funcionalidade: Gestão de Materiais de Estoque e Checklists Técnicos
  Como mecânico e chefe de oficina
  Quero registrar insumos de manutenção e validar os itens de inspeção veicular
  Para garantir controle de peças utilizadas e qualidade nos procedimentos da oficina

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço de Execução
  # ============================================================================
  Contexto:
    Dado que o serviço de Execução da Oficina está em execução e operacional
    E uma ordem de execução criada com identificador válido

  # ============================================================================
  # Cenário 1: Cadastro e Vínculo de Material / Peça de Estoque
  # ============================================================================
  @cadastro-material
  Cenário: Registro de material e insumo utilizado no reparo
    Quando um novo material com código "PART-BRK-01", descrição "Pastilha de Freio Dianteira" e quantidade 2 é registrado
    Então o material deve ser computado com sucesso na manutenção

  # ============================================================================
  # Cenário 2: Preenchimento de Checklist de Inspeção
  # ============================================================================
  @checklist-inspecao
  Cenário: Preenchimento e validação de checklist de inspeção veicular
    Quando o mecânico registra o item de checklist "Verificar fluido de freio" como concluído
    E o mecânico registra o item de checklist "Inspecionar suspensão e amortecedores" como concluído
    Então os itens de checklist devem ser persistidos com sucesso no MongoDB
