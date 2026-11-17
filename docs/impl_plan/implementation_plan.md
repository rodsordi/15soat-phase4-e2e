# Plano de Implementação: Atualização Completa dos Módulos Críticos de Negócio (`.feature`)

> **Decisão do Usuário**: Opção 1 - Atualização Completa dos Módulos Críticos de Negócio
> **Status**: Aprovado para Execução

## Contexto e Escopo
Propagar as práticas e convenções estabelecidas em `customer_vehicle_management.feature` para os demais módulos de negócio da suíte E2E:
1. **`materials_and_checklist.feature`**:
   - Migrar o registro de insumo no reparo para Cucumber Data Table.
   - Refatorar checklist técnico para tabela de itens de inspeção.
2. **`execution_queue_and_repair.feature`**:
   - Polimento semântico para voz ativa do ator técnico (`Quando o técnico inicia o reparo...`, `Quando o técnico conclui o reparo...`).
3. **`invoice_generation.feature`**:
   - Migrar a abertura de fatura para Cucumber Data Table (`amount`, `customerDocument`, `workOrderId`).
4. **`service_catalog_management.feature` e `material_management.feature`**:
   - Adicionar cenários negativos (sad paths) com asserção RFC 7807 (`ProblemDetail`) para itens duplicados ou não encontrados.
5. **Step Definitions (`WorkOrderSteps.java` e `BillingSteps.java`)**:
   - Implementar e compatibilizar os bindings para as tabelas e steps ajustados.
   - Validar compilação com `mvn test-compile`.
