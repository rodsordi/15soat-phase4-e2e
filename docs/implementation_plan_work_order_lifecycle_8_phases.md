# Plano de Implementação: Alinhamento do Ciclo de Vida da Ordem de Serviço em 8 Fases

> **Decisão do Usuário**: Opção 1 - Paridade 1:1 entre Gherkin (`.feature`), Diagrama de Sequência (`.md`) e Step Definitions Java
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

O diagrama de sequência canônico em `work_order_lifecycle.md` foi atualizado para contemplar a arquitetura completa orientada a eventos em 8 fases operacionais (incluindo `api-billing` e `Mercado Pago`), com segregação de responsabilidades entre cliente (`CUSTOMER`) e equipe técnica (`EMPLOYEE`).

Este plano estabelece a atualização do arquivo Gherkin `work_order_lifecycle.feature` e das respectivas implementações de passos em `WorkOrderSteps.java` no projeto `15soat-phase4-e2e`, garantindo:
1. Paridade estrita 1:1 com o fluxo documentado em `work_order_lifecycle.md`.
2. As 8 fases operacionais representadas com clareza nos cenários BDD:
   - **Fase 1**: `RECEIVED` (Recepção do veículo e abertura de OS pelo atendente).
   - **Fase 2**: `DIAGNOSING` (Mecânico inicia diagnóstico na `api-exec`).
   - **Fase 3**: `WAITING_APPROVAL` (Diagnóstico concluído e orçamento emitido pela `api-billing`).
   - **Fase 4**: `APPROVED` (Aprovação soberana do orçamento pelo próprio cliente).
   - **Fase 5**: `EXECUTING` (Mecânico inicia execução dos reparos na oficina).
   - **Fase 6**: `COMPLETED` (Reparos finalizados na oficina e checkout Mercado Pago gerado).
   - **Fase 7**: `PAID` (Pagamento via PIX Mercado Pago e confirmação via webhook).
   - **Fase 8**: `DELIVERED` (Atendente valida quitação e entrega o veículo ao cliente).
3. Remoção do passo de métricas analíticas (`calculateAverageExecutionTime`) do fluxo operacional de ciclo de vida da OS.
4. Código e anotações Java estritamente em inglês, mantendo Gherkin em português (`# language: pt`).

---

## 2. Itens de Execução

1. **Atualização do Gherkin**: `15soat-phase4-e2e/src/test/resources/features/work-order/work_order_lifecycle.feature`
   - Atualizar a descrição da funcionalidade e do contexto para incluir os papéis `CUSTOMER` e `EMPLOYEE`.
   - Reestruturar o cenário principal para cobrir as 8 fases ordenadas sequencialmente.
   - Ajustar as frases das ações operacionais para refletir os atores corretos (cliente aprovando orçamento, cliente pagando Mercado Pago, mecânico iniciando reparos).
2. **Atualização dos Step Definitions**: `15soat-phase4-e2e/src/test/java/br/com/fiap/phase4/e2e/steps/WorkOrderSteps.java`
   - Adicionar binding para autenticação de perfil `CUSTOMER`.
   - Adicionar métodos de step em inglês com anotações `@Quando` para cada nova transição (`WAITING_APPROVAL`, `APPROVED`, `EXECUTING`, `PAID`, `DELIVERED`).
   - Preservar métodos auxiliares sem quebrar cenários legados.
3. **Validação e Compilação**:
   - Executar `mvn test-compile` no módulo `15soat-phase4-e2e` para validar sintaxe e bindings sem erros.
