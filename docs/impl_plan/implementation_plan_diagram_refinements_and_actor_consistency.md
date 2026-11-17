# Plano de Implementação: Refinamentos de Consistência Visual e Nomenclatura nos Diagramas

> **Decisão do Usuário**: Opção 1 - Refinamento Completo de Consistência e ADR-004 (com Preservação Obrigatória do CSS Zoom)
> **Status**: Aprovado para Execução

---

## 🎯 Objetivo
Executar melhorias de semântica e padronização visual nos diagramas de sequência da suíte E2E, renomeando o ator de faturamento em `invoice_generation.md` para eliminar ambiguidade com clientes, harmonizando a largura do Cenário 1 da Saga, padronizando cabeçalhos H2 e preservando estritamente a propriedade `zoom` conforme determinação explícita do usuário.

---

## 🏗️ Especificações Técnicas

### 1. Semântica de Ator em `invoice_generation.md`
- Substituir o identificador `Cliente` por `Analista`:
  - `actor Analista as Analista Financeiro<br/>(Funcionário: EMPLOYEE)`
  - Atualizar as interações de envio de requisição e recebimento de resposta para `Analista`.

### 2. Preservação Estrita do CSS `zoom`
- Em `work_order_lifecycle.md`: **manter integralmente** `zoom: 1.5;` no bloco de estilo CSS conforme diretriz mandatória do usuário.

### 3. Harmonização de Largura na Saga Coreografada (`choreographed_saga.md`)
- No Cenário 1 (7 participantes): ajustar `min-width: 1450px !important;` e `width: 1450px !important;` para simetria visual com os demais cenários complexos.

### 4. Padronização de Cabeçalhos H2
- Padronizar os títulos de [material_management.md](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/material_management.md) e [service_catalog_management.md](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/work-order/service_catalog_management.md) para `## Diagrama de Sequência`.

---

## 📋 Passos de Execução
1. Atualizar `invoice_generation.md` com o ator `Analista`.
2. Ajustar `choreographed_saga.md` Cenário 1 para `1450px`.
3. Padronizar cabeçalhos H2 em `material_management.md` e `service_catalog_management.md`.
4. Executar `mvn clean process-test-resources test-compile`.
5. Submeter ao auditor independente (Code Reviewer Sentinel).
6. Solicitar permissão do usuário para commit e push.
