# Bateria de Testes End-to-End (BDD) e Testes de Carga/Estresse (`15soat-phase4-e2e`)

Repositório centralizado de testes automatizados para a Fase 4 do Tech Challenge da pós-graduação em Software Architecture (SOAT) da FIAP, orquestrando testes comportamentais de ponta a ponta (BDD via Cucumber) entre os três microsserviços, Lambdas Serverless e testes de estresse de performance dedicados por fluxo funcional (com 100% de paridade em relação à Fase 3).

## Tecnologias
- **Java 25 LTS**
- **Cucumber 7.x** (Cenários BDD Gherkin em português, estruturados segundo a especificação de negócio)
- **RestAssured 5.x** (Validação de contratos de API REST e asserções HTTP)
- **k6** (1 script de teste de carga e estresse dedicado para cada fluxo funcional compartilhando o mesmo nome base)
- **Diagramas de Sequência em Mermaid** (Acompanhando cada arquivo `.feature` em formato markdown responsivo)

---

## 🏛️ Arquitetura Macro do Sistema (C4 Model)

O sistema de gestão de oficina mecânica da **Fase 4** é construído sob uma **Arquitetura Orientada a Eventos (Event-Driven Architecture - EDA)**, com microsserviços desacoplados, padrão *Database-per-Service* com persistência poliglota, orquestração de transações distribuídas via *Choreographed Saga* no Apache Kafka e infraestrutura automatizada no Amazon EKS.

A arquitetura macro está detalhada a seguir segundo os níveis canônicos do **C4 Model** (Simon Brown):

### 1. Nível 1: Diagrama de Contexto de Sistema (System Context)

Apresenta os usuários do sistema, os limites da solução de oficina mecânica e a integração com sistemas externos de pagamento:

<div style="overflow-x: auto; width: 100%;">
<style>
  .mermaid svg {
    max-width: none !important;
    min-width: 1200px !important;
    width: 1200px !important;
  }
</style>
<div style="min-width: 1200px;">

```mermaid
C4Context
    title Diagrama de Contexto de Sistema - Sistema de Gestão de Oficina Mecânica (Fase 4)

    Enterprise_Boundary(b_users, "1. Atores de Negócio (Usuários & Operadores)") {
        Person(customer, "Cliente da Oficina", "Aprova orçamentos, acompanha<br/>o status de OS e realiza pagamentos.")
        Person(mechanic, "Mecânico / Atendente", "Abre ordens de serviço, inspeciona<br/>veículos e executa manutenções.")
    }

    Enterprise_Boundary(b_system, "2. Escopo da Solução (Fase 4)") {
        System(workshopSystem, "Sistema de Gestão<br/>de Oficina Mecânica", "Plataforma distribuída em microsserviços<br/>para orquestração de OS, execução<br/>e faturamento.")
    }

    Enterprise_Boundary(b_external, "3. Ecossistema Externo de Pagamentos") {
        System_Ext(mercadopago, "Mercado Pago", "Gateway externo para processamento<br/>de pagamentos Pix e notificações Webhook.")
    }

    Rel_Down(customer, workshopSystem, "Aprova orçamentos e consulta OS", "HTTPS / JSON")
    Rel_Down(mechanic, workshopSystem, "Gerencia ordens de serviço e manutenções", "HTTPS / JSON")
    Rel_Down(workshopSystem, mercadopago, "Solicita ordens de pagamento Pix e<br/>recebe liquidação via Webhook", "HTTPS / REST")

    UpdateLayoutConfig($c4ShapeInRow="2", $c4BoundaryInRow="1")
```
</div>
</div>

---

### 2. Nível 2: Diagrama de Contêineres (Containers)

Detalhamento de todos os contêineres de aplicação, mensageria distribuída, bancos de dados relacionais e NoSQL, proxies de borda e funções serverless:

<div style="overflow-x: auto; width: 100%;">
<style>
  .mermaid svg {
    max-width: none !important;
    min-width: 1850px !important;
    width: 1850px !important;
  }
</style>
<div style="min-width: 1850px;">

