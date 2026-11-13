# language: pt
@e2e @oficina @producao @reparo
Funcionalidade: Gerenciamento da Fila de Oficina e Execução de Reparos
  Como mecânico responsável e gestor da oficina
  Quero gerenciar a fila de execução veicular e registrar as transições de reparo
  Para garantir a rastreabilidade do ciclo de manutenção e notificar a conclusão ao ecossistema

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço e Mecânico Autenticado
  # ============================================================================
  Contexto:
    Dado que o serviço de Execução da Oficina está em execução e operacional
    E que o mecânico responsável está devidamente autenticado com perfil "EMPLOYEE"

  # ============================================================================
  # Cenário 1: Ciclo de Manutenção da Fila à Conclusão
  # ============================================================================
  @ciclo-reparo
  Cenário: Ciclo completo de reparo do enfileiramento até a finalização técnica
    # --- 1. Entrada na Fila de Execução da Oficina ---
    Quando uma ordem de execução é enfileirada para a Ordem de Serviço com o técnico "TECH-101"
    Então a ordem de execução deve ser criada com o status "QUEUED"

    # --- 2. Início do Reparo pelo Mecânico ---
    Quando o mecânico inicia a execução do reparo com status "IN_REPAIR"
    Então o status da execução deve ser atualizado para "IN_REPAIR"

    # --- 3. Finalização Técnica e Timestamp de Conclusão ---
    Quando o mecânico conclui a execução do reparo com status "COMPLETED"
    Então a ordem de execução deve ter o status "COMPLETED" e registrar a data de conclusão

  # ============================================================================
  # Cenário 2: Visualização da Fila Operacional da Oficina
  # ============================================================================
  @fila-operacional
  Cenário: Visualização da fila operacional de veículos na oficina
    Quando uma ordem de execução é enfileirada para a Ordem de Serviço com o técnico "TECH-101"
    E o supervisor consulta a fila de execução da oficina
    Então a fila deve listar a ordem de execução com paginação e status ativo

  # ============================================================================
  # Cenário 3: Registro de Impedimento Técnico e Falha
  # ============================================================================
  @falha-tecnica
  Cenário: Registro de impedimento técnico com encerramento por falha no reparo
    Quando uma ordem de execução é enfileirada para a Ordem de Serviço com o técnico "TECH-101"
    E o mecânico inicia a execução do reparo com status "IN_REPAIR"
    E o mecânico registra uma interrupção técnica com status "FAILED" e justificativa "Impossibilidade de reparo: bloco do motor trincado"
    Então a ordem de execução deve ter o status "FAILED" e registrar a data de conclusão

  # ============================================================================
  # Cenário 4: Imutabilidade Pós-Finalização (Regra de Negócio Defensiva)
  # ============================================================================
  @imutabilidade-finalizacao
  Cenário: Bloqueio de alteração de status em ordem de execução já finalizada
    Quando uma ordem de execução é enfileirada para a Ordem de Serviço com o técnico "TECH-101"
    E o mecânico conclui a execução do reparo com status "COMPLETED"
    E o mecânico tenta atualizar o status da execução para "IN_REPAIR"
    Então o sistema deve rejeitar a operação informando violação de regra de negócio com código 400
