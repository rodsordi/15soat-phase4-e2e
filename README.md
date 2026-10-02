# Bateria de Testes End-to-End (BDD) e Testes de Carga/Estresse (`15soat-phase4-e2e`)

Repositório centralizado de testes automatizados para a Fase 4 do Tech Challenge da pós-graduação em Software Architecture (SOAT) da FIAP, orquestrando testes comportamentais de ponta a ponta (BDD via Cucumber) entre os três microsserviços, Lambdas Serverless e testes de estresse de performance dedicados por fluxo funcional (com 100% de paridade em relação à Fase 3).

## Tecnologias
- **Java 25 LTS**
- **Cucumber 7.x** (Cenários BDD Gherkin em português, estruturados segundo a especificação de negócio)
- **RestAssured 5.x** (Validação de contratos de API REST e asserções HTTP)
- **k6** (1 script de teste de carga e estresse dedicado para cada fluxo funcional compartilhando o mesmo nome base)
- **Diagramas de Sequência em Mermaid** (Acompanhando cada arquivo `.feature` em formato markdown responsivo)

## Funcionalidades, Diagramas & Testes de Carga (1:1 por Fluxo)

### 1. Microsserviço de Ordens de Serviço (`features/work-order/`)
- **Gestão de Clientes e Veículos**:
  - Especificação BDD: [`customer_vehicle_management.feature`](src/test/resources/features/work-order/customer_vehicle_management.feature)
  - Diagrama de Sequência: [`customer_vehicle_management.md`](src/test/resources/features/work-order/customer_vehicle_management.md)
  - Teste de Carga (k6): [`customer_vehicle_management.js`](src/test/resources/features/work-order/customer_vehicle_management.js)
- **Gestão do Catálogo de Serviços**:
  - Especificação BDD: [`service_catalog_management.feature`](src/test/resources/features/work-order/service_catalog_management.feature)
  - Diagrama de Sequência: [`service_catalog_management.md`](src/test/resources/features/work-order/service_catalog_management.md)
  - Teste de Carga (k6): [`service_catalog_management.js`](src/test/resources/features/work-order/service_catalog_management.js)
- **Ciclo de Vida Completo da Ordem de Serviço (7 Etapas)**:
  - Especificação BDD: [`work_order_lifecycle.feature`](src/test/resources/features/work-order/work_order_lifecycle.feature)
  - Diagrama de Sequência: [`work_order_lifecycle.md`](src/test/resources/features/work-order/work_order_lifecycle.md)
  - Teste de Carga (k6): [`work_order_lifecycle.js`](src/test/resources/features/work-order/work_order_lifecycle.js)

### 2. Microsserviço de Faturamento e Pagamentos (`features/billing/`)
- **Geração de Fatura e Checkout**:
  - Especificação BDD: [`invoice_generation.feature`](src/test/resources/features/billing/invoice_generation.feature)
  - Diagrama de Sequência: [`invoice_generation.md`](src/test/resources/features/billing/invoice_generation.md)
  - Teste de Carga (k6): [`invoice_generation.js`](src/test/resources/features/billing/invoice_generation.js)
- **Processamento de Webhook de Pagamento e Liquidação**:
  - Especificação BDD: [`payment_webhook_processing.feature`](src/test/resources/features/billing/payment_webhook_processing.feature)
  - Diagrama de Sequência: [`payment_webhook_processing.md`](src/test/resources/features/billing/payment_webhook_processing.md)
  - Teste de Carga (k6): [`payment_webhook_processing.js`](src/test/resources/features/billing/payment_webhook_processing.js)

### 3. Microsserviço de Execução de Oficina (`features/exec/`)
- **Materiais e Checklist de Inspeção Técnica**:
  - Especificação BDD: [`materials_and_checklist.feature`](src/test/resources/features/exec/materials_and_checklist.feature)
  - Diagrama de Sequência: [`materials_and_checklist.md`](src/test/resources/features/exec/materials_and_checklist.md)
  - Teste de Carga (k6): [`materials_and_checklist.js`](src/test/resources/features/exec/materials_and_checklist.js)
- **Fila de Execução e Progressão do Reparo**:
  - Especificação BDD: [`execution_queue_and_repair.feature`](src/test/resources/features/exec/execution_queue_and_repair.feature)
  - Diagrama de Sequência: [`execution_queue_and_repair.md`](src/test/resources/features/exec/execution_queue_and_repair.md)
  - Teste de Carga (k6): [`execution_queue_and_repair.js`](src/test/resources/features/exec/execution_queue_and_repair.js)

### 4. Padrão Saga Distribuída Coreografada (`features/saga/`)
- **Saga Coreografada - Caminho Feliz (Happy Path)**:
  - Especificação BDD: [`complete_choreographed_saga.feature`](src/test/resources/features/saga/complete_choreographed_saga.feature)
  - Diagrama de Sequência: [`complete_choreographed_saga.md`](src/test/resources/features/saga/complete_choreographed_saga.md)
  - Teste de Carga (k6): [`complete_choreographed_saga.js`](src/test/resources/features/saga/complete_choreographed_saga.js)
- **Saga Coreografada - Rollback Compensatório por Falha de Pagamento**:
  - Especificação BDD: [`compensating_saga_rollback.feature`](src/test/resources/features/saga/compensating_saga_rollback.feature)
  - Diagrama de Sequência: [`compensating_saga_rollback.md`](src/test/resources/features/saga/compensating_saga_rollback.md)
  - Teste de Carga (k6): [`compensating_saga_rollback.js`](src/test/resources/features/saga/compensating_saga_rollback.js)

### 5. Lambda Serverless de Autenticação (`features/auth-lambda/`)
- **Token Authorizer e Segurança da API**:
  - Especificação BDD: [`auth_token_authorizer.feature`](src/test/resources/features/auth-lambda/auth_token_authorizer.feature)
  - Diagrama de Sequência: [`auth_token_authorizer.md`](src/test/resources/features/auth-lambda/auth_token_authorizer.md)
  *(Observação: conforme especificação arquitetural, funções serverless Lambda não possuem scripts de carga k6).*

---

## Execução dos Testes Automatizados

### Ambiente Local
```bash
mvn clean test -Denv=local
```

### Ambiente Remoto / Kubernetes em Produção (Modo Black-Box Estrito)
```bash
mvn clean test -Denv=prd -Dgateway.url=http://api.fiap-oficina.local
```

### Execução dos Testes de Carga e Estresse (k6)
```bash
# Fluxos de Ordem de Serviço
k6 run src/test/resources/features/work-order/customer_vehicle_management.js
k6 run src/test/resources/features/work-order/service_catalog_management.js
k6 run src/test/resources/features/work-order/work_order_lifecycle.js

# Fluxos de Faturamento e Pagamentos
k6 run src/test/resources/features/billing/invoice_generation.js
k6 run src/test/resources/features/billing/payment_webhook_processing.js

# Fluxos de Execução da Oficina
k6 run src/test/resources/features/exec/materials_and_checklist.js
k6 run src/test/resources/features/exec/execution_queue_and_repair.js

# Fluxos da Saga Coreografada
k6 run src/test/resources/features/saga/complete_choreographed_saga.js
k6 run src/test/resources/features/saga/compensating_saga_rollback.js
```
