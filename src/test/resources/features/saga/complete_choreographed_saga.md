## Diagrama de Sequência

**Coreografia Completa da Saga Distribuída - Happy Path (Referência: `complete_choreographed_saga.feature`)**

Este diagrama documenta a coordenação distribuída assíncrona orientada a eventos via Apache Kafka entre os microsserviços `api-work-order`, `api-billing` e `api-exec` no fluxo feliz completo.

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 1100px;">

```mermaid
sequenceDiagram
    autonumber
    actor Atendente as Atendente<br/>(Recepção: EMPLOYEE)
    participant Kong as Kong<br/>API Gateway
    participant OS as API Ordem de Serviço<br/>(PostgreSQL)
    participant Kafka as Apache Kafka<br/>Broker
    participant Billing as API Faturamento<br/>(PostgreSQL)
    participant MP as Mercado Pago<br/>Gateway
    participant Exec as API Execução & Oficina<br/>(MongoDB)

    Note over Atendente,Kong: 🔒 Sessão Autenticada: Requisições utilizam Bearer JWT (Role: EMPLOYEE)

    %% Abertura
    Note over Atendente,OS: 1. Abertura da Ordem de Serviço
    Atendente->>+Kong: POST /api/v1/work-orders<br/>{ customerDocument, licensePlate, description }
    Kong->>+OS: Proxy Request
    OS->>OS: Salva OS no PG (Status: RECEIVED)
    OS-->>-Kong: 201 Created (workOrderId)
    Kong-->>-Atendente: 201 Created

    %% Aprovação
    Note over Atendente,Kafka: 2. Aprovação de Orçamento
    Atendente->>+Kong: PATCH /api/v1/work-orders/{id}/status<br/>{ status: "APPROVED", totalAmount: 500.00 }
    Kong->>+OS: Proxy Request
    OS->>OS: Atualiza OS (Status: APPROVED)
    OS->>Kafka: Publica WorkOrderApprovedEvent (Tópico: work-order-events)
    OS-->>-Kong: 200 OK
    Kong-->>-Atendente: 200 OK

    %% Faturamento e Pagamento
    Note over Kafka,MP: 3. Geração de Fatura e Liquidação Mercado Pago
    Kafka->>+Billing: Consome WorkOrderApprovedEvent
    Billing->>+MP: Gera checkout / QR Code Pix
    MP-->>-Billing: Retorna checkout URL
    Billing->>Billing: Salva Fatura no PG (Status: PENDING)
    Billing-->>-Kafka: Fatura registrada
    MP->>+Kong: POST /api/v1/payments/webhook<br/>{ action: "payment.updated", status: "approved" }
    Kong->>+Billing: Proxy Request
    Billing->>Billing: Atualiza Fatura (Status: PAID)
    Billing->>Kafka: Publica PaymentConfirmedEvent (Tópico: payment-events)
    Billing-->>-Kong: 200 OK
    Kong-->>-MP: 200 OK

    %% Execução na Oficina
    Note over Kafka,OS: 4. Execução dos Reparos e Finalização
    Kafka->>+Exec: Consome PaymentConfirmedEvent
    Exec->>Exec: Enfileira OS no MongoDB (Status: QUEUED)
    Exec->>Kafka: Publica ExecutionStartedEvent
    Exec->>Exec: Mecânico executa reparos (Status: COMPLETED)
    Exec->>Kafka: Publica ExecutionCompletedEvent (Tópico: execution-events)
    Exec-->>-Kafka: Execução concluída

    %% Conclusão Final
    Kafka->>+OS: Consome ExecutionCompletedEvent
    OS->>OS: Atualiza OS para COMPLETED no PostgreSQL
    OS-->>-Kafka: OS atualizada
    Atendente->>+Kong: GET /api/v1/work-orders/{id}
    Kong->>+OS: Consulta OS
    OS-->>-Kong: 200 OK (Status: COMPLETED)
    Kong-->>-Atendente: 200 OK (Status: COMPLETED)
```

</div>
</div>
