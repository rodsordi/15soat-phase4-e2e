# Diagrama do Modelo de Dados & MER (`15soat-phase4-api-work-order`)

Este documento descreve a modelagem de dados relacional do microsserviço de Gestão de Ordens de Serviço (`work_order_db`), implementada em **PostgreSQL** e versionada através de migrações gerenciadas pelo **Flyway** (`db/migration/V1__...` a `V4__...`).

---

## 1. Diagrama Entidade-Relacionamento (MER)

<div style="overflow-x: auto; width: 100%;">
<div style="min-width: 950px;">

```mermaid
erDiagram
    %% =========================================================================
    %% Nível 1: Cadastro Mestre (Cliente e Veículo)
    %% =========================================================================
    CUSTOMER {
        uuid id PK "Identificador único do Cliente"
        string name "Nome completo do cliente"
        string email UK "E-mail único do cliente"
        string document UK "CPF ou CNPJ (Value Object CpfOrCnpj)"
        timestamp created_at "Data e hora de cadastro (UTC)"
        timestamp updated_at "Data e hora de alteração (UTC)"
        bigint version "Controle de concorrência otimista (@Version)"
    }

    VEHICLE {
        uuid id PK "Identificador único do Veículo"
        uuid customer_id FK "FK referenciando CUSTOMER(id)"
        string license_plate UK "Placa do veículo (Value Object LicensePlate)"
        string make "Marca / Fabricante (ex: Toyota)"
        string model "Modelo do veículo (ex: Corolla)"
        integer manufacture_year "Ano de fabricação"
        timestamp created_at "Data e hora de cadastro (UTC)"
        timestamp updated_at "Data e hora de alteração (UTC)"
        bigint version "Controle de concorrência otimista (@Version)"
    }

    CUSTOMER ||--o{ VEHICLE : "possui (1:N)"

    %% =========================================================================
    %% Nível 2: Ordem de Serviço (Agregado Raiz)
    %% =========================================================================
    WORK_ORDER {
        uuid id PK "Identificador único da Ordem de Serviço"
        uuid customer_id FK "FK opcional para CUSTOMER(id)"
        uuid vehicle_id FK "FK opcional para VEHICLE(id)"
        string customer_document "Snapshot do documento CPF/CNPJ"
        string license_plate "Snapshot da placa do veículo"
        string description "Descrição da avaria ou solicitação de serviço"
        string status "Status (RECEIVED, DIAGNOSING, WAITING_APPROVAL, APPROVED, EXECUTING, COMPLETED, DELIVERED, CANCELED, PAYMENT_REJECTED)"
        numeric total_amount "Valor total consolidado da OS (Serviços + Peças)"
        timestamp created_at "Data de abertura (UTC)"
        timestamp updated_at "Última atualização (UTC)"
        bigint version "Controle de concorrência otimista (@Version)"
        string created_by "Identificador do criador"
        string last_modified_by "Identificador do último modificador"
    }

    CUSTOMER ||--o{ WORK_ORDER : "solicita (1:N)"
    VEHICLE ||--o{ WORK_ORDER : "atendido_em (1:N)"

    %% =========================================================================
    %% Nível 3: Trilha de Auditoria e Histórico de Transições
    %% =========================================================================
    WORK_ORDER_STATUS_HISTORY {
        uuid id PK "Identificador único do evento de transição"
        uuid work_order_id FK "FK referenciando WORK_ORDER(id) ON DELETE CASCADE"
        string from_status "Status anterior"
        string to_status "Novo status assumido"
        string reason "Motivo ou observação da transição"
        timestamp changed_at "Timestamp da mudança (UTC)"
        string changed_by "Usuário ou sistema responsável pela transição"
    }

    WORK_ORDER ||--o{ WORK_ORDER_STATUS_HISTORY : "registra_historico (1:N)"

    %% =========================================================================
    %% Nível 4: Itens da OS (Serviços e Peças com Snapshot Imutável ADR 0002)
    %% =========================================================================
    WORK_ORDER_SERVICE {
        uuid id PK "Identificador do item de serviço"
        uuid work_order_id FK "FK referenciando WORK_ORDER(id) ON DELETE CASCADE"
        uuid service_id FK "FK referenciando SERVICE(id)"
        string item_name "Snapshot do nome do serviço no momento da OS"
        string item_code "Snapshot do código do serviço no momento da OS"
        numeric unit_price "Preço unitário congelado no momento da inclusão"
        integer quantity "Quantidade de execuções do serviço"
        timestamp created_at "Data de inclusão na OS (UTC)"
    }

    WORK_ORDER_MATERIAL {
        uuid id PK "Identificador do item de material / peça"
        uuid work_order_id FK "FK referenciando WORK_ORDER(id) ON DELETE CASCADE"
        uuid material_id FK "FK referenciando MATERIAL(id)"
        string item_name "Snapshot do nome da peça no momento da OS"
        string item_sku "Snapshot do SKU da peça no momento da OS"
        numeric unit_price "Preço unitário congelado no momento da inclusão"
        integer quantity "Quantidade de itens consumidos"
        timestamp created_at "Data de inclusão na OS (UTC)"
    }

    WORK_ORDER ||--o{ WORK_ORDER_SERVICE : "contem_servicos (1:N)"
    WORK_ORDER ||--o{ WORK_ORDER_MATERIAL : "contem_materiais (1:N)"

    %% =========================================================================
    %% Nível 5: Catálogos de Serviços e Materiais
    %% =========================================================================
    SERVICE {
        uuid id PK "Identificador único do serviço no catálogo"
        string code UK "Código padronizado do serviço (ex: SRV-OIL-01)"
        string name "Nome descritivo do serviço"
        string description "Descrição técnica do procedimento"
        numeric price "Preço de tabela de mão de obra"
        bigint estimated_minutes "Tempo padrão estimado de execução em minutos"
        timestamp created_at "Data de cadastro no catálogo (UTC)"
        timestamp updated_at "Última atualização cadastral (UTC)"
        bigint version "Controle de concorrência otimista (@Version)"
    }

    MATERIAL {
        uuid id PK "Identificador único do material / peça"
        string sku UK "Código de Estoque / SKU (ex: PART-BRK-01)"
        string name "Nome comercial da peça / insumo"
        string description "Especificação técnica e aplicação"
        numeric unit_cost "Custo de aquisição contábil"
        numeric unit_price "Preço padrão de tabela para faturamento"
        integer stock_quantity "Quantidade física total disponível em estoque"
        integer reserved_quantity "Quantidade reservada em ordens aprovadas"
        integer min_stock "Estoque mínimo para ponto de pedido"
        timestamp created_at "Data de cadastro (UTC)"
        timestamp updated_at "Última alteração (UTC)"
        bigint version "Controle de concorrência otimista (@Version)"
    }

    WORK_ORDER_SERVICE }o--|| SERVICE : "referencia_catalogo"
    WORK_ORDER_MATERIAL }o--|| MATERIAL : "referencia_catalogo"
```

