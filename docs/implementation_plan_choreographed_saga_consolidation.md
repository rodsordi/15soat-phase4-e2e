# Plano de Implementação: Unificação da Saga Distribuída Coreografada

> **Decisão do Usuário**: Opção 1 - Nomenclatura Padrão do Domínio (`choreographed_saga.*`)
> **Status**: Aprovado para Execução

---

## 1. Visão Geral e Objetivos

Consolidar as especificações fragmentadas de Saga distribuída (`complete_choreographed_saga.*` e `compensating_saga_rollback.*`) em uma única especificação canônica por padrão de Living Documentation:
1. `src/test/resources/features/saga/choreographed_saga.feature`:
   - Cenário 1: Caminho Feliz (Happy Path) da abertura à finalização do veículo.
   - Cenário 2: Rollback Compensatório por Recusa de Pagamento.
   - Cenário 3: Rollback Compensatório com Estorno Financeiro por Falha Técnica na Oficina.
   - Cenário 4: Idempotência e Imutabilidade do Estado Compensado perante Notificações Repetidas.
2. `src/test/resources/features/saga/choreographed_saga.md`:
   - Diagrama 1: Coreografia do Fluxo Principal (Happy Path) - 6 participantes.
   - Diagrama 2: Rollback por Recusa de Pagamento - 5 participantes.
   - Diagrama 3: Rollback Retrôgrado Completo por Falha na Oficina (com Estorno Mercado Pago) - 6 participantes.
   - Diagrama 4: Idempotência de Compensação e At-Least-Once Delivery - 4 participantes.
   - Atores estritamente alinhados com a regra de negócio: Cliente (`CUSTOMER`) exercendo a decisão soberana de aprovação do orçamento, Atendente (`EMPLOYEE`) na abertura/entrega e Mecânico (`EMPLOYEE`) na execução.
3. `src/test/resources/features/saga/choreographed_saga.js`:
   - Script de teste de estresse k6 consolidado para a Saga distribuída.
4. `README.md`:
   - Atualização dos links e descrições na seção `4. Padrão Saga Distribuída Coreografada`.
5. Exclusão dos arquivos legados fragmentados:
   - `complete_choreographed_saga.feature`, `complete_choreographed_saga.md`, `complete_choreographed_saga.js`.
   - `compensating_saga_rollback.feature`, `compensating_saga_rollback.md`, `compensating_saga_rollback.js`.
6. Validação:
   - Execução dos testes unitários e BDD E2E com `CucumberTestRunner`.
