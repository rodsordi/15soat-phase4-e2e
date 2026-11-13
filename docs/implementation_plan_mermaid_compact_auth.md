# Plano de Implementação: Harmonização Arquitetural Global dos Diagramas Mermaid (Clean Gateway & Semantic DB)

> **Decisão do Usuário**: Opção 1 - Abstração Arquitetural Consistente & Clean Gateway
> **Status**: Aprovado para Execução

---

## 1. Diretrizes de Harmonização Estabelecidas

1. **Sessão Autenticada Canônica no Topo**:
   - `Note over Ator,Kong: 🔒 Sessão Autenticada: Requisições utilizam Bearer JWT (Role: [ROLE])`
   - O fluxo detalhado de validação de token fica isolado em `auth_token_authorizer.md`.
2. **Gateway Homogêneo como Proxy Seguro (Clean Gateway)**:
   - Remover validações internas redundantes (`Kong->>Kong: Valida assinatura do JWT` ou `Kong->>Auth: Validar JWT Bearer Token`) de diagramas de negócio.
   - O Kong atua consistentemente encaminhando a requisição autenticada para o microsserviço correspondente.
3. **Persistência Semântica de Domínio (Semantic DB - Sem SQL Literal)**:
   - Eliminar instruções SQL brutas (`INSERT INTO ...`, `SELECT ... WHERE ...`).
   - Usar intenções de negócio claras:
     - `Persiste cadastro do cliente no PostgreSQL`
     - `Busca cliente e persiste vínculo do veículo`
     - `Verifica duplicidade e integridade cadastral`
4. **Setas Limpas e Caixas de Ativação (`+`/`-`)**:
   - Manter as setas livres de headers HTTP crus e tags de role repetitivas.
   - Manter o ciclo de vida de ativação proporcional e balanceado.

---

## 2. Arquivos Impactados

1. **`src/test/resources/features/work-order/customer_vehicle_management.md`**:
   - Remover `Kong->>Kong: Valida assinatura do JWT`.
   - Substituir `INSERT INTO customers` e `SELECT customer_id FROM customers...` por persistência semântica.
2. **`src/test/resources/features/work-order/material_management.md`**:
   - Remover chamada síncrona `Kong->>Auth: Validar JWT Bearer Token`.
   - Homogeneizar persistência semântica com o PostgreSQL.
3. **`src/test/resources/features/work-order/service_catalog_management.md`**:
   - Remover chamada síncrona `Kong->>Auth: Validar JWT Bearer Token`.
   - Homogeneizar persistência semântica com o PostgreSQL.
4. **Demais diagramas (`work-order`, `exec`, `billing`, `saga`)**:
   - Revisar para assegurar que nenhum contenha SQL literal ou chamadas de auth redundantes.
