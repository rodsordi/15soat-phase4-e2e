# Diagrama de Sequência: Gestão do Catálogo de Serviços

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 900px;">

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Administrador<br/>(ADMIN)
    participant Kong as Kong API Gateway
    participant WorkOrder as Work Order Service<br/>(PostgreSQL)
    participant DB as PostgreSQL<br/>(work_order_db)

    Note over Admin,Kong: 🔒 Sessão Autenticada: Requisições utilizam Bearer JWT (Role: ADMIN)

    %% 1. Cadastro de Serviço Padronizado
    Note over Admin,DB: 1. Cadastro de Serviço Padronizado no Catálogo
    Admin->>+Kong: POST /api/v1/services<br/>{code, name, price}
    Kong->>+WorkOrder: Encaminha requisição autenticada
    WorkOrder->>+DB: Persiste novo serviço no catálogo
    DB-->>-WorkOrder: Confirma persistência
    WorkOrder-->>-Kong: 201 Created (ServiceCatalogResponse)
    Kong-->>-Admin: 201 Created

    %% 2. Consulta de Serviço por Código
    Note over Admin,DB: 2. Consulta do Serviço por Código
    Admin->>+Kong: GET /api/v1/services?code=SRV-OIL-01
    Kong->>+WorkOrder: Encaminha requisição autenticada
    WorkOrder->>+DB: Consulta serviço por código
    DB-->>-WorkOrder: Serviço localizado
    WorkOrder-->>-Kong: 200 OK [ServiceCatalogResponse]
    Kong-->>-Admin: 200 OK
```

</div>
</div>
