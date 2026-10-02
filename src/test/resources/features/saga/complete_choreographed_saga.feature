# language: pt
@e2e @saga @coreografia @happy-path
Funcionalidade: Coreografia Completa da Saga Distribuída (Happy Path)
  Como plataforma distribuída da oficina mecânica
  Quero coordenar eventos assíncronos entre Ordem de Serviço, Faturamento e Execução
  Para garantir o fluxo distribuído íntegro desde a abertura até a conclusão do veículo

  # ============================================================================
  # Contexto: Validação Operacional dos Microsserviços da Plataforma
  # ============================================================================
  Contexto:
    Dado que todos os microsserviços da plataforma estão em execução e operacionais

  # ============================================================================
  # Cenário 1: Happy Path - Saga Concluída com Sucesso
  # ============================================================================
  @saga-sucesso
  Cenário: Fluxo Principal - Ciclo completo da Saga da abertura à finalização do veículo
    # --- 1. Abertura da Ordem de Serviço ---
    Quando o cliente abre uma nova Ordem de Serviço para o veículo "BRA2E19"

    # --- 2. Aprovação do Orçamento ---
    E o gestor da oficina aprova o orçamento da Ordem de Serviço

    # --- 3. Emissão da Fatura e Liquidação Mercado Pago ---
    E o serviço de Faturamento gera a fatura e o Mercado Pago confirma o pagamento

    # --- 4. Enfileiramento e Execução dos Reparos na Oficina ---
    E o serviço de Execução enfileira o veículo e conclui os reparos

    # --- 5. Sincronização Final da Ordem de Serviço ---
    Então a Ordem de Serviço deve refletir o status final "COMPLETED"
