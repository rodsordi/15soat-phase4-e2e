# Plano de Implementação: Referências de Borda Segura ao auth_token_authorizer.md

> **Decisão do Usuário**: Opção 1 - Referência Dupla: In-Diagram + Link Clicável no Markdown
> **Status**: Aprovado para Execução

---

## 1. Contexto & Objetivos

Padronizar todas as notas de segurança perimetral (iniciadas pelo emoji `🔒`) nos diagramas de sequência Mermaid e na documentação descritiva de cada módulo da suíte E2E (`15soat-phase4-e2e`).
A validação perimetral de tokens Bearer JWT via AWS API Gateway / Kong Ingress é centralizada no **Lambda Token Authorizer**, documentado formalmente em `features/auth-lambda/auth_token_authorizer.md`.

Para atender à máxima navegabilidade e conformidade com a ADR-004:
1. **No Bloco Mermaid (`Note over ...: 🔒`)**: O texto passa a referenciar explicitamente o Authorizer e o diagrama:
   `Note over <Atores>,<Serviço>: 🔒 Borda Segura via API Gateway & Token Authorizer (vide auth_token_authorizer.md):<br/>Requisições utilizam Bearer JWT (Role: ...)`
2. **No Texto Markdown (Logo acima do diagrama)**: Adicionar um callout `[!NOTE]` com link navegável para o arquivo `auth_token_authorizer.md`.
3. **Alinhamento do Cliente no `choreographed_saga.md`**: Garantir que o `Cliente` figure na primeira coluna (extremo esquerdo) em todos os cenários aplicáveis.

---

## 2. Matriz de Arquivos Afetados

| Módulo | Arquivo | Linha / Cenário | Papel / Escopo |
| :--- | :--- | :--- | :--- |
| `auth-lambda` | `employee_identity_management.md` | Linha 27 | `Role: ADMIN` |
| `billing` | `invoice_generation.md` | Linha 26 | `Role: EMPLOYEE` |
| `billing` | `payment_webhook_processing.md` | Linha 23 | Webhook Ingress perimetral |
| `exec` | `execution_queue_and_repair.md` | Linha 24 | `Role: EMPLOYEE` |
| `exec` | `materials_and_checklist.md` | Linha 23 | `Role: EMPLOYEE` |
| `work-order` | `customer_vehicle_management.md` | Linha 28 | `Role: EMPLOYEE` |
| `work-order` | `material_management.md` | Linha 19 | `Role: EMPLOYEE` |
| `work-order` | `service_catalog_management.md` | Linha 19 | `Role: ADMIN` |
| `work-order` | `work_order_lifecycle.md` | Linha 45 | Todas as requisições autenticadas |
| `saga` | `choreographed_saga.md` | Cenários 1, 2 e 3 | `Cliente` à esquerda + referência ao authorizer |
| `docs/adr` | `004-padrao-visual-diagramas-sequencia-mermaid.md` | Item 3 | Atualização da regra visual |

---

## 3. Critérios de Aceite

1. Todos os 12 pontos com notas `🔒` nos diagramas de sequência possuem a referência canônica `(vide auth_token_authorizer.md)`.
2. Os arquivos `.md` contêm o callout `[!NOTE]` com link navegável relativo para `auth_token_authorizer.md`.
3. O `choreographed_saga.md` possui o ator `Cliente` como participante da primeira coluna (extremo esquerdo) no Cenário 1 e Cenário 3.
4. Compilação de testes e recursos via `mvn process-test-resources test-compile` finalizada com **BUILD SUCCESS**.
5. Revisão e homologação pelo subagente `code-reviewer-sentinel-subagent`.
