# Plano de Implementação: Unificação do PostgreSQL e Segregação por Schemas com Keycloak

> **Decisão do Usuário**: Opção 1 - Schema-per-Service Unificado com Inicialização Idempotente
> **Status**: Aprovado para Execução

---

## 🎯 Objetivo
Consolidar a infraestrutura de banco de dados relacional local da **Fase 4** em uma **única instância de PostgreSQL (`phase4-postgres`)** ativa na rede `phase4_garage-net` na porta `5432:5432`, eliminando instâncias duplicadas e segregando os domínios estritamente por **Schemas (`work_order`, `billing`, `keycloak`)** no database `garage_db`. 

Adicionalmente, provisionar no repositório `15soat-phase4-e2e` um `docker-compose.yml` para subir o **Keycloak** (porta `8085:8080`, schema `keycloak`) e reutilizar a instância do PostgreSQL que já estiver em execução na rede compartilhada, ou inicializá-la via perfil `standalone` caso não esteja ativa.

---

## 🏗️ Especificações Técnicas

### 1. Script de Inicialização Idempotente de Schemas (`scripts/init-schemas.sql`)
- Script executado no ponto de entrada `/docker-entrypoint-initdb.d/init-schemas.sql`:
  ```sql
  CREATE SCHEMA IF NOT EXISTS work_order;
  CREATE SCHEMA IF NOT EXISTS billing;
  CREATE SCHEMA IF NOT EXISTS keycloak;
  ```

### 2. Configuração do Microsserviço `15soat-phase4-api-work-order`
- **`application.yml`**:
  - `spring.datasource.url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/garage_db?currentSchema=work_order}`
  - `spring.flyway.schemas: work_order`
  - `spring.flyway.default-schema: work_order`
  - `spring.flyway.create-schemas: true`
  - `spring.jpa.properties.hibernate.default_schema: work_order`
- **`docker-compose.yml` / `docker-compose.override.yml`**:
  - Container compartilhado `phase4-postgres` com profile `standalone`.
  - JDBC URL dentro do container: `jdbc:postgresql://postgres:5432/garage_db?currentSchema=work_order`.

### 3. Configuração do Microsserviço `15soat-phase4-api-billing`
- **`application.yml`**:
  - `spring.datasource.url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/garage_db?currentSchema=billing}`
  - `spring.flyway.schemas: billing`
  - `spring.flyway.default-schema: billing`
  - `spring.flyway.create-schemas: true`
  - `spring.jpa.properties.hibernate.default_schema: billing`
- **`docker-compose.yml` / `docker-compose.override.yml`**:
  - Container compartilhado `phase4-postgres` com profile `standalone`.
  - JDBC URL dentro do container: `jdbc:postgresql://postgres:5432/garage_db?currentSchema=billing`.

### 4. Atualização do `docker-compose.yml` Raiz da Fase 4
- Substituição dos serviços `work-order-postgres` e `billing-postgres` por um único serviço:
  - `postgres` (container `phase4-postgres`, porta `5432:5432`, banco `garage_db`, volume `phase4_postgres_data`).
  - Montagem de `./scripts/init-schemas.sql` em `/docker-entrypoint-initdb.d/init-schemas.sql`.
  - Rede `phase4_garage-net` com aliases `postgres` e `phase4-postgres`.

### 5. Provisionamento do Keycloak e Docker Compose em `15soat-phase4-e2e`
- **`15soat-phase4-e2e/docker-compose.yml`**:
  - Serviço `keycloak`:
    - Imagem: `quay.io/keycloak/keycloak:24.0.5`
    - Portas: `8085:8080` (evitando colisão com `api-work-order` na 8080)
    - Comando: `["start-dev", "--import-realm"]`
    - Variáveis de ambiente:
      - `KC_DB: postgres`
      - `KC_DB_URL: jdbc:postgresql://postgres:5432/garage_db?currentSchema=keycloak`
      - `KC_DB_SCHEMA: keycloak`
      - `KC_DB_USERNAME: postgres`
      - `KC_DB_PASSWORD: postgres`
      - `KEYCLOAK_ADMIN: admin`
      - `KEYCLOAK_ADMIN_PASSWORD: admin`
    - Volume: `./keycloak/garage-realm.json:/opt/keycloak/data/import/garage-realm.json:ro`
    - Rede: `phase4_garage-net`
  - Serviço `postgres`:
    - Vinculado ao perfil `standalone` para não conflitar caso o PostgreSQL central já esteja em execução na rede compartilhada.
- **`15soat-phase4-e2e/keycloak/garage-realm.json`**:
  - Realm: `garage`
  - Client: `garage-client` (Direct Access Grants ativado)
  - Roles: `ADMIN`, `EMPLOYEE`, `CUSTOMER`
  - Usuários declarativos provisionados para atender aos testes E2E sem intervenção manual.
- **`15soat-phase4-e2e/src/test/resources/application-local.properties`**:
  - `keycloak.url=http://localhost:8085`
