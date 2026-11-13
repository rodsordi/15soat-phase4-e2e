# language: pt
@e2e @faturamento @cobranca
Funcionalidade: Geração de Faturas e Checkout de Pagamento
  Como analista de faturamento da oficina mecânica
  Quero emitir faturas com precificação de peças e mão de obra
  Para disponibilizar links de checkout e QR Code Pix ao cliente

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço e Analista Autenticado
  # ============================================================================
  Contexto:
    Dado que o serviço de Faturamento está em execução e operacional
    E que o analista de faturamento está devidamente autenticado com perfil "EMPLOYEE"

  # ============================================================================
  # Cenário 1: Caminho Feliz - Emissão de Fatura com Sucesso
  # ============================================================================
  @geracao-fatura @happy-path
  Cenário: Emissão de fatura com cálculo financeiro e preferência de pagamento
    Quando a fatura é emitida com os seguintes dados:
      | customerDocument | 52998224725 |
      | amount           | 350.00      |
    Então a fatura deve ser criada com o status "PENDING"
    E a preferência de pagamento deve conter um link de checkout válido

  # ============================================================================
  # Cenário 2: Idempotência - Emissão Duplicada para a Mesma Ordem de Serviço
  # ============================================================================
  @idempotencia @resiliencia
  Cenário: Garantia de idempotência perante requisições duplicadas de faturamento
    Dado uma fatura criada para a Ordem de Serviço com o valor 400.00 e cliente "52998224725"
    Quando uma nova solicitação de fatura é submetida para a mesma Ordem de Serviço com o valor 400.00
    Então o sistema deve retornar a fatura existente sem duplicar o registro
    E o identificador da fatura deve permanecer inalterado

  # ============================================================================
  # Cenário 3: Validação de Domínio - Rejeição de Fatura com Valor Inválido
  # ============================================================================
  @validacao @valor-invalido
  Esquema do Cenário: Rejeição na emissão de faturas com valor monetário menor ou igual a zero
    Quando uma fatura é criada para a Ordem de Serviço com o valor <valor> e cliente "52998224725"
    Então a requisição deve ser rejeitada com status 400

    Exemplos:
      | valor  |
      | 0.00   |
      | -50.00 |

  # ============================================================================
  # Cenário 4: Validação de Domínio - Rejeição de Documento de Cliente Inválido
  # ============================================================================
  @validacao @cpf-invalido
  Cenário: Rejeição na emissão de fatura com documento de cliente inválido
    Quando uma fatura é criada para a Ordem de Serviço com o valor 250.00 e cliente "11111111111"
    Então a requisição deve ser rejeitada com status 422

  # ============================================================================
  # Cenário 5: Consulta de Fatura por ID e por Ordem de Serviço
  # ============================================================================
  @consulta @read-operation
  Cenário: Recuperação e consulta detalhada de fatura existente
    Dado uma fatura criada para a Ordem de Serviço com o valor 500.00 e cliente "52998224725"
    Quando o analista consulta a fatura pelo seu identificador único
    Então o código de status HTTP da resposta deve ser 200
    E a fatura deve conter os links HATEOAS de auto-referência e da ordem de serviço
    Quando o analista consulta a fatura pelo identificador da Ordem de Serviço
    Então a mesma fatura deve ser retornada com status "PENDING"

  # ============================================================================
  # Cenário 6: Consulta Negativa - Fatura Inexistente
  # ============================================================================
  @consulta @not-found
  Cenário: Consulta de fatura inexistente
    Quando o analista consulta uma fatura com um ID inexistente "00000000-0000-0000-0000-000000000000"
    Então a requisição deve ser rejeitada com status 404

  # ============================================================================
  # Cenário 7: Geração Específica de Checkout Pix QR Code do Mercado Pago
  # ============================================================================
  @checkout @mercadopago @pix
  Cenário: Geração explícita de Checkout Pix para fatura pendente
    Dado uma fatura criada para a Ordem de Serviço com o valor 600.00 e cliente "52998224725"
    Quando uma requisição de inicialização de checkout é enviada para a fatura
    Então o código de status HTTP da resposta deve ser 200
    E a resposta deve conter a chave "qrCode" e o payload "qrCodeBase64" do Mercado Pago

  # ============================================================================
  # Cenário 8: Segurança e Controle de Acesso (RBAC)
  # ============================================================================
  @seguranca @rbac
  Cenário: Bloqueio de acesso para requisição sem token de autenticação
    Dado que a requisição não possui cabeçalho de autenticação
    Quando uma tentativa de emissão de fatura é submetida
    Então o código de status HTTP da resposta deve ser 401
