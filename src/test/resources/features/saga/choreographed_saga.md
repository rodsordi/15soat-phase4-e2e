## Diagramas de Sequência da Saga Distribuída Coreografada

**Referência Canônica: `choreographed_saga.feature` e ADR-004**

Este documento consolida a especificação arquitetural visual da **Saga Distribuída Coreografada** via Apache Kafka entre os microsserviços `api-work-order`, `api-billing` e `api-exec`, integrados ao **Mercado Pago** e protegidos por autenticação perimetral com **Keycloak (OIDC)**.

O ecossistema adota coreografia assíncrona orientada a eventos para garantir consistência eventual em 4 dimensões:
1. **Fluxo Principal (Happy Path)**: Do ingresso do veículo à entrega com quitação e reparos concluídos.
2. **Rollback Compensatório por Recusa de Pagamento**: Cancelamento defensivo da Ordem de Serviço perante rejeição do gateway financeiro com liberação de materiais.
3. **Rollback Compensatório com Estorno Financeiro**: Falha irrecuperável na oficina dispara estorno ativo no Mercado Pago (`REFUNDED`), cancelamento da OS (`CANCELED`) e estorno de insumos reservados.
4. **Idempotência de Compensação**: Tolerância contra reentregas de mensagens (*At-Least-Once Delivery*) prevenindo estornos duplicados e inconsistência de estados.

> [!TIP]
> **Relação com o Ciclo de Vida da Ordem de Serviço**:
> Este documento foca especificamente nos cenários de **Engenharia de Sistemas Distribuídos e Padrão Saga Coreografada** (Happy Path, Rollbacks Compensatórios e Idempotência). O fluxo operacional exaustivo da oficina mecânica física (todas as 8 fases da recepção à entrega) está documentado em [`work_order_lifecycle.md`](../work-order/work_order_lifecycle.md).

> [!NOTE]
> **🔒 Autenticação & Emissão de JWT**: A obtenção e emissão de tokens Bearer JWT via Keycloak estão detalhadas no diagrama [user_authentication.md](../auth-lambda/user_authentication.md).

---

### Cenário 1: Caminho Feliz - Ciclo Completo da Saga com Sucesso

```mermaid
sequenceDiagram
    autonumber
    box rgba(100, 116, 139, 0.08) Atores de Negócio
        actor Cliente as Cliente<br/>(CUSTOMER)
        actor Atendente as Atendente<br/>(Recepção)
    end
    box rgba(59, 130, 246, 0.08) Cluster Amazon EKS (Microsserviços)
        participant OS as API Ordem de Serviço<br/>(PostgreSQL)
        participant Exec as API Execução & Oficina<br/>(MongoDB)
        participant Billing as API Faturamento<br/>(PostgreSQL)
    end
    box rgba(34, 197, 94, 0.08) Mensageria Gerenciada
        participant Kafka as Apache Kafka<br/>Broker
    end
    box rgba(249, 115, 22, 0.08) Gateway Externo
        participant MP as Mercado Pago<br/>Gateway
    end

    Note over Cliente,Billing: 🔒 Borda Autenticada via Bearer JWT (vide user_authentication.md):<br/>Cliente (CUSTOMER) e Atendente (EMPLOYEE) autenticados via Keycloak

    %% Abertura
    Note over Cliente,MP: 1. Abertura da Ordem de Serviço e Reserva Preventiva de Peças
    Atendente->>+OS: POST /api/v1/work-orders<br/>{ customerDocument,<br/>  licensePlate,<br/>  description }
    OS->>OS: Salva OS no PG (Status: RECEIVED)<br/>e reserva materiais no estoque
    OS-->>-Atendente: 201 Created (workOrderId)

    %% Aprovação Soberana na API Billing
    Note over Cliente,MP: 2. Decisão Soberana: Aprovação do Orçamento na API Billing
    Cliente->>+Billing: PATCH /api/v1/estimates/work-order/{id}<br/>{ status: "APPROVED" }
    Billing->>Billing: Atualiza Estimate para APPROVED
    Billing--)Kafka: Publica EstimateApprovedEvent<br/>(Tópico: billing-events)
    Billing-->>-Cliente: 200 OK (EstimateDetailedResponse)
    Kafka--)OS: Consome EstimateApprovedEvent<br/>(Tópico: billing-events)
    activate OS
    OS->>OS: Libera OS e atualiza para APPROVED<br/>(materiais já reservados)
    OS--)Kafka: Publica WorkOrderApprovedEvent<br/>(Tópico: work-order-events)
    deactivate OS

    %% Faturamento e Pagamento
    Note over Kafka,MP: 3. Geração de Fatura e Liquidação Mercado Pago
    Kafka--)Billing: Consome WorkOrderApprovedEvent<br/>(Tópico: work-order-events)
    activate Billing
    Billing->>Billing: Registra fatura no PostgreSQL<br/>(Status: PENDING)
    deactivate Billing
    MP->>+Billing: POST /api/v1/payments/mercadopago/webhook<br/>{ action: "payment.updated",<br/>  data: { id: "ORD-9988" } }
    Billing->>+MP: GET /v1/orders/{order_id}<br/>(Reconciliação e Verificação Ativa)
    MP-->>-Billing: Retorna status: "approved"
    Billing->>Billing: Valida idempotência e atualiza<br/>fatura para PAID
    Billing--)Kafka: Publica PaymentConfirmedEvent<br/>(Tópico: payment-events)
    Billing-->>-MP: 200 OK { status: "PROCESSED" }

    %% Execução na Oficina
    Note over Kafka,MP: 4. Execução dos Reparos na Oficina
    Kafka--)Exec: Consome PaymentConfirmedEvent<br/>(Tópico: payment-events)
    activate Exec
    Exec->>Exec: Salva ordem no MongoDB<br/>(Status: QUEUED)
    Exec--)Kafka: Publica ExecutionStartedEvent<br/>(Tópico: execution-events)
    Exec->>Exec: Mecânico executa reparos na baia<br/>(Status: COMPLETED)
    Exec--)Kafka: Publica ExecutionCompletedEvent<br/>(Tópico: execution-events)
    deactivate Exec

    %% Conclusão Final
    Note over OS,MP: 5. Sincronização Final e Conclusão da Ordem de Serviço
    Kafka--)OS: Consome ExecutionCompletedEvent<br/>(Tópico: execution-events)
    activate OS
    OS->>OS: Atualiza OS para COMPLETED<br/>e baixa materiais do estoque físico
    OS-->>Cliente: Envia e-mail informando que o<br/>veículo está finalizado (Status: COMPLETED)
    deactivate OS
    Atendente->>+OS: GET /api/v1/work-orders/{id}
    OS-->>-Atendente: 200 OK (Status: COMPLETED)
```

