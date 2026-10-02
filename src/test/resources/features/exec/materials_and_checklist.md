## Diagrama de Sequência

**Gestão de Materiais de Estoque e Checklists Técnicos (Referência: `materials_and_checklist.feature`)**

Este diagrama documenta o lançamento de insumos e preenchimento de checklists operacionais de inspeção com persistência documental no MongoDB.

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 900px;">

```mermaid
sequenceDiagram
    autonumber
    actor Mecanico as Mecânico /<br/>Técnico
    participant Kong as Kong<br/>API Gateway
    participant Exec as API Exec<br/>(Spring Boot 4 / Mongo)
    participant Mongo as MongoDB<br/>(exec_db)

    %% Lançamento de Material
    Note over Mecanico,Mongo: 1. Registro de Insumo / Peça de Estoque
    Mecanico->>Kong: POST /api/v1/executions/{id}/materials<br/>{ code: "PART-BRK-01", description, quantity: 2 }
    Kong->>Exec: Proxy HTTP Request
    Exec->>Mongo: Registra insumo na subcoleção/array de materials
    Mongo-->>Exec: Confirma atualização
    Exec-->>Kong: Retorna 200 OK
    Kong-->>Mecanico: Retorna 200 OK

    %% Preenchimento de Checklist
    Note over Mecanico,Mongo: 2. Registro de Item de Checklist Técnico
    Mecanico->>Kong: POST /api/v1/executions/{id}/checklist<br/>{ task: "Verificar fluido de freio", completed: true }
    Kong->>Exec: Proxy HTTP Request
    Exec->>Mongo: Atualiza array de checklist no documento
    Mongo-->>Exec: Confirma atualização
    Exec-->>Kong: Retorna 200 OK
    Kong-->>Mecanico: Retorna 200 OK
```

</div>
</div>
