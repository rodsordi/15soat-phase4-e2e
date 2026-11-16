## Diagrama de Sequência

**Gestão de Materiais de Estoque e Checklists Técnicos (Referência: `materials_and_checklist.feature`)**

Este diagrama documenta o lançamento de insumos e preenchimento de checklists operacionais de inspeção com persistência documental no MongoDB:
1. **Lançamento de Insumos e Peças (`POST /api/v1/executions/{id}/materials`)**: Registro estruturado de materiais aplicados na ordem com persistência em array embutido no MongoDB.
2. **Preenchimento de Checklists de Inspeção (`POST /api/v1/executions/{id}/checklist`)**: Validação e atualização de itens de conferência veicular e segurança.
3. **Consulta Consolidada do Agregado (`GET /api/v1/executions/{id}`)**: Recuperação integral do documento de execução com insumos e itens de checklist.

> [!NOTE]
> **🔒 Autenticação & Emissão de JWT**: A obtenção e emissão de tokens Bearer JWT via Keycloak estão detalhadas no diagrama [user_authentication.md](../auth-lambda/user_authentication.md).

<div style="overflow-x: auto; width: 100%;">
<style>
  .mermaid svg {
    max-width: none !important;
    min-width: 900px !important;
    width: 900px !important;
  }
</style>

```mermaid
sequenceDiagram
    autonumber
    actor Mecanico as Mecânico / Técnico<br/>(Funcionário: EMPLOYEE)
    participant Exec as API Exec<br/>(Spring Boot 4 / Mongo)
    participant Mongo as MongoDB<br/>(exec_db)

    Note over Mecanico,Exec: 🔒 Borda Autenticada via Bearer JWT (vide user_authentication.md):<br/>Requisições utilizam token OIDC emitido pelo Keycloak (Role: EMPLOYEE)

    %% Lançamento de Material
    Note over Mecanico,Mongo: 1. Registro de Insumo / Peça de Estoque
    Mecanico->>+Exec: POST /api/v1/executions/{id}/materials<br/>{ code: "PART-BRK-01", description, quantity: 2 }
    Exec->>+Mongo: Registra insumo no array embutido de materials
    Mongo-->>-Exec: Confirma atualização
    Exec-->>-Mecanico: Retorna 200 OK

    %% Preenchimento de Checklist
    Note over Mecanico,Mongo: 2. Registro de Item de Checklist Técnico
    Mecanico->>+Exec: POST /api/v1/executions/{id}/checklist<br/>{ task: "Verificar fluido de freio", completed: true }
    Exec->>+Mongo: Atualiza array de checklist no documento
    Mongo-->>-Exec: Confirma atualização
    Exec-->>-Mecanico: Retorna 200 OK

    %% Consulta Consolidada
    Note over Mecanico,Mongo: 3. Consulta Detalhada Consolidada do Histórico
    Mecanico->>+Exec: GET /api/v1/executions/{id}
    Exec->>+Mongo: Recupera documento completo agregado por ID
    Mongo-->>-Exec: Retorna documento com materials e checklist
    Exec-->>-Mecanico: Retorna 200 OK (ExecutionDetailedResponse)
```

</div>
