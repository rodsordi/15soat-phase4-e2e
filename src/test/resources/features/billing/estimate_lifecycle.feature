# language: pt
@e2e @billing @estimate @orcamento
Funcionalidade: Ciclo de Vida do Orçamento na API de Faturamento
  Como cliente da oficina mecânica
  Quero receber, consultar e aprovar ou rejeitar o orçamento dos serviços diretamente na API de Faturamento
  Para formalizar a autorização dos reparos com total transparência e segurança

  # ============================================================================
  # Contexto: Serviços Operacionais e Ator Autenticado
  # ============================================================================
  Contexto:
    Dado que o serviço de Faturamento está em execução e operacional
    E que o cliente está autenticado com perfil "CUSTOMER"

  # ============================================================================
  # Cenário 1: Consulta de Orçamento Gerado para a Ordem de Serviço
  # ============================================================================
  @consulta-orcamento @happy-path
  Cenário: Consulta detalhada de orçamento com discriminação de serviços e materiais
    Dado que o atendente abre uma nova Ordem de Serviço para o cliente "52998224725" e veículo "BRA2E19" com a descrição "Troca de pastilhas e disco"
    E o mecânico inicia o diagnóstico da Ordem de Serviço
    E o mecânico conclui o diagnóstico e o faturamento gera o orçamento
    Quando o cliente consulta o orçamento da Ordem de Serviço na API Billing
    Então o código de status HTTP da resposta deve ser 200
    E o orçamento deve ter o status "PENDING_APPROVAL"

  # ============================================================================
  # Cenário 2: Aprovação Soberana de Orçamento pelo Cliente
  # ============================================================================
  @aprovacao-orcamento @happy-path
  Cenário: Aprovação do orçamento pelo cliente via API Billing liberando a execução da OS
    Dado que o atendente abre uma nova Ordem de Serviço para o cliente "52998224725" e veículo "BRA2E19" com a descrição "Alinhamento e balanceamento"
    E o mecânico inicia o diagnóstico da Ordem de Serviço
    E o mecânico conclui o diagnóstico e o faturamento gera o orçamento
    Quando o próprio cliente aprova o orçamento diretamente na API Billing
    Então o código de status HTTP da resposta deve ser 200
    E o orçamento deve ter o status "APPROVED"

  # ============================================================================
  # Cenário 3: Rejeição Soberana de Orçamento pelo Cliente com Justificativa
  # ============================================================================
  @rejeicao-orcamento @fluxo-alternativo
  Cenário: Rejeição do orçamento pelo cliente via API Billing com justificativa
    Dado que o atendente abre uma nova Ordem de Serviço para o cliente "52998224725" e veículo "BRA2E19" com a descrição "Troca de amortecedores"
    E o mecânico inicia o diagnóstico da Ordem de Serviço
    E o mecânico conclui o diagnóstico e o faturamento gera o orçamento
    Quando o próprio cliente rejeita o orçamento diretamente na API Billing com motivo "Custo das peças acima do esperado"
    Então o código de status HTTP da resposta deve ser 200
    E o orçamento deve ter o status "REJECTED"