```mermaid
C4Container
    title Diagrama de Contêineres - Sistema de Gestão de Oficina Mecânica (Fase 4)

    Enterprise_Boundary(b_actors, "1. Atores & Clientes") {
        Person(customer, "Cliente da Oficina", "Aprova orçamentos e realiza<br/>pagamentos Pix.")
        Person(mechanic, "Mecânico / Atendente", "Abre ordens de serviço, inspeciona<br/>veículos e registra manutenções.")
    }

    Container_Boundary(b_edge, "2. Camada de Borda & Serverless (iac-lambda)") {
        Container(nlb, "AWS Network Load Balancer (NLB)", "AWS NLB", "Entrada de rede de alta performance (L4)<br/>para o cluster EKS.")
        Container(apigw, "AWS HTTP API Gateway v2", "AWS API Gateway", "Ponto de entrada público para rotas<br/>serverless e recepção de webhooks.")
        Container(authorizer, "Lambda Token Authorizer", "Node.js 20 / Lambda", "Validação de tokens de autorização<br/>e emissão de contexto seguro.")
        Container(webhook, "Lambda Mercado Pago Webhook", "Node.js 20 / Lambda", "Recepção e validação de callbacks<br/>de notificação de pagamento Pix.")
    }

    Container_Boundary(eksCluster, "3. Plataforma Kubernetes Amazon EKS 1.30 (iac-k8s)") {
        Container(kong, "Kong Ingress Controller", "Kong Ingress / Proxy NLB", "API Gateway Ingress para roteamento<br/>HTTP L7 aos microsserviços.")

        Container(api_wo, "api-work-order", "Spring Boot 4 / Java 25", "Gestão de clientes, veículos,<br/>triagem e ciclo de vida de OS.")
        Container(api_billing, "api-billing", "Spring Boot 4 / Java 25", "Faturamento, emissão de faturas,<br/>integração Pix e reconciliação.")
        Container(api_exec, "api-exec", "Spring Boot 4 / Java 25", "Fila de oficina, inspeções,<br/>alocação de peças e execução.")

        ContainerQueue(kafka, "Apache Kafka Cluster", "Strimzi KRaft (3 brokers)", "Cluster de mensageria para<br/>coreografia assíncrona da saga.")
        ContainerDb(mongo, "MongoDB StatefulSet (exec_db)", "MongoDB 7.0 Community", "Armazenamento NoSQL de documentos<br/>de inspeção e ordens de execução.")
    }

    Container_Boundary(b_data, "4. Bancos Relacionais AWS RDS & Governança de Segredos (iac-db)") {
        ContainerDb(rds_wo, "RDS PostgreSQL (work_order_db)", "PostgreSQL 16.10", "Banco relacional isolado (Database-per-Service)<br/>para api-work-order.")
        ContainerDb(rds_billing, "RDS PostgreSQL (billing_db)", "PostgreSQL 16.10", "Banco relacional isolado (Database-per-Service)<br/>para api-billing.")
        Container(secrets, "AWS Secrets Manager", "AWS KMS / Secrets", "Gestão e rotação centralizada de<br/>credenciais e connection strings.")
    }

    Enterprise_Boundary(b_external, "5. Provedor Externo de Pagamentos") {
        System_Ext(mercadopago, "Mercado Pago", "Gateway externo de pagamentos Pix<br/>e envio de notificações via Webhook.")
    }

    Rel_Down(customer, nlb, "Consulta OS e aprova orçamentos", "HTTPS / JSON")
    Rel_Down(mechanic, nlb, "Gerencia ordens de serviço<br/>e manutenções", "HTTPS / JSON")

    Rel(apigw, authorizer, "Invoca validação<br/>de autenticação", "AWS Lambda Invoke")
    Rel(apigw, webhook, "Roteia payloads de<br/>notificação Pix", "AWS Lambda Invoke")
    Rel(webhook, nlb, "Encaminha notificação<br/>confirmada", "HTTPS / JSON")

    Rel_Down(nlb, kong, "Encaminha tráfego balanceado L4/L7", "TCP / 80/443")
    Rel_Down(kong, api_wo, "Roteia /api/v1/work-orders", "HTTP / REST")
    Rel_Down(kong, api_billing, "Roteia /api/v1/invoices", "HTTP / REST")
    Rel_Down(kong, api_exec, "Roteia /api/v1/executions", "HTTP / REST")

    Rel(api_wo, kafka, "Publica: work-order.approved<br/>Consome: payment.failed,<br/>execution.completed", "Kafka Protocol")
    Rel(api_billing, kafka, "Consome: work-order.approved<br/>Publica: payment.confirmed,<br/>payment.failed", "Kafka Protocol")
    Rel(api_exec, kafka, "Consome: payment.confirmed<br/>Publica: execution.completed", "Kafka Protocol")

    Rel_Down(api_wo, rds_wo, "Persiste ordens de serviço<br/>e veículos", "JDBC / HikariCP")
    Rel_Down(api_billing, rds_billing, "Persiste faturas e status<br/>de pagamento", "JDBC / HikariCP")
    Rel(api_exec, mongo, "Persiste histórico de execuções", "mongodb://")

    Rel(rds_wo, secrets, "Credenciais gerenciadas", "AWS KMS")
    Rel(rds_billing, secrets, "Credenciais gerenciadas", "AWS KMS")
    Rel(mongo, secrets, "Credenciais gerenciadas", "AWS KMS")

    Rel_Down(api_billing, mercadopago, "Gera ordem de pagamento<br/>e QR Code Pix", "HTTPS / REST")
    Rel(mercadopago, apigw, "Dispara notificações<br/>de liquidação (Webhook)", "HTTPS / Webhook")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```
