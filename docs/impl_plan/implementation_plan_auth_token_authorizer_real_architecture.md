# Plano de Implementação: Alinhamento Arquitetural do Lambda Token Authorizer com a Infraestrutura Real

> **Decisão do Usuário**: Opção 1 - Alinhamento Fidedigno com a Infraestrutura Real do Terraform
> **Status**: Aprovado para Execução

---

## 🎯 Objetivo
Corrigir o documento e diagrama de sequência [auth_token_authorizer.md](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/auth-lambda/auth_token_authorizer.md) para representar com precisão a infraestrutura provisionada no Terraform (`15soat-phase4-iac-lamda` e `15soat-phase4-iac-k8s`), eliminando o acoplamento incorreto entre o AWS API Gateway e o Kong Ingress Controller.

---

## 🏗️ Especificações Técnicas

### 1. Separação de Perímetros
- **Borda Serverless (AWS API Gateway + Lambda Authorizer)**:
  - AWS API Gateway (HTTP API v2) valida requisições públicas invocando nativamente o `Lambda Token Authorizer` (`fiap-authorizer-prd`).
  - Emite IAM Policy com `Effect: "Allow"` ou `Effect: "Deny"`.
  - Em caso de `Deny`, bloqueia a requisição na borda com `403 Forbidden`.
  - Em caso de `Allow`, encaminha a requisição ao destino serverless protegido, retornando `200 OK`.
- **Borda Microsserviços no Kubernetes (Kong Ingress Controller via AWS NLB)**:
  - O Kong Ingress Controller é exposto diretamente na internet via seu próprio **AWS Network Load Balancer (NLB) internet-facing** no EKS.
  - Não há roteamento em cascata ou `VPC Link` conectando o API Gateway ao Kong.

### 2. Lifelines e Participantes no Mermaid (ADR-004)
- `actor Cliente as Cliente /<br/>Operador`
- `participant APIGW as AWS API Gateway<br/>(HTTP API v2)`
- `participant Lambda as Lambda Token Authorizer<br/>(Node.js 20 / IAM Policy)`
- `participant Backend as Backend Protegido<br/>(Target Serverless / Endpoint)`

---

## 📋 Passos de Execução
1. Atualizar [auth_token_authorizer.md](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/auth-lambda/auth_token_authorizer.md) com a narrativa arquitetural precisa e o diagrama de sequência sem o Kong Ingress Controller.
2. Executar `mvn process-test-resources test-compile -f 15soat-phase4-e2e/pom.xml` para validar integridade e sincronização com `target/test-classes`.
3. Executar o duplo crivo de qualidade com o subagente sentinela independente.
4. Reportar o resultado final e solicitar permissão explícita para commit/push.
