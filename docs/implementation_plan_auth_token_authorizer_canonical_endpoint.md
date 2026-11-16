# Plano de Implementação: Endpoint Canônico de Verificação no Lambda Token Authorizer

> **Decisão do Usuário**: Opção 1 - Endpoint Canônico de Verificação/Consumo no API Gateway
> **Status**: Aprovado para Execução

---

## 🎯 Objetivo
Ajustar o documento [auth_token_authorizer.md](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/auth-lambda/auth_token_authorizer.md) para substituir o endpoint `GET /api/v1/work-orders` (que pertencia indevidamente ao microsserviço de Work Order no cluster EKS) pelo endpoint canônico de consumo e verificação perimetral `GET /auth/verify`, demonstrando com clareza como o cliente consome o API Gateway e como este, por sua vez, consome o Lambda Authorizer internamente.

---

## 🏗️ Especificações Técnicas

### 1. Endpoint Canônico de Consumo do JWT
- **Endpoint**: `GET /auth/verify`
- **Cabeçalho**: `Authorization: Bearer <token>`
- **Comportamento**:
  - O cliente consome o endpoint público `GET /auth/verify` exposto no **AWS API Gateway (HTTP API v2)**.
  - O API Gateway consome a função **Lambda Token Authorizer** através do mecanismo nativo de autorizadores de requisição (`event.authorizationToken`).
  - O Lambda gera a **IAM Policy** com `Effect: "Allow"` (para token válido) ou `Effect: "Deny"` (para token inválido/revogado).
  - Em `Allow`, a requisição é liberada para o target serverless protegido, retornando `200 OK (Acesso Autorizado)`.
  - Em `Deny`, o próprio API Gateway realiza o bloqueio perimetral de borda, retornando `403 Forbidden`.

### 2. Participantes e Lifelines (ADR-004)
- `actor Cliente as Cliente /<br/>Operador`
- `participant APIGW as AWS API Gateway<br/>(HTTP API v2)`
- `participant Lambda as Lambda Token Authorizer<br/>(Node.js 20 / IAM Policy)`
- `participant Backend as Recurso Protegido<br/>(Target Serverless / Endpoint)`

---

## 📋 Passos de Execução
1. Atualizar [auth_token_authorizer.md](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/auth-lambda/auth_token_authorizer.md) com o endpoint `GET /auth/verify` e narrativa coerente de consumo.
2. Executar `mvn process-test-resources test-compile -f 15soat-phase4-e2e/pom.xml`.
3. Submeter a alteração ao crivo do subagente **Code Reviewer Sentinel**.
4. Apresentar os resultados ao usuário e solicitar autorização explícita para commit/push.