---

### Cenário 2: Rollback Compensatório por Recusa de Pagamento

```mermaid
sequenceDiagram
    autonumber
    box rgba(100, 116, 139, 0.08) Atores de Negócio
        actor Cliente as Cliente<br/>(Proprietário: CUSTOMER)
    end
    box rgba(59, 130, 246, 0.08) Cluster Amazon EKS (Microsserviços)
        participant OS as API Ordem de Serviço<br/>(PostgreSQL)
        participant Billing as API Faturamento<br/>(PostgreSQL)
    end
    box rgba(34, 197, 94, 0.08) Mensageria Gerenciada
        participant Kafka as Apache Kafka<br/>Broker
    end
    box rgba(249, 115, 22, 0.08) Gateway Externo
        participant MP as Mercado Pago<br/>Gateway
    end

    Note over Cliente,Billing: 🔒 Borda Autenticada via Bearer JWT (vide user_authentication.md):<br/>Requisições utilizam token OIDC emitido pelo Keycloak (Role: CUSTOMER)

    Note over Cliente,MP: 1. Orçamento Aprovado na API Billing e Fatura Gerada (Status: PENDING)
    Cliente->>+Billing: PATCH /api/v1/estimates/work-order/{id}<br/>{ status: "APPROVED" }
    Billing->>Billing: Atualiza Estimate para APPROVED
    Billing--)Kafka: Publica EstimateApprovedEvent<br/>(Tópico: billing-events)
    Billing-->>-Cliente: 200 OK (EstimateDetailedResponse)
    Kafka--)OS: Consome EstimateApprovedEvent
    activate OS
    OS->>OS: Libera OS para APPROVED
    OS--)Kafka: Publica WorkOrderApprovedEvent<br/>(Tópico: work-order-events)
    deactivate OS
    Kafka--)Billing: Consome WorkOrderApprovedEvent<br/>(Tópico: work-order-events)
    activate Billing
    Billing->>Billing: Cria Fatura PENDING no PG
    deactivate Billing

    Note over MP,Kafka: 2. Pagamento Recusado / Cancelado pelo Gateway
    MP->>+Billing: POST /api/v1/payments/mercadopago/webhook<br/>{ action: "payment.updated",<br/>  data: { id: "ORD-9988-REJECT" } }
    Billing->>+MP: GET /v1/orders/{order_id}<br/>(Reconciliação e Verificação Ativa)
    MP-->>-Billing: Retorna status: "rejected"
    Billing->>Billing: Valida idempotência e atualiza<br/>fatura (Status: FAILED)
    Billing--)Kafka: Publica PaymentFailedEvent<br/>(Tópico: payment-events)
    Billing-->>-MP: 200 OK { status: "PROCESSED" }

    Note over Kafka,OS: 3. Transação Compensatória (Saga Rollback)
    Kafka--)OS: Consome PaymentFailedEvent<br/>(Tópico: payment-events)
    activate OS
    OS->>OS: Cancela Ordem de Serviço (Status: CANCELED)<br/>e estorna reserva de materiais no estoque
    OS-->>Cliente: Envia e-mail informando cancelamento<br/>por recusa financeira
    deactivate OS
    Note over OS: Rollback garantido sem<br/>inconsistência distribuída!
```

