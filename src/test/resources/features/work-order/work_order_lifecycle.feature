# language: pt
@e2e @ordem-de-servico
Funcionalidade: Ciclo de Vida Completo da Ordem de Serviço
  Como atendente e mecânico da oficina mecânica
  Quero gerenciar o fluxo operacional completo das ordens de serviço
  Para que o veículo seja reparado com qualidade, notificado com transparência e liberado com métricas consolidadas

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço de Ordens de Serviço
  # ============================================================================
  Contexto:
    Dado que o serviço de Ordem de Serviço está em execução e operacional
    E um cliente cadastrado com documento "52998224725" e veículo "BRA2E19"

  # ============================================================================
  # Fluxo Principal: Ciclo de Vida Completo da Ordem de Serviço (7 Fases)
  # ============================================================================
  @ciclo-completo
  Cenário: Ciclo de vida completo da ordem de serviço da recepção até a liberação final
    # --- 1. Recepção do Veículo e Abertura da Ordem de Serviço (RECEIVED) ---
    Quando o cliente com documento "52998224725" solicita uma Ordem de Serviço para o veículo "BRA2E19" com a descrição "Troca de óleo e revisão de freios"
    Então a Ordem de Serviço deve ser criada com o status "RECEIVED"
    E a consulta da Ordem de Serviço por ID deve retornar o documento "52998224725" e a placa "BRA2E19"

    # --- 2. Diagnóstico Técnico pelo Mecânico (DIAGNOSING) ---
    Quando o mecânico inicia o diagnóstico da Ordem de Serviço
    Então o status da Ordem de Serviço deve ser atualizado para "DIAGNOSING"

    # --- 3. Conclusão do Orçamento e Notificação ao Cliente (WAITING_FOR_APPROVAL) ---
    Quando o diagnóstico é concluído e aguarda aprovação do cliente com valor total de 450.00
    Então o status da Ordem de Serviço deve ser atualizado para "WAITING_FOR_APPROVAL"

    # --- 4. Aprovação do Orçamento e Autorização de Produção (APPROVED) ---
    Quando o gestor da oficina atualiza o status da Ordem de Serviço para "APPROVED" com valor total de 450.00
    Então o status da Ordem de Serviço deve ser atualizado para "APPROVED"

    # --- 5. Execução dos Reparos e Finalização dos Serviços (FINISHED) ---
    Quando todos os reparos técnicos são concluídos na oficina
    Então o status da Ordem de Serviço deve ser atualizado para "FINISHED"

    # --- 6. Retirada e Liberação do Veículo ao Cliente (RELEASED) ---
    Quando o veículo é liberado para o cliente
    Então o status da Ordem de Serviço deve ser atualizado para "RELEASED"

    # --- 7. Fechamento de Métricas e Tempo Médio de Execução ---
    Quando a rotina de tempo médio de execução é acionada
    Então o tempo médio do serviço deve ser apurado com sucesso
