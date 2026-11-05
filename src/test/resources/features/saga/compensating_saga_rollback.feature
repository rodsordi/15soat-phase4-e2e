# language: pt
@e2e @saga @compensacao @rollback
Funcionalidade: Transação Compensatória e Rollback da Saga Distribuída
  Como plataforma distribuída da oficina mecânica
  Quero executar rollback coordenado em caso de falha de liquidação de pagamento
  Para garantir consistência eventual e evitar ordens de serviço órfãs ou em estado inconsistente

  # ============================================================================
  # Contexto: Validação Operacional dos Microsserviços e Colaborador Autenticado
  # ============================================================================
  Contexto:
    Dado que todos os microsserviços da plataforma estão em execução e operacionais
    E que o atendente da oficina está devidamente autenticado com perfil "EMPLOYEE"

  # ============================================================================
  # Cenário 1: Rollback Compensatório por Recusa de Pagamento
  # ============================================================================
  @saga-compensacao
  Cenário: Rollback Compensatório - Recusa de pagamento cancela a Ordem de Serviço
    # --- 1. Abertura da Ordem de Serviço na Recepção ---
    Quando o atendente abre uma nova Ordem de Serviço com os seguintes dados:
      | customerDocument | 52998224725                        |
      | licensePlate     | ABC1D23                            |
      | description      | Revisão com falha de processamento |

    # --- 2. Diagnóstico e Orçamento Técnico ---
    E o mecânico inicia o diagnóstico da Ordem de Serviço
    E o mecânico conclui o diagnóstico e o faturamento gera o orçamento

    # --- 3. Aprovação do Orçamento ---
    E o gestor da oficina aprova o orçamento da Ordem de Serviço

    # --- 4. Gateway de Pagamento Recusa a Transação ---
    E o gateway de pagamento recusa a transação de pagamento

    # --- 5. Ação Compensatória Distribuída (Saga Rollback) ---
    Então a Saga distribuída deve executar a compensação e marcar a Ordem de Serviço como "CANCELED"
