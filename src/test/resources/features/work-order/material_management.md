# Diagrama de Sequência: Gestão e Precificação de Materiais

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 900px;">

```mermaid
sequenceDiagram
    autonumber
    actor Operator as Operador de Estoque<br/>(Funcionário: EMPLOYEE)
    participant Kong as Kong API Gateway
    participant WorkOrder as Work Order Service<br/>(PostgreSQL)
    participant DB as PostgreSQL<br/>(work_order_db)

    Note over Operator,Kong: 🔒 Sessão Autenticada: Requisições utilizam Bearer JWT (Role: EMPLOYEE)

    %% 1. Cadastro de Material
    Note over Operator,DB: 1. Cadastro de Material com Precificação e Estoque
    Operator->>+Kong: POST /api/v1/materials<br/>{sku, name, unitCost, unitPrice, initialStock, minStock}
    Kong->>+WorkOrder: Encaminha requisição autenticada
    WorkOrder->>+DB: Persiste novo material no catálogo
    DB-->>-WorkOrder: Confirma persistência
    WorkOrder-->>-Kong: 201 Created (MaterialResponse)
    Kong-->>-Operator: 201 Created

    %% 2. Consulta de Material
    Note over Operator,DB: 2. Consulta do Material por SKU
    Operator->>+Kong: GET /api/v1/materials?sku=PART-BRK-01
    Kong->>+WorkOrder: Encaminha requisição autenticada
    WorkOrder->>+DB: Consulta material por SKU
    DB-->>-WorkOrder: Material localizado
    WorkOrder-->>-Kong: 200 OK [MaterialResponse]
    Kong-->>-Operator: 200 OK

    %% 3. Atualização de Preço
    Note over Operator,DB: 3. Atualização de Preço de Venda
    Operator->>+Kong: PATCH /api/v1/materials/{id}<br/>{unitPrice: 105.00}
    Kong->>+WorkOrder: Encaminha requisição autenticada
    WorkOrder->>+DB: Atualiza preço unitário do material
    DB-->>-WorkOrder: Confirma atualização
    WorkOrder-->>-Kong: 200 OK (MaterialResponse atualizado)
    Kong-->>-Operator: 200 OK
```

</div>
</div>
