# Plano de Implementação: Otimização Visual dos Diagramas de Sequência (Remoção do Kong Gateway)

> **Decisão do Usuário**: Opção 1 - Remover o participante Kong Gateway de todos os diagramas de sequência
> **Status**: Aprovado para Execução

---

## 1. Visão Geral e Benefícios

Remover o participante intermediário `Kong` em todos os diagramas de sequência dos microsserviços (`features/work-order/`, `features/saga/`, `features/billing/`, `features/exec/`), mantendo o Gateway explicitado como nota de borda e segurança perimetral no topo de cada diagrama.
No diagrama específico de segurança `auth-lambda/auth_token_authorizer.md`, o Gateway é mantido pois é o sujeito direto da validação do Lambda Authorizer.

### Benefícios:
1. **Redução Drástica de Poluição Visual**: Eliminação de setas duplicadas de proxy pass-through (`Ator -> Kong -> Microsserviço` e `Microsserviço --> Kong --> Ator`).
2. **Foco no Domínio e Interação entre Microsserviços**: As chamadas passam a ir diretamente do ator/cliente ao microsserviço de destino (`Cli ->>+ WO`, `Mec ->>+ Exec`).
3. **Escala e Legibilidade Perfeitas no IntelliJ & GitHub**: Redução da largura necessária (`min-width`) em cerca de 250px a 350px em todos os diagramas, permitindo fontes maiores e proporcionais sem compressão do SVG.

---

## 2. Arquivos Impactados

1. `src/test/resources/features/work-order/work_order_lifecycle.md`
2. `src/test/resources/features/saga/choreographed_saga.md`
3. `src/test/resources/features/billing/invoice_generation.md`
4. `src/test/resources/features/billing/payment_webhook_processing.md`
5. `src/test/resources/features/exec/execution_queue_and_repair.md`
6. `src/test/resources/features/exec/materials_and_checklist.md`
7. `src/test/resources/features/work-order/customer_vehicle_management.md`
8. `src/test/resources/features/work-order/service_catalog_management.md`
9. `src/test/resources/features/work-order/material_management.md`
10. `docs/adr/004-padrao-visual-diagramas-sequencia-mermaid.md` (Atualização da matriz de largura recomendada).
