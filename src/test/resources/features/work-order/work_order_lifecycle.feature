# language: pt
@e2e @ordem-de-servico
Funcionalidade: Ciclo de Vida Completo da Ordem de Serviço
  Como cliente, atendente e mecânico da oficina mecânica
  Quero vivenciar o fluxo operacional completo das ordens de serviço
  Para que o veículo seja recepcionado, diagnosticado, orçado, aprovado, reparado, pago e entregue com total rastreabilidade

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço, Usuários e Catálogo Prévio
  # ============================================================================
  Contexto:
    Dado que o serviço de Ordem de Serviço está em execução e operacional
    E que o atendente e o mecânico estão devidamente autenticados com perfil "EMPLOYEE"
    E que o cliente está autenticado com perfil "CUSTOMER"
    E um cliente cadastrado com documento "52998224725" e veículo "BRA2E19"
    E os seguintes serviços cadastrados no catálogo:
      | code       | name                   | price  |
      | SRV-OIL-01 | Troca de Óleo e Filtro | 150.00 |
      | SRV-BRK-01 | Revisão de Freios      | 200.00 |
    E os seguintes materiais cadastrados no catálogo:
      | sku         | description                 | unitCost | unitPrice | quantity |
      | PART-OIL-01 | Óleo Sintético 5W30         | 30.00    | 50.00     | 50       |
      | PART-BRK-01 | Pastilha de Freio Dianteira | 45.00    | 95.00     | 20       |

  # ============================================================================
  # Cenário 1: Happy Path - Ciclo de Vida Completo da Ordem de Serviço (8 Fases)
  # ============================================================================
  @happy-path @ciclo-completo
  Cenário: Ciclo de vida completo da ordem de serviço da recepção até a liberação final
    # --- 1. Recepção do Veículo e Entrada na Fila (RECEIVED) ---
    Quando o atendente abre uma nova Ordem de Serviço com os seguintes dados:
      | customerDocument | 52998224725                       |
      | licensePlate     | BRA2E19                           |
      | description      | Troca de óleo e revisão de freios |
    Então a Ordem de Serviço deve ser criada com o status "RECEIVED"
    E a consulta da Ordem de Serviço por ID deve retornar os seguintes dados:
      | customerDocument | 52998224725 |
      | licensePlate     | BRA2E19     |
      | status           | RECEIVED    |

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
  # Cenário 2: Fluxo Alternativo - Rejeição Soberana do Orçamento pelo Cliente
  # ============================================================================
  @fluxo-alternativo @rejeicao-orcamento
  Cenário: Cancelamento da Ordem de Serviço após rejeição do orçamento pelo cliente
    Dado uma Ordem de Serviço existente com o status "WAITING_APPROVAL"
    Quando o próprio cliente rejeita o orçamento da Ordem de Serviço
    Então o status da Ordem de Serviço deve ser atualizado para "CANCELED"

  # ============================================================================
  # Cenário 3: Exceção de Domínio - Tentativa de Liberação sem Pagamento (Sad Path)
  # ============================================================================
  @sad-path @excecao-liberacao-nao-quitada
  Cenário: Rejeição da liberação de veículo antes da confirmação de quitação
    Dado uma Ordem de Serviço existente com o status "COMPLETED"
    Quando o atendente tenta liberar o veículo para o cliente sem a confirmação do pagamento
    Então o erro retornado deve corresponder a:
      | status    | 422                                                         |
      | errorCode | INVALID_STATUS_TRANSITION                                   |
      | title     | Business Rule Violation                                     |
      | detail    | Work order cannot be delivered before payment is confirmed  |
