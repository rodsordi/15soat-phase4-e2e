# language: pt
@e2e @faturamento @cobranca
Funcionalidade: Geração de Faturas e Checkout de Pagamento
  Como analista de faturamento da oficina mecânica
  Quero emitir faturas com precificação de peças e mão de obra
  Para disponibilizar links de checkout e QR Code Pix ao cliente

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço de Faturamento
  # ============================================================================
  Contexto:
    Dado que o serviço de Faturamento está em execução e operacional

  # ============================================================================
  # Cenário 1: Emissão de Fatura com Integração ao Mercado Pago
  # ============================================================================
  @geracao-fatura
  Cenário: Emissão de fatura com cálculo financeiro e preferência de pagamento
    Quando a fatura é emitida com os seguintes dados:
      | customerDocument | 52998224725 |
      | amount           | 350.00      |
    Então a fatura deve ser criada com o status "PENDING"
    E a preferência de pagamento deve conter um link de checkout válido
