# language: pt
@e2e @oficina @producao @reparo
Funcionalidade: Gerenciamento da Fila de Oficina e Execução de Reparos
  Como mecânico responsável e gestor da oficina
  Quero gerenciar a fila de execução veicular e registrar as transições de reparo
  Para garantir a rastreabilidade do ciclo de manutenção e notificar a conclusão ao ecossistema

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço de Execução
  # ============================================================================
  Contexto:
    Dado que o serviço de Execução da Oficina está em execução e operacional

  # ============================================================================
  # Cenário 1: Ciclo de Manutenção da Fila à Conclusão
  # ============================================================================
  @ciclo-reparo
  Cenário: Ciclo completo de reparo do enfileiramento até a finalização técnica
    # --- 1. Entrada na Fila de Execução da Oficina ---
    Quando uma ordem de execução é enfileirada para a Ordem de Serviço com o técnico "TECH-101"
    Então a ordem de execução deve ser criada com o status "QUEUED"

    # --- 2. Início do Reparo pelo Mecânico ---
    Quando atualiza o status da execução para "IN_REPAIR"
    Então o status da execução deve ser atualizado para "IN_REPAIR"

    # --- 3. Finalização Técnica e Timestamp de Conclusão ---
    Quando finaliza atualizando o status da execução para "COMPLETED"
    Então a ordem de execução deve ter o status "COMPLETED" e registrar a data de conclusão
