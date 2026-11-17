# Plano de Implementação: Pacote Completo de Governança e Paridade BDD

> **Decisão do Usuário**: Opção 1 - Pacote Completo de Governança e Paridade BDD
> **Status**: Aprovado para Execução

---

## 🎯 Objetivo
Consolidar a governança e conformidade técnica da suíte `15soat-phase4-e2e` para a avaliação da Fase 4 da FIAP através de 3 entregáveis:
1. Purificação do diagrama C4 Container em `README.md`, removendo o contêiner residual do authorizer.
2. Inclusão da Matriz Geral de Rastreabilidade e Cobertura no `README.md` (BDD, Diagramas, Testes k6, Microsserviços e Bancos SQL/NoSQL).
3. Complementação de `user_identity_management.feature` com os cenários executáveis da Saga Orquestrada de Cliente e Rollback Compensatório para atingir 100% de paridade estrita com `user_identity_management.md`.

---

## 🏗️ Especificações Técnicas

### 1. Purificação do Diagrama C4 Container (`README.md`)
- Remover `Container(authorizer, "Lambda Token Authorizer", ...)` e `Rel(apigw, authorizer, ...)`.
- Manter o foco da borda serverless exclusivamente no proxy de webhook do Mercado Pago e no Ingress NLB do cluster EKS.

### 2. Matriz de Rastreabilidade (`README.md`)
- Tabela de governança mapeando os 11 fluxos funcionais:
  - Bounded Context / Domínio
  - Especificação BDD (`.feature`)
  - Diagrama de Sequência (`.md`)
  - Teste de Carga e Estresse (`.js`)
  - Microsserviços Envolvidos
  - Camada de Persistência (SQL PostgreSQL vs NoSQL MongoDB)

### 3. Paridade em `user_identity_management.feature`
- Adicionar:
  - `@cadastro-cliente-saga`: Atendente cadastra novo cliente via Saga Orquestrada com sucesso (Keycloak + API Work Order).
  - `@cadastro-cliente-rollback`: Falha na API Work Order aciona rollback compensatório distribuído (remoção no Keycloak e status 502 Bad Gateway com ProblemDetail).
- Implementar os step definitions em `AuthLambdaSteps.java` com asserções limpas e defensivas.

---

## 📋 Passos de Execução
1. Atualizar o diagrama C4 e adicionar a Matriz de Rastreabilidade em `README.md`.
2. Adicionar os cenários de Saga e Rollback a `user_identity_management.feature`.
3. Implementar os steps em `AuthLambdaSteps.java`.
4. Validar via `mvn clean process-test-resources test-compile`.
5. Submeter ao auditor independente (Code Reviewer Sentinel).
6. Solicitar permissão do usuário para commit e push.
