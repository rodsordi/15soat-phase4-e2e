# language: pt
@e2e @ordem-de-servico
Funcionalidade: Ciclo de Vida Completo da Ordem de Serviço
  Como cliente, atendente e mecânico da oficina mecânica
  Quero vivenciar o fluxo operacional completo das ordens de serviço
  Para que o veículo seja recepcionado, diagnosticado, orçado, aprovado, reparado, pago e entregue com total rastreabilidade

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço, Usuários e Cliente/Veículo Base
  # ============================================================================
  Contexto:
    Dado que o serviço de Ordem de Serviço está em execução e operacional
    E que o atendente e o mecânico estão devidamente autenticados com perfil "EMPLOYEE"
    E que o cliente está autenticado com perfil "CUSTOMER"
    E um cliente cadastrado com documento "52998224725" e veículo "BRA2E19"

  # ============================================================================
  # Regra 1: O ciclo de vida da OS deve evoluir pelas 8 fases operacionais
  # ============================================================================
  Regra: O ciclo de vida da Ordem de Serviço deve evoluir sequencialmente através das 8 fases canônicas

    @happy-path @ciclo-completo
    Cenário: Ciclo de vida completo da ordem de serviço da recepção até a liberação final
      # --- 1. Recepção do Veículo e Entrada na Fila (RECEIVED) ---
      Quando o atendente abre uma nova Ordem de Serviço com os seguintes dados:
        | customerDocument | 52998224725                       |
        | licensePlate     | BRA2E19                           |
        | description      | Troca de óleo e revisão de freios |
      Então a Ordem de Serviço deve ser criada com o status "RECEIVED"

      # --- 2. Diagnóstico Técnico na Oficina (DIAGNOSING) ---
      Quando o mecânico inicia o diagnóstico da Ordem de Serviço
      Então o status da Ordem de Serviço deve ser atualizado para "DIAGNOSING"

      # --- 3. Conclusão do Diagnóstico e Envio do Orçamento (WAITING_APPROVAL) ---
      Quando o mecânico conclui o diagnóstico e o faturamento gera o orçamento
      Então o status da Ordem de Serviço deve ser atualizado para "WAITING_APPROVAL"

      # --- 4. Aprovação Soberana do Orçamento pelo Cliente (APPROVED) ---
      Quando o próprio cliente aprova o orçamento da Ordem de Serviço
      Então o status da Ordem de Serviço deve ser atualizado para "APPROVED"

      # --- 5. Execução dos Reparos Técnicos na Oficina (EXECUTING) ---
      Quando o mecânico inicia a execução dos reparos na oficina
      Então o status da Ordem de Serviço deve ser atualizado para "EXECUTING"

      # --- 6. Conclusão dos Serviços Técnicos (COMPLETED) ---
      Quando o mecânico conclui todos os reparos técnicos na oficina
      Então o status da Ordem de Serviço deve ser atualizado para "COMPLETED"

      # --- 7. Liquidação via Mercado Pago e Webhook (PAID) ---
      Quando o cliente realiza o pagamento via Mercado Pago e o webhook confirma a liquidação
      Então o status da Ordem de Serviço deve ser atualizado para "PAID"

      # --- 8. Retirada e Liberação do Veículo Quitado ao Cliente (DELIVERED) ---
      Quando o atendente libera o veículo quitado para o cliente
      Então o status da Ordem de Serviço deve ser atualizado para "DELIVERED"

  # ============================================================================
  # Regra 2: O cliente possui autoridade soberana sobre o orçamento
  # ============================================================================
  Regra: O cliente possui autoridade soberana para decidir sobre a aprovação ou recusa do orçamento

    @fluxo-alternativo @rejeicao-orcamento
    Cenário: Cancelamento da Ordem de Serviço após rejeição do orçamento pelo cliente
      Dado que o atendente abre uma nova Ordem de Serviço com os seguintes dados:
        | customerDocument | 52998224725                       |
        | licensePlate     | BRA2E19                           |
        | description      | Troca de óleo e revisão de freios |
      E o mecânico inicia o diagnóstico da Ordem de Serviço
      E o mecânico conclui o diagnóstico e o faturamento gera o orçamento
      Quando o próprio cliente rejeita o orçamento da Ordem de Serviço
      Então o status da Ordem de Serviço deve ser atualizado para "CANCELED"

    @sad-path @excecao-aprovacao-sem-orcamento
    Cenário: Rejeição da aprovação de Ordem de Serviço antes da formalização do orçamento
      Dado que o atendente abre uma nova Ordem de Serviço com os seguintes dados:
        | customerDocument | 52998224725                       |
        | licensePlate     | BRA2E19                           |
        | description      | Troca de óleo e revisão de freios |
      Quando o próprio cliente tenta aprovar a Ordem de Serviço antes da formulação do orçamento
      Então o erro retornado deve corresponder a:
        | status    | 400                                                  |
        | errorCode | INVALID_STATUS_TRANSITION                            |
        | title     | Business Rule Violation                              |
        | detail    | Cannot approve work order before budget is generated |

    @sad-path @excecao-execucao-sem-aprovacao
    Cenário: Rejeição do início de execução de reparos sem aprovação prévia do cliente
      Dado que o atendente abre uma nova Ordem de Serviço com os seguintes dados:
        | customerDocument | 52998224725                       |
        | licensePlate     | BRA2E19                           |
        | description      | Troca de óleo e revisão de freios |
      E o mecânico inicia o diagnóstico da Ordem de Serviço
      E o mecânico conclui o diagnóstico e o faturamento gera o orçamento
      Quando o mecânico tenta iniciar os reparos sem a aprovação do orçamento
      Então o erro retornado deve corresponder a:
        | status    | 400                                                 |
        | errorCode | INVALID_STATUS_TRANSITION                           |
        | title     | Business Rule Violation                             |
        | detail    | Cannot execute work order before budget is approved |

  # ============================================================================
  # Regra 3: Nenhum veículo pode ser liberado sem comprovação de quitação financeira
  # ============================================================================
  Regra: Nenhum veículo pode ser liberado sem prévia confirmação de quitação financeira

    @sad-path @excecao-liberacao-nao-quitada
    Cenário: Rejeição da liberação de veículo antes da confirmação de quitação
      Dado que o atendente abre uma nova Ordem de Serviço com os seguintes dados:
        | customerDocument | 52998224725                       |
        | licensePlate     | BRA2E19                           |
        | description      | Troca de óleo e revisão de freios |
      E o mecânico inicia o diagnóstico da Ordem de Serviço
      E o mecânico conclui o diagnóstico e o faturamento gera o orçamento
      E o próprio cliente aprova o orçamento da Ordem de Serviço
      E o mecânico inicia a execução dos reparos na oficina
      E o mecânico conclui todos os reparos técnicos na oficina
      Quando o atendente tenta liberar o veículo para o cliente sem a confirmação do pagamento
      Então o erro retornado deve corresponder a:
        | status    | 400                                                         |
        | errorCode | INVALID_STATUS_TRANSITION                                   |
        | title     | Business Rule Violation                                     |
        | detail    | Work order cannot be delivered before payment is confirmed  |

  # ============================================================================
  # Regra 4: Ordens de serviço liquidadas ou entregues não podem ser canceladas
  # ============================================================================
  Regra: Ordens de serviço liquidadas ou entregues não podem ser canceladas

    @sad-path @excecao-cancelamento-pos-quitacao
    Cenário: Rejeição de cancelamento de Ordem de Serviço já liquidada financeiramente
      Dado que o atendente abre uma nova Ordem de Serviço com os seguintes dados:
        | customerDocument | 52998224725                       |
        | licensePlate     | BRA2E19                           |
        | description      | Troca de óleo e revisão de freios |
      E o mecânico inicia o diagnóstico da Ordem de Serviço
      E o mecânico conclui o diagnóstico e o faturamento gera o orçamento
      E o próprio cliente aprova o orçamento da Ordem de Serviço
      E o mecânico inicia a execução dos reparos na oficina
      E o mecânico conclui todos os reparos técnicos na oficina
      E o cliente realiza o pagamento via Mercado Pago e o webhook confirma a liquidação
      Quando o cliente tenta cancelar a Ordem de Serviço já quitada
      Então o erro retornado deve corresponder a:
        | status    | 400                                            |
        | errorCode | INVALID_STATUS_TRANSITION                      |
        | title     | Business Rule Violation                        |
        | detail    | Cannot cancel an already finalized work order  |
