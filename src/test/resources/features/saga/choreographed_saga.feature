# language: pt
@e2e @saga @coreografia
Funcionalidade: Coreografia Completa da Saga Distribuída
  Como plataforma distribuída da oficina mecânica
  Quero coordenar eventos assíncronos entre Ordem de Serviço, Faturamento e Execução
  Para garantir consistência eventual e execução de compensações distribuídas em caso de falhas

  # ============================================================================
  # Contexto: Validação Operacional dos Microsserviços e Colaborador Autenticado
  # ============================================================================
  Contexto:
    Dado que todos os microsserviços da plataforma estão em execução e operacionais
    E que o atendente da oficina está devidamente autenticado com perfil "EMPLOYEE"

  # ============================================================================
  # Cenário 1: Caminho Feliz - Ciclo Completo da Saga com Sucesso
  # ============================================================================
  @saga-sucesso @happy-path
  Cenário: Fluxo Principal - Ciclo completo da Saga da abertura à finalização do veículo
    # --- 1. Abertura da Ordem de Serviço na Recepção ---
    Quando o atendente abre uma nova Ordem de Serviço com os seguintes dados:
      | customerDocument | 52998224725              |
      | licensePlate     | BRA2E19                  |
      | description      | Manutenção geral da Saga |

    # --- 2. Diagnóstico e Orçamento Técnico ---
    E o mecânico inicia o diagnóstico da Ordem de Serviço
    E o mecânico conclui o diagnóstico e o faturamento gera o orçamento

    # --- 3. Decisão Soberana: Aprovação do Orçamento pelo Cliente ---
    E o próprio cliente aprova o orçamento da Ordem de Serviço

    # --- 4. Emissão da Fatura e Liquidação Mercado Pago ---
    E o serviço de Faturamento gera a fatura e o Mercado Pago confirma o pagamento

    # --- 5. Enfileiramento e Execução dos Reparos na Oficina ---
    E o serviço de Execução enfileira o veículo e conclui os reparos

    # --- 6. Sincronização Final da Ordem de Serviço ---
    Então a Ordem de Serviço deve refletir o status final "COMPLETED"

  # ============================================================================
  # Cenário 2: Rollback Compensatório por Recusa de Pagamento
  # ============================================================================
  @saga-compensacao @pagamento-recusado
  Cenário: Rollback Compensatório - Recusa de pagamento cancela a Ordem de Serviço
    # --- 1. Abertura da Ordem de Serviço na Recepção ---
    Quando o atendente abre uma nova Ordem de Serviço com os seguintes dados:
      | customerDocument | 52998224725                        |
      | licensePlate     | ABC1D23                            |
      | description      | Revisão com falha de processamento |

    # --- 2. Diagnóstico e Orçamento Técnico ---
    E o mecânico inicia o diagnóstico da Ordem de Serviço
    E o mecânico conclui o diagnóstico e o faturamento gera o orçamento

    # --- 3. Decisão Soberana: Aprovação do Orçamento pelo Cliente ---
    E o próprio cliente aprova o orçamento da Ordem de Serviço

    # --- 4. Gateway de Pagamento Recusa a Transação ---
    E o gateway de pagamento recusa a transação de pagamento

    # --- 5. Ação Compensatória Distribuída (Saga Rollback) ---
    Então a Saga distribuída deve executar a compensação e marcar a Ordem de Serviço como "CANCELED"

  # ============================================================================
  # Cenário 3: Rollback Compensatório com Estorno Financeiro por Falha na Oficina
  # ============================================================================
  @saga-compensacao-reparo @estorno-financeiro
  Cenário: Rollback Compensatório - Falha técnica na oficina estorna o pagamento e cancela a OS
    # --- 1. Fluxo Inicial até Pagamento Aprovado ---
    Quando o atendente abre uma nova Ordem de Serviço com os seguintes dados:
      | customerDocument | 52998224725                         |
      | licensePlate     | BRA2E19                             |
      | description      | Reparo com impedimento técnico real |
    E o mecânico inicia o diagnóstico da Ordem de Serviço
    E o mecânico conclui o diagnóstico e o faturamento gera o orçamento
    E o próprio cliente aprova o orçamento da Ordem de Serviço
    E o serviço de Faturamento gera a fatura e o Mercado Pago confirma o pagamento

    # --- 2. Início do Reparo e Registro de Falha Irrecuperável ---
    Quando o mecânico registra falha técnica irrecuperável no reparo da oficina com justificativa "Impossibilidade de reparo: bloco do motor trincado"

    # --- 3. Transação Compensatória (Saga Rollback) ---
    Então o sistema de faturamento deve processar o estorno financeiro no Mercado Pago e marcar a fatura como "REFUNDED"
    E a Saga distribuída deve executar a compensação e marcar a Ordem de Serviço como "CANCELED"

  # ============================================================================
  # Cenário 4: Idempotência da Ação Compensatória perante Notificações Repetidas
  # ============================================================================
  @saga-idempotencia @resiliencia
  Cenário: Idempotência do estorno compensatório perante eventos repetidos
    Dado que a Ordem de Serviço foi previamente compensada com fatura estornada
    Quando uma tentativa de transição na Ordem de Serviço já cancelada é submetida
    Então a tentativa deve ser rejeitada com código 400
    E o estado compensado deve permanecer inalterado como fatura "REFUNDED" e Ordem de Serviço "CANCELED"