---

### Cenário 3: Rollback Compensatório com Estorno Financeiro por Falha na Oficina

```mermaid
sequenceDiagram
    autonumber
    box rgba(100, 116, 139, 0.08) Atores de Negócio
        actor Cliente as Cliente<br/>(Proprietário: CUSTOMER)
        actor Mecanico as Mecânico / Técnico<br/>(Oficina: EMPLOYEE)
    end
    box rgba(59, 130, 246, 0.08) Cluster Amazon EKS (Microsserviços)
        participant OS as API Ordem de Serviço<br/>(PostgreSQL)
        participant Exec as API Execução & Oficina<br/>(MongoDB)
        participant Billing as API Faturamento<br/>(PostgreSQL)
    end
    box rgba(34, 197, 94, 0.08) Mensageria Gerenciada
        participant Kafka as Apache Kafka<br/>Broker
    end
    box rgba(249, 115, 22, 0.08) Gateway Externo
        participant MP as Mercado Pago<br/>Gateway
    end

    Note over Cliente,Billing: 🔒 Borda Autenticada via Bearer JWT (vide user_authentication.md):<br/>Requisições utilizam token OIDC emitido pelo Keycloak (Roles: CUSTOMER / EMPLOYEE)

    Note over Cliente,MP: 1. Ordem em Reparo após Pagamento Confirmado (Fatura: PAID)
    Mecanico->>+Exec: PATCH /api/v1/executions/{id}/status<br/>{ status: "FAILED",<br/>  reason: "Bloco do motor trincado" }
    Exec->>Exec: Atualiza execução para FAILED<br/>no MongoDB
    Exec--)Kafka: Publica ExecutionFailedEvent<br/>(Tópico: execution-events)
    Exec-->>-Mecanico: 200 OK (Falha Registrada)

    Note over Kafka,MP: 2. Transação Compensatória Financeira (Selective Consumer Pattern)
    Kafka--)Billing: Consome ExecutionFailedEvent<br/>(Tópico: execution-events)
    activate Billing
    Billing->>+MP: POST /v1/orders/{order_id}/refunds<br/>(Solicitação de Estorno / Reversal)
    MP-->>-Billing: 200 OK (Estorno Confirmado)
    Billing->>Billing: Valida idempotência e atualiza<br/>fatura para REFUNDED
    deactivate Billing

    Note over OS,Kafka: 3. Cancelamento Compensatório da Ordem de Serviço
    Kafka--)OS: Consome ExecutionFailedEvent<br/>(Tópico: execution-events)
    activate OS
    OS->>OS: Cancela Ordem de Serviço (Status: CANCELED)<br/>e estorna reserva de materiais no estoque
    OS-->>Cliente: Notifica cancelamento da OS e<br/>estorno do pagamento por e-mail
    deactivate OS
    Note over Cliente,OS: Consistência eventual garantida:<br/>Fatura REFUNDED & OS CANCELED!
```

---

### Cenário 4: Idempotência do Estorno Compensatório perante Notificações Repetidas

```mermaid
sequenceDiagram
    autonumber
    box rgba(59, 130, 246, 0.08) Cluster Amazon EKS (Microsserviços)
        participant OS as API Ordem de Serviço<br/>(PostgreSQL)
        participant Billing as API Faturamento<br/>(PostgreSQL)
    end
    box rgba(34, 197, 94, 0.08) Mensageria Gerenciada
        participant Kafka as Apache Kafka<br/>Broker
    end
    box rgba(249, 115, 22, 0.08) Gateway Externo
        participant MP as Mercado Pago<br/>Gateway
    end

    Note over OS,MP: 1. Redelivery de Mensagem de Falha (At-Least-Once Delivery)
    Kafka--)Billing: Reentrega ExecutionFailedEvent<br/>(Mesmo workOrderId e traceId)
    activate Billing
    Billing->>Billing: Consulta Fatura no PG<br/>(Status atual: REFUNDED)
    Note over Billing: Fatura já estornada!<br/>Selective/Idempotent Consumer descarta<br/>sem acionar novo estorno no gateway
    deactivate Billing

    Note over OS,MP: 2. Reentrega na API Ordem de Serviço
    Kafka--)OS: Reentrega ExecutionFailedEvent<br/>(Mesmo workOrderId e traceId)
    activate OS
    OS->>OS: Consulta Ordem de Serviço no PG<br/>(Status atual: CANCELED)
    Note over OS: Transição redundante descartada<br/>(Máquina de estados defensiva)
    deactivate OS
    Note over MP: Zero estornos duplicados<br/>Zero chamadas espúrias ao gateway!
```
