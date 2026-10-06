## Diagrama de Sequência

**Gestão de Clientes e Veículos Vinculados (Referência: `customer_vehicle_management.feature`)**

Este diagrama documenta o fluxo de cadastro e consulta de clientes e seus respectivos veículos através do API Gateway Kong até o microsserviço de Ordens de Serviço com persistência relacional no PostgreSQL.

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 900px;">

```mermaid
sequenceDiagram
    autonumber
    actor Atendente as Atendente /<br/>Operador
    participant Kong as Kong<br/>API Gateway
    participant WO as API Work Order<br/>(Spring Boot 4 / PG)
    participant DB as PostgreSQL<br/>(work_order_db)

    %% Cadastro do Cliente
    Note over Atendente,DB: 1. Cadastro e Validação de Cliente
    Atendente->>Kong: POST /api/v1/customers<br/>{ document: "52998224725", name, email }
    Kong->>WO: Proxy HTTP Request
    WO->>WO: Valida algoritmo Módulo 11 (CPF)
    WO->>DB: Persiste registro na tabela customers
    DB-->>WO: Confirma persistência
    WO-->>Kong: Retorna 201 Created (CustomerResponse)
    Kong-->>Atendente: Retorna 201 Created

    %% Consulta de Cliente
    Note over Atendente,DB: 2. Consulta de Cliente por Documento
    Atendente->>Kong: GET /api/v1/customers/52998224725
    Kong->>WO: Proxy HTTP Request
    WO->>DB: Busca customer por documento
    DB-->>WO: Retorna dados cadastrais
    WO-->>Kong: Retorna 200 OK
    Kong-->>Atendente: Retorna 200 OK com dados do cliente

    %% Cadastro de Veículo
    Note over Atendente,DB: 3. Cadastro e Vínculo de Veículo
    Atendente->>Kong: POST /api/v1/vehicles<br/>{ licensePlate: "BRA2E19", customerDocument, make, model }
    Kong->>WO: Proxy HTTP Request
    WO->>DB: Valida existência do cliente e persiste veículo
    DB-->>WO: Confirma persistência
    WO-->>Kong: Retorna 201 Created (VehicleResponse)
    Kong-->>Atendente: Retorna 201 Created

    %% Múltiplos Veículos (1:N)
    Note over Atendente,DB: 4. Vínculo de Segundo Veículo ao Mesmo Cliente (1:N)
    Atendente->>Kong: POST /api/v1/vehicles<br/>{ licensePlate: "XYZ9F88", customerDocument: "52998224725", make: "Honda", model: "Civic" }
    Kong->>WO: Proxy HTTP Request
    WO->>DB: Persiste segundo veículo associado ao mesmo customer_id
    DB-->>WO: Confirma persistência relacional (1:N)
    WO-->>Kong: Retorna 201 Created (VehicleResponse)
    Kong-->>Atendente: Retorna 201 Created

    %% Sad Paths / Regras de Integridade
    Note over Atendente,DB: 5. Tratamento de Exceções de Domínio (Sad Paths)
    Atendente->>Kong: POST /api/v1/vehicles (Cliente Inexistente ou Placa Duplicada)
    Kong->>WO: Proxy HTTP Request
    WO->>DB: Consulta integridade cadastral
    WO-->>Kong: Retorna 400 Bad Request (ProblemDetail + errorCode)
    Kong-->>Atendente: Retorna 400 Bad Request
```

</div>
</div>
