# Plano de Implementação: Novos Cenários de Rollback Compensatório da Saga e Diagramas de Sequência

> **Decisão do Usuário**: Opção 1 - Suíte Completa de Compensação da Saga Distribuída com Documentação Viva Integrada (BDD + Mermaid)
> **Status**: Aprovado para Execução

---

## 1. Contexto e Motivação
O arquivo `compensating_saga_rollback.feature` continha apenas 1 cenário inicial cobrindo o cancelamento de Ordem de Serviço devido à recusa preliminar de pagamento pelo gateway financeiro.
Na arquitetura de microsserviços distribuídos orientada a eventos (*Saga Choreography* via Apache Kafka), a compensação transacional mais complexa ocorre quando a transação financeira já foi consolidada (`PAID`) e precisa ser estornada retroativamente (`REFUNDED`) no Mercado Pago e na base de dados devido a impedimentos insuperáveis na oficina mecânica (`ExecutionFailedEvent`). Além disso, é indispensável cobrir a idempotência contra reentregas de mensagens no Kafka.

---

## 2. Escopo das Alterações

### 2.1. Especificação BDD (`15soat-phase4-e2e`)
- **Arquivo**: `src/test/resources/features/saga/compensating_saga_rollback.feature`
  - **Cenário 2**: *Rollback Compensatório com Estorno Financeiro por Falha Mecânica na Oficina*
    - Fluxo: Ordem de Serviço aberta e aprovada $\rightarrow$ Fatura liquidada no Mercado Pago (`PAID`) $\rightarrow$ Veículo entra em reparo na oficina $\rightarrow$ Mecânico registra impedimento técnico insuperável com status `FAILED` $\rightarrow$ Saga distribuída executa estorno no Mercado Pago e transiciona a fatura para `REFUNDED` $\rightarrow$ Ordem de Serviço é cancelada (`CANCELED`).
  - **Cenário 3**: *Idempotência da Ação Compensatória perante Mensagens Duplicadas de Falha no Kafka*
    - Fluxo: Transmissão de eventos duplicados de falha técnica $\rightarrow$ Sistema preserva a fatura estornada (`REFUNDED`) e a Ordem de Serviço cancelada (`CANCELED`) sem duplicar estornos nem lançar falhas de transição de estado.

### 2.2. Implementação dos Steps (`15soat-phase4-e2e`)
- **Arquivo**: `src/test/java/br/com/fiap/phase4/e2e/steps/E2ESagaSteps.java`
  - Implementar step `@Quando("o mecânico registra falha técnica irrecuperável no reparo da oficina")`
  - Implementar step `@Entao("o sistema de faturamento deve processar o estorno financeiro no Mercado Pago e marcar a fatura como {string}")`
  - Implementar step `@Quando("o Kafka retransmite o evento de falha técnica para a mesma Ordem de Serviço")`
  - Implementar step `@Entao("o estado compensado deve permanecer inalterado como fatura {string} e Ordem de Serviço {string}")`

### 2.3. Diagramas de Sequência Mermaid (`15soat-phase4-e2e`)
- **Arquivo**: `src/test/resources/features/saga/compensating_saga_rollback.md`
  - Atualizar documento conforme **ADR-004**:
    - **Diagrama 1**: Cenário 1 - Rollback Preliminar por Recusa de Pagamento.
    - **Diagrama 2**: Cenário 2 - Rollback Compensatório com Estorno Financeiro e Reversão Multi-Serviço (`Exec` $\rightarrow$ `Kafka` $\rightarrow$ `Billing` $\rightarrow$ `Mercado Pago` $\rightarrow$ `OS`).
    - **Diagrama 3**: Cenário 3 - Garantia de Idempotência e Tratamento de Redelivery no Kafka.

---

## 3. Validação e Quality Gate
1. Executar os testes unitários e de BDD locais do `15soat-phase4-e2e` via Maven contra o ambiente `prd`:
   ```powershell
   mvn test -f C:\git\fiap\phase4\15soat-phase4-e2e\pom.xml "-Dtest=CucumberTestRunner" "-Denv=prd" "-Dgateway.url=http://34.197.77.217" "-Dgateway.host=api.garage.fiap.com.br"
   ```
2. Garantir 100% de sucesso (55/55 cenários verdes).
3. Submeter ao auditor sentinela de código independente.
4. Solicitar autorização explícita do usuário para commit, push e abertura/merge de Pull Request.
