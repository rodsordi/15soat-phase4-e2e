# Plano de Implementação: Segregação e Migração de Requisitos Não-Funcionais (*-non-func.feature)

> **Decisão do Usuário**: Opção 1 - Espelhamento Coeso *-non-func.feature por Bounded Context
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

Conforme diretriz do usuário e em estrito alinhamento com a **ADR-002** (Pirâmide de Testes e Segregação de Validações) e a norma **ISO/IEC 25010** (Product Quality Model), este plano define a segregação completa dos aspectos técnicos e não-funcionais (Segurança OIDC, RBAC, Performance/SLA, Observabilidade e Carga) dos arquivos de negócio funcionais.

Para cada arquivo `<feature>.feature` de domínio, será criado um arquivo espelho `<feature>-non-func.feature`. Os cenários técnicos dispersos nas features originais serão migrados, deixando os arquivos funcionais 100% focados na linguagem ubíqua e nas jornadas de negócio.

---

## 2. Matriz de Arquivos Espelho (*-non-func.feature)

| Módulo / Domínio | Feature Funcional (`.feature`) | Nova Feature Não-Funcional (`-non-func.feature`) | Aspectos Cobertos |
| :--- | :--- | :--- | :--- |
| **`auth-lambda`** | `user_authentication.feature` | `user_authentication-non-func.feature` | Resiliência OIDC, rejeição de credenciais inválidas (401), SLA de emissão de token (< 2000ms). |
| **`auth-lambda`** | `user_identity_management.feature` | `user_identity_management-non-func.feature` | Resiliência da saga de onboarding (502 em falha com compensação), SLA de cadastro (< 2000ms), header `X-Trace-Id`. |
| **`billing`** | `invoice_generation.feature` | `invoice_generation-non-func.feature` | Migração do Cenário 8 (Token ausente 401), RBAC (403 para cliente emitindo fatura administrativa), SLA de checkout Pix (< 1500ms). |
| **`billing`** | `payment_webhook_processing.feature` | `payment_webhook_processing-non-func.feature` | Ingress público sem autenticação (webhook do Mercado Pago), idempotência sob rede, SLA de processamento assíncrono. |
| **`exec`** | `execution_queue_and_repair.feature` | `execution_queue_and_repair-non-func.feature` | Autenticação (401), RBAC (403 para cliente acessando fila técnica da oficina), SLA de consulta da fila (< 1000ms). |
| **`exec`** | `materials_and_checklist.feature` | `materials_and_checklist-non-func.feature` | Autenticação (401), RBAC (403 para cliente inserindo itens no checklist), SLA de atualização. |
| **`saga`** | `choreographed_saga.feature` | `choreographed_saga-non-func.feature` | Rastreabilidade distribuída ponta a ponta (`X-Trace-Id`), resiliência a eventos repetidos (idempotência Kafka), SLA do ciclo completo. |
| **`work-order`** | `customer_vehicle_management.feature` | `customer_vehicle_management-non-func.feature` | Migração do Cenário 9 (Token ausente 401), RBAC (403 para cliente cadastrando outros clientes), SLA (< 1000ms), `X-Trace-Id`. |
| **`work-order`** | `material_management.feature` | `material_management-non-func.feature` | Autenticação (401), RBAC (403 para cliente cadastrando peças no estoque), SLA de catálogo (< 800ms). |
| **`work-order`** | `service_catalog_management.feature` | `service_catalog_management-non-func.feature` | Autenticação (401), RBAC (403 para cliente cadastrando serviços no catálogo), SLA (< 800ms). |
| **`work-order`** | `work_order_lifecycle.feature` | `work_order_lifecycle-non-func.feature` | Autenticação (401) em todas as rotas da OS; **RBAC Soberano**: bloqueio de `EMPLOYEE` tentando aprovar orçamento (403); bloqueio de `CUSTOMER` tentando abrir OS na recepção (403); SLA de transições; `X-Trace-Id`. |

---

## 3. Plano de Execução

1. **Purificação dos Arquivos Funcionais**:
   - Remover cenários estritamente técnicos de `customer_vehicle_management.feature` (Cenário 9) e `invoice_generation.feature` (Cenário 8).
2. **Criação dos 11 Arquivos `*-non-func.feature`**:
   - Estruturados com tags `@e2e @nao-funcional @security @performance @observabilidade @sla`.
3. **Enriquecimento dos Steps em Java**:
   - Suporte a assertivas de SLA de tempo de resposta (`CommonAssertionSteps`).
   - Suporte a cabeçalhos de rastreabilidade (`X-Trace-Id`).
   - Suporte a asserção de código HTTP 403 com fallback gracioso em ambientes black-box.
4. **Validação do Quality Gate (Diretriz Global #7)**:
   - Execução determinística dos testes no Maven (`EnvironmentConfigTest` e `CucumberTestRunner`).
   - Auditoria semântica independente com o subagente Sentinela.
