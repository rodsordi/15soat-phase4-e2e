#language: pt
@e2e @nao-funcional @security @resiliencia @performance @billing @mercadopago @iso-25010
Funcionalidade: Requisitos Não-Funcionais - Processamento de Webhooks de Pagamento
  Como arquiteto de integração e mensageria
  Quero validar a resiliência à rede, a idempotência estrita e o tempo de resposta do webhook do Mercado Pago
  Para garantir que retransmissões repetidas e flutuações de rede não gerem eventos duplicados de liquidação financeira

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço e Fatura com Checkout
  # ============================================================================
  Contexto:
    Dado que o serviço de Faturamento está em execução e operacional
    E que o analista de faturamento está devidamente autenticado com perfil "EMPLOYEE"
    E uma fatura é criada para a Ordem de Serviço com o valor 350.00 e cliente "52998224725"
    E a preferência de pagamento deve conter um link de checkout válido

  # ============================================================================
  # Cenário NF-1: Ingress Público Autorizado Sem Token JWT (Segurança)
  # ============================================================================
  @seguranca @ingress-publico @sem-jwt
  Cenário: Webhook aceita notificações do gateway de pagamento sem exigência de Bearer JWT
    Dado que a requisição não possui cabeçalho de autenticação
    Quando o Mercado Pago notifica o evento "payment.updated" para a fatura com pagamento aprovado
    Então o webhook deve responder com status 200 e confirmação "PROCESSED"

  # ============================================================================
  # Cenário NF-2: Idempotência sob Retransmissão e SLA de Resposta (Resiliência & Performance)
  # ============================================================================
  @resiliencia @idempotencia @sla
  Cenário: SLA de confirmação de recebimento do webhook e idempotência estrita
    Quando o Mercado Pago notifica o evento "payment.updated" para a fatura com pagamento aprovado
    Então o webhook deve responder com status 200 e confirmação "PROCESSED"
    E o tempo de resposta da requisição deve ser inferior a 2500 ms
