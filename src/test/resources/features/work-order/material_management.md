## Diagrama de Sequência

**Gestão e Precificação de Materiais (Referência: `material_management.feature`)**

Este diagrama documenta o gerenciamento do estoque e da precificação de insumos operacionais no microsserviço de Ordens de Serviço:
1. **Cadastro de Material no Catálogo (`POST /api/v1/materials`)**: Provisionamento de peças com SKU, custo unitário, preço de venda e margens de estoque mínimo (Cenário 1).
2. **Consulta e Localização por SKU (`GET /api/v1/materials?sku=...`)**: Recuperação de especificações técnicas e disponibilidade de insumos (Cenário 2).
3. **Atualização Dinâmica de Preço de Venda (`PATCH /api/v1/materials/{id}`)**: Ajuste de precificação comercial com controle transacional e versionamento (Cenário 3).
4. **Tratamento de Sad Path (SKU Duplicado)**: Rejeição com código `400 Bad Request` perante tentativa de cadastro de SKU já existente na base (Cenário 4).

> [!NOTE]
> **🔒 Autenticação & Emissão de JWT**: A obtenção e emissão de tokens Bearer JWT via Keycloak estão detalhadas no diagrama [user_authentication.md](../auth-lambda/user_authentication.md).

---

<div style="overflow-x: auto; zoom: 1.5; min-width: 900px;">

```mermaid
sequenceDiagram
    autonumber
    actor Operator as Operador de Estoque<br/>(Funcionário: EMPLOYEE)
    participant WO as API Work Order<br/>(Spring Boot 4 / PG)
    participant DB as PostgreSQL<br/>(work_order_db)

    Note over Operator,WO: 🔒 Borda Autenticada via Bearer JWT (vide user_authentication.md):<br/>Requisições utilizam token OIDC emitido pelo Keycloak (Role: EMPLOYEE)

    %% 1. Cadastro de Material
    Note over Operator,DB: 1. Cadastro de Material com Precificação e Estoque
    Operator->>+WO: POST /api/v1/materials<br/>{ sku, name, unitCost, unitPrice,<br/>  initialStock, minStock }
    WO->>+DB: Persiste novo material no catálogo
    DB-->>-WO: Confirma persistência
    WO-->>-Operator: 201 Created (MaterialResponse)

    %% 2. Consulta de Material
    Note over Operator,DB: 2. Consulta do Material por SKU
    Operator->>+WO: GET /api/v1/materials?sku=PART-BRK-01
    WO->>+DB: Consulta material por SKU
    DB-->>-WO: Material localizado
    WO-->>-Operator: 200 OK [MaterialResponse]

    %% 3. Atualização de Preço
    Note over Operator,DB: 3. Atualização de Preço de Venda
    Operator->>+WO: PATCH /api/v1/materials/{id}<br/>{ unitPrice: 105.00 }
    WO->>+DB: Atualiza preço unitário do material
    DB-->>-WO: Confirma atualização
    WO-->>-Operator: 200 OK (MaterialResponse atualizado)

    %% 4. Sad Path: SKU Duplicado
    Note over Operator,DB: 4. Tentativa de Cadastro com SKU Duplicado (Cenário 4)
    Operator->>+WO: POST /api/v1/materials<br/>{ sku: "PART-BRK-01" (Já existente), name, ... }
    WO->>+DB: Consulta existência do SKU
    DB-->>-WO: SKU já cadastrado
    WO-->>-Operator: 400 Bad Request (ProblemDetail: RESOURCE_ALREADY_EXISTS)
```

</div>
