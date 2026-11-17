# Documentação Viva: Ciclo de Vida do Orçamento na API Billing

## Visão Geral

Este documento descreve a governança de orçamentos (*Estimates*) centralizada no microsserviço `15soat-phase4-api-billing`. O orçamento é originado a partir da conclusão do diagnóstico técnico na oficina (`15soat-phase4-api-work-order`) e submetido à decisão soberana do cliente.

> [!NOTE]
> **🔒 Autenticação & Emissão de JWT**: A obtenção e emissão de tokens Bearer JWT via Keycloak estão detalhadas no diagrama [user_authentication.md](../auth-lambda/user_authentication.md).

---

```mermaid
sequenceDiagram
    autonumber
    box rgba(100, 116, 139, 0.08) Atores de Negócio
        actor Customer as Cliente<br/>(CUSTOMER)
    end
    box rgba(59, 130, 246, 0.08) Cluster Amazon EKS (Microsserviços)
        participant WO as API Work Order<br/>(PostgreSQL)
        participant Billing as API Billing<br/>(PostgreSQL)
    end
    box rgba(34, 197, 94, 0.08) Mensageria Gerenciada
        participant Kafka as Apache Kafka<br/>Broker
    end

    Note over Customer,Billing: 🔒 Borda Autenticada via Bearer JWT (vide user_authentication.md):<br/>Requisições utilizam token OIDC emitido pelo Keycloak (Roles: CUSTOMER / EMPLOYEE)

    %% -------------------------------------------------------------------------
    %% 1. Levantamento Técnico e Criação do Orçamento
    %% -------------------------------------------------------------------------
    Note over WO,Billing: 1. Conclusão do Diagnóstico e Envio do Orçamento para a API Billing
    WO--)Kafka: Publica WorkOrderEstimateRequestedEvent<br/>(Tópico: work-order-events)
    Kafka--)Billing: Consome WorkOrderEstimateRequestedEvent
    activate Billing
    Billing->>Billing: Salva Estimate no PG (Status: PENDING_APPROVAL)<br/>com itens de serviços e peças
    Billing-->>Customer: Envia e-mail com orçamento detalhado<br/>e link seguro de decisão soberana
    deactivate Billing

    %% -------------------------------------------------------------------------
    %% 2. Consulta Detalhada do Orçamento
    %% -------------------------------------------------------------------------
    Note over Customer,Billing: 2. Consulta do Orçamento pelo Cliente
    Customer->>+Billing: GET /api/v1/estimates/work-order/{id}
    Billing-->>-Customer: 200 OK (EstimateDetailedResponse com itens e status PENDING_APPROVAL)

    %% -------------------------------------------------------------------------
    %% 3. Decisão Soberana do Cliente
    %% -------------------------------------------------------------------------
    Note over Customer,WO: 3. Decisão Soberana do Cliente na API Billing (Aprovação ou Rejeição)
    alt Cliente Aprova o Orçamento (Status OS: APPROVED)
        Customer->>+Billing: PATCH /api/v1/estimates/work-order/{id}<br/>{ status: "APPROVED" }
        Billing->>Billing: Atualiza Estimate para APPROVED
        Billing--)Kafka: Publica EstimateApprovedEvent<br/>(Tópico: billing-events)
        Billing-->>-Customer: 200 OK (EstimateDetailedResponse)
        Kafka--)WO: Consome EstimateApprovedEvent
        activate WO
        WO->>WO: Libera OS e atualiza status para APPROVED<br/>(reserva materiais no estoque)
        WO-->>Customer: Envia e-mail confirmando aprovação<br/>do orçamento e início dos reparos
        deactivate WO
    else Cliente Rejeita o Orçamento (Status OS: CANCELED)
        Customer->>+Billing: PATCH /api/v1/estimates/work-order/{id}<br/>{ status: "REJECTED", reason: "Orçamento elevado" }
        Billing->>Billing: Atualiza Estimate para REJECTED
        Billing--)Kafka: Publica EstimateRejectedEvent<br/>(Tópico: billing-events)
        Billing-->>-Customer: 200 OK (EstimateDetailedResponse)
        Kafka--)WO: Consome EstimateRejectedEvent
        activate WO
        WO->>WO: Transiciona OS para CANCELED<br/>e estorna materiais reservados
        WO-->>Customer: Envia e-mail confirmando cancelamento<br/>e liberação para retirada sem reparos
        deactivate WO
    end
```

---

## Contratos REST

### `GET /api/v1/estimates/work-order/{workOrderId}`
- **Descrição**: Retorna o orçamento detalhado da Ordem de Serviço, com status atual e itens discriminados.
- **Autenticação**: Requer perfil `CUSTOMER` ou `EMPLOYEE`.

### `PATCH /api/v1/estimates/work-order/{workOrderId}`
- **Descrição**: Processa a decisão soberana do cliente sobre o orçamento (`APPROVED` ou `REJECTED`).
- **Autenticação**: Requer perfil `CUSTOMER` (SoD - funcionários não podem aprovar).
- **Efeitos Colaterais**: Publicação de `EstimateApprovedEvent` ou `EstimateRejectedEvent` no tópico `billing-events`.