</div>
</div>

---

### 3. Visão Dinâmica: Coreografia da Saga Distribuída (Choreographed Saga)

Ilustra o comportamento temporal e as trocas assíncronas de mensagens via Apache Kafka entre os microsserviços durante o ciclo de vida da Ordem de Serviço:

<div style="overflow-x: auto; width: 100%;">
<style>
  .mermaid svg {
    max-width: none !important;
    min-width: 1450px !important;
    width: 1450px !important;
  }
</style>
<div style="min-width: 1450px;">

```mermaid
sequenceDiagram
    autonumber
    participant WO as api-work-order<br/>(Spring Boot 4 / PG)
    participant K as Apache Kafka<br/>Broker (KRaft)
    participant B as api-billing<br/>(Spring Boot 4 / PG)
    participant MP as Mercado Pago<br/>Gateway (Pix)
    participant E as api-exec<br/>(Spring Boot 4 / Mongo)

    Note over WO,E: 1. Ciclo Comercial & Início da Saga
    WO->>K: Publica WorkOrderApprovedEvent<br/>(Tópico: work-order.approved)
    K->>B: Consome WorkOrderApprovedEvent
    B->>+MP: Gera Fatura e Solicita QR Code Pix
    MP-->>-B: Retorna QR Code Pix para Pagamento

    alt Cenário de Sucesso (Happy Path)
        MP->>B: Webhook: Pagamento Confirmado
        B->>K: Publica PaymentConfirmedEvent<br/>(Tópico: payment.confirmed)
        K->>E: Consome PaymentConfirmedEvent
        E->>E: Enfileira OS e Executa Reparo na Oficina
        E->>K: Publica ExecutionCompletedEvent<br/>(Tópico: execution.completed)
        K->>WO: Consome ExecutionCompletedEvent
        WO->>WO: Finaliza OS<br/>(Status: COMPLETED / DELIVERED)
    else Cenário de Falha & Compensação (Rollback)
        MP->>B: Webhook: Pagamento Rejeitado / Expirado
        B->>K: Publica PaymentFailedEvent<br/>(Tópico: payment.failed)
        K->>WO: Consome PaymentFailedEvent
        WO->>WO: Estorna Reserva de Peças<br/>& Cancela OS (Status: CANCELED)
    end
```
</div>
</div>

---

## Funcionalidades, Diagramas & Testes de Carga (1:1 por Fluxo)

### 1. Microsserviço de Ordens de Serviço (`features/work-order/`)
- 📊 **Modelo de Dados & MER**: [`MODEL.md`](../15soat-phase4-api-work-order/MODEL.md) *(PostgreSQL - Clientes, Veículos, OS, Itens com Snapshot e Histórico)*
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
- 📊 **Modelo de Dados & MER**: [`MODEL.md`](../15soat-phase4-api-billing/MODEL.md) *(PostgreSQL - Faturas, Idempotência e Webhooks)*
- **Geração de Fatura e Checkout**:
  - Especificação BDD: [`invoice_generation.feature`](src/test/resources/features/billing/invoice_generation.feature)
  - Diagrama de Sequência: [`invoice_generation.md`](src/test/resources/features/billing/invoice_generation.md)
  - Teste de Carga (k6): [`invoice_generation.js`](src/test/resources/features/billing/invoice_generation.js)
- **Processamento de Webhook de Pagamento e Liquidação**:
  - Especificação BDD: [`payment_webhook_processing.feature`](src/test/resources/features/billing/payment_webhook_processing.feature)
  - Diagrama de Sequência: [`payment_webhook_processing.md`](src/test/resources/features/billing/payment_webhook_processing.md)
  - Teste de Carga (k6): [`payment_webhook_processing.js`](src/test/resources/features/billing/payment_webhook_processing.js)

### 3. Microsserviço de Execução de Oficina (`features/exec/`)
- 📊 **Modelo de Dados NoSQL**: [`MODEL.md`](../15soat-phase4-api-exec/MODEL.md) *(MongoDB - Agregado de Execução, Checklists e Peças Embutidas)*
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
