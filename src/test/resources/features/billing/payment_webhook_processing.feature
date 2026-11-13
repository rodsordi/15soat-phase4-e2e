# language: pt
@e2e @pagamento @webhook @mercadopago
Funcionalidade: Processamento de Webhooks de Pagamento e Liquidação
  Como gateway de pagamento Mercado Pago e sistema de faturamento
  Quero notificar eventos de transação e reconciliar status financeiros
  Para atualizar faturas para PAID e disparar eventos na Saga distribuída

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço e Fatura com Checkout
  # ============================================================================
  Contexto:
    Dado que o serviço de Faturamento está em execução e operacional
    E que o analista de faturamento está devidamente autenticado com perfil "EMPLOYEE"
    E uma fatura é criada para a Ordem de Serviço com o valor 350.00 e cliente "52998224725"
    E a preferência de pagamento deve conter um link de checkout válido

  # ============================================================================
  # Cenário 1: Confirmação de Pagamento Aprovado com Reconciliação Ativa
  # ============================================================================
  @pagamento-aprovado @happy-path
  Cenário: Processamento de webhook com status de pagamento aprovado via reconciliação
    Quando o Mercado Pago notifica o evento "payment.updated" para a fatura com pagamento aprovado
    Então o webhook deve responder com status 200 e confirmação "PROCESSED"
    E o status da fatura deve ser alterado para "PAID"

  # ============================================================================
  # Cenário 2: Recusa de Pagamento e Falha Financeira
  # ============================================================================
  @pagamento-recusado
  Cenário: Processamento de webhook com status de pagamento recusado via reconciliação
    Quando o Mercado Pago notifica o evento "payment.updated" para a fatura com pagamento recusado
    Então o webhook deve responder com status 200 e confirmação "PROCESSED"
    E o status da fatura deve ser alterado para "FAILED"

  # ============================================================================
  # Cenário 3: Idempotência de Webhooks Duplicados (ADR 0001)
  # ============================================================================
  @idempotencia-webhook @resiliencia
  Cenário: Processamento idempotente de notificações de webhook repetidas
    Dado que a fatura foi previamente liquidada com sucesso
    Quando o Mercado Pago retransmite a notificação de webhook com o mesmo identificador
    Então o webhook deve responder com status 200 e confirmação "PROCESSED"
    E o status da fatura deve permanecer inalterado como "PAID"
