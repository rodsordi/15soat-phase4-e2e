# Plano de Implementação: Alinhamento e Sanitização dos Scripts de Carga k6

> **Decisão do Usuário**: Opção 1 - Alinhamento e Sanitização dos Scripts de Carga k6 (Autenticação Bearer, Máquina de Estados Válida e Contratos Limpos)
> **Status**: Aprovado para Execução

## 1. Contexto & Objetivos
Sanitizar e modernizar a suíte de scripts de teste de carga e performance em k6 localizada em `15soat-phase4-e2e/src/test/resources/features/**/*.js`.
- Injetar o cabeçalho `Authorization: Bearer <token>` padronizado via variável de ambiente `__ENV.AUTH_TOKEN || 'Bearer mock_valid_token'` para garantir compatibilidade plena com o Kong API Gateway e o Lambda Authorizer.
- Corrigir a progressão de estados em `work_order_lifecycle.js`, respeitando a máquina de estados estrita (`RECEIVED` -> `DIAGNOSING` -> `WAITING_APPROVAL` -> `APPROVED` -> `EXECUTING` -> `COMPLETED` -> `PAID` -> `DELIVERED`).
- Eliminar atributos legados e obsoletos nos payloads JSON (como `totalAmount` nas transições de status da Ordem de Serviço).
- Garantir alinhamento aos contratos RESTful vigentes em todos os domínios: Work Order, Billing, Exec, Saga e Auth.

## 2. Detalhamento dos Scripts

1. **`work-order/work_order_lifecycle.js`**:
   - Adicionar `Authorization` aos headers default.
   - Remover `totalAmount` de todos os payloads de transição de status.
   - Adicionar a transição sequencial completa: `APPROVED` -> `EXECUTING` -> `COMPLETED` -> `PAID` -> `DELIVERED`.
2. **`work-order/customer_vehicle_management.js`**:
   - Adicionar `Authorization` aos headers default.
3. **`work-order/service_catalog_management.js`**:
   - Adicionar `Authorization` aos headers default.
4. **`work-order/material_management.js`**:
   - Adicionar `Authorization` aos headers default.
5. **`billing/invoice_generation.js`**:
   - Adicionar `Authorization` aos headers default.
6. **`billing/payment_webhook_processing.js`**:
   - Adicionar `Authorization` para consultas administrativas de faturas (mantendo webhook sem token ou com token de serviço).
7. **`exec/execution_queue_and_repair.js`**:
   - Adicionar `Authorization` aos headers default.
8. **`exec/materials_and_checklist.js`**:
   - Adicionar `Authorization` aos headers default.
9. **`saga/complete_choreographed_saga.js`**:
   - Adicionar `Authorization` aos headers default.
10. **`saga/compensating_saga_rollback.js`**:
    - Adicionar `Authorization` aos headers default.

## 3. Validação
- Validar sintaxe JavaScript dos scripts k6.
- Executar `mvn test-compile` e `CucumberTestRunner` (dry-run) para garantir integridade e isolamento de todo o projeto `15soat-phase4-e2e`.