</div>
</div>

---

## 2. Dicionário de Dados e Tabelas

### `customer`
Armazena o cadastro mestre dos clientes da oficina mecânica.
* **`id`** (`UUID`, PK): Identificador imutável.
* **`name`** (`VARCHAR(255)`, NOT NULL): Nome completo do cliente.
* **`email`** (`VARCHAR(255)`, NOT NULL, UNIQUE): Contato de e-mail institucional ou pessoal.
* **`document`** (`VARCHAR(14)`, NOT NULL, UNIQUE): CPF (11 dígitos) ou CNPJ (14 dígitos).
* **`created_at` / `updated_at`** (`TIMESTAMP WITH TIME ZONE`): Rastreamento temporal em UTC.
* **`version`** (`BIGINT`): Lock otimista JPA.

### `vehicle`
Armazena os veículos sob responsabilidade de cada cliente.
* **`id`** (`UUID`, PK): Identificador do veículo.
* **`customer_id`** (`UUID`, FK `customer.id` RESTRICT): Proprietário do veículo.
* **`license_plate`** (`VARCHAR(10)`, NOT NULL, UNIQUE): Placa Mercosul ou padrão antigo.
* **`make`** (`VARCHAR(100)`, NOT NULL): Marca ou fabricante (ex: Toyota, Honda).
* **`model`** (`VARCHAR(100)`, NOT NULL): Modelo do veículo.
* **`manufacture_year`** (`INTEGER`, NOT NULL): Ano de fabricação do veículo.

### `work_order`
Agregado principal do microsserviço, gerencia a solicitação, aprovação de orçamento, status de produção e encerramento comercial.
* **`id`** (`UUID`, PK): Identificador único da OS.
* **`customer_id` / `vehicle_id`** (`UUID`, FKs opcionais ON DELETE SET NULL): Referência estruturada caso existam no cadastro.
* **`customer_document` / `license_plate`** (`VARCHAR`): Snapshots imutáveis informados na abertura.
* **`description`** (`VARCHAR(500)`, NOT NULL): Diagnóstico inicial ou queixa do cliente.
* **`status`** (`VARCHAR(30)`, NOT NULL): Estado atual do ciclo de vida da OS.
* **`total_amount`** (`NUMERIC(12,2)`, NOT NULL): Consolidado monetário calculado.

### `work_order_status_history`
Trilha cronológica completa de transições de status da Ordem de Serviço para fins de auditoria e cálculo de métricas de permanência (Lead Time por etapa).
* **`id`** (`UUID`, PK): Identificador do registro histórico.
* **`work_order_id`** (`UUID`, FK `work_order.id` CASCADE): Ordem de serviço referenciada.
* **`from_status`** (`VARCHAR(30)`): Status anterior (nulo na criação).
* **`to_status`** (`VARCHAR(30)`, NOT NULL): Status de destino.
* **`reason`** (`VARCHAR(500)`): Justificativa do operador, cliente ou evento da Saga.
* **`changed_at`** (`TIMESTAMP WITH TIME ZONE`, NOT NULL): Momento exato da transição.
* **`changed_by`** (`VARCHAR(100)`): Identificador do operador ou sistema.

### `work_order_service` e `work_order_material`
Itens vinculados à OS, preservando snapshots do valor e identificadores nominais contratados (ADR 0002).
* **`item_name` / `item_code` / `item_sku`**: Snapshots congelados do catálogo no momento da inclusão.
* **`unit_price`**: Preço unitário contratado, imune a alterações cadastrais posteriores.
* **`quantity`**: Quantidade solicitada/utilizada.

### `service` e `material`
Catálogos padronizados de serviços (mão de obra) e materiais (peças de reposição e consumíveis).
* Em `material`, a coluna **`reserved_quantity`** gerencia o compromisso de estoque para OSs aprovadas antes da baixa física definitiva na conclusão.
