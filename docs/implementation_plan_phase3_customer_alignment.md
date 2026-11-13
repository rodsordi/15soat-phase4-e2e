# Plano de Implementação: Adequação do Fluxo de Clientes e Veículos à Arquitetura Phase 3 (Serverless + Keycloak + Saga)

> **Decisão do Usuário**: Opção 1 - Reestruturação Completa do Diagrama com o Fluxo Real Phase 3 / Phase 4
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivo
Adequar a documentação viva em `src/test/resources/features/work-order/customer_vehicle_management.md` ao padrão arquitetural estabelecido na Phase 3:
- O onboarding de clientes não ocorre de forma isolada na API relacional; ele é orquestrado via **AWS Lambda Serverless**, integrado ao **Keycloak (IdP)** e propagado para a **API Work Order** através de uma **Saga Orquestrada com Rollback Compensatório**.
- Consultas rápidas de status de usuário ocorrem via Lambda/Keycloak (`GET /users/{cpf}`).
- Operações de negócio autenticadas (como vinculação de novos veículos ou aberturas de OS) trafegam pelo **API Gateway Kong** utilizando token **Bearer JWT** emitido pelo Keycloak.

---

## 2. Mudanças a Serem Aplicadas

### Arquivo: `src/test/resources/features/work-order/customer_vehicle_management.md`
1. **Participantes do Diagrama de Sequência**:
   - `actor Atendente as Atendente /<br/>Cliente`
   - `participant Lambda as AWS Lambda<br/>(Auth & Onboarding)`
   - `participant KC as Keycloak IdP<br/>(IAM / OIDC)`
   - `participant Kong as Kong API Gateway<br/>(Ingress / Proxy)`
   - `participant WO as API Work Order<br/>(Spring Boot 4 / Hexagonal)`
   - `participant DB as PostgreSQL<br/>(work_order_db)`
2. **Dimensionamento**:
   - Ajustar container para `min-width: 950px` para acomodar com legibilidade as 6 raias.
3. **Fluxos Documentados**:
   - **Fluxo 1 (Onboarding Serverless com Saga Orquestrada)**:
     - `POST /register` na Lambda -> Validação Módulo 11 (CPF) -> Criação de usuário no Keycloak -> Propagação para API Work Order (`POST /api/v1/customers`) -> Persistência no Postgres.
   - **Fluxo 2 (Saga Compensatória / Rollback)**:
     - Falha na API Work Order dispara ação compensatória imediata na Lambda (`deleteUser` no Keycloak).
   - **Fluxo 3 (Consulta de Status de Usuário)**:
     - `GET /users/{cpf}` na Lambda consultando o Keycloak.
   - **Fluxo 4 (Gestão Autenticada de Veículos via Gateway)**:
     - Chamadas autenticadas com token `Bearer JWT` via Kong para a API Work Order (`POST /api/v1/vehicles`).

---

## 3. Validação
- Verificar renderização sintática do diagrama Mermaid.
- Validar `git status` em `15soat-phase4-e2e`.
- Consultar o usuário para autorização de commit e push conforme a Diretriz Global 1.
