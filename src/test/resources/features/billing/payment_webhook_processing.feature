# language: pt
@e2e @pagamento @webhook @mercadopago
Funcionalidade: Processamento de Webhooks de Pagamento e Liquidação
  Como gateway de pagamento Mercado Pago e sistema de faturamento
  Quero notificar eventos de transação e reconciliar status financeiros
  Para atualizar faturas para PAID e disparar eventos na Saga distribuída

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço de Faturamento
  # ============================================================================
  Contexto:
    Dado que o serviço de Faturamento está em execução e operacional
    E uma fatura gerada com status "PENDING"

  # ============================================================================
  # Cenário 1: Confirmação de Pagamento Aprovado
  # ============================================================================
  @pagamento-aprovado
  Cenário: Processamento de webhook com status de pagamento aprovado
    Quando o Mercado Pago envia uma notificação de webhook de pagamento com o status "approved"
    Então o status da fatura deve ser alterado para "PAID"

  # ============================================================================
  # Cenário 2: Recusa de Pagamento e Rejeição
  # ============================================================================
  @pagamento-recusado
  Cenário: Processamento de webhook com status de pagamento recusado
    Quando o Mercado Pago envia uma notificação de webhook de pagamento com o status "rejected"
    Então o status da fatura deve ser alterado para "FAILED"
