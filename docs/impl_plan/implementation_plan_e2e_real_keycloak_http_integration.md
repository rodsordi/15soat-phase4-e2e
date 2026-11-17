# Plano de Implementação: Integração HTTP Real com Keycloak no E2E

> **Decisão do Usuário**: Opção 1 - Conexão HTTP Real Direta com a API REST do Keycloak Local
> **Status**: Aprovado para Execução

---

## 🎯 Objetivo
Substituir todas as respostas sintéticas em memória (`ResponseBuilder`) em `AuthLambdaSteps.java` por chamadas HTTP reais via RestAssured contra os endpoints REST do **Keycloak Local (`http://localhost:8085`)**, assegurando que a execução de `user_identity_management.feature` e `user_authentication.feature` (tanto no IntelliJ quanto no Maven) registre e imprima no console 100% de tráfego de rede HTTP real.

---

## 🏗️ Especificações Técnicas

### 1. Autenticação e Emissão de Tokens (`user_authentication.feature`)
- Executar chamada HTTP real via RestAssured contra:
  `POST http://localhost:8085/realms/garage/protocol/openid-connect/token`
  - Content-Type: `application/x-www-form-urlencoded`
  - Form Params: `client_id=garage-client`, `grant_type=password`, `username`, `password`
- Para credenciais válidas, o Keycloak retorna HTTP 200 real contendo o Access Token JWT RS256 com os papéis do realm (`ADMIN`, `EMPLOYEE`, `CUSTOMER`).
- Para credenciais inválidas, o Keycloak retorna HTTP 401 real.

### 2. Gestão e Provisionamento de Identidades (`user_identity_management.feature`)
- **Validação de Documento**:
  - Validar CPF de 11 dígitos; caso inválido, responder com HTTP 400 (`INVALID_DOCUMENT`).
- **Provisionamento no Keycloak**:
  - Obter Admin Token via RestAssured: `POST http://localhost:8085/realms/master/protocol/openid-connect/token`.
  - Limpar previamente usuário existente via `DELETE http://localhost:8085/admin/realms/garage/users/{id}` (idempotência).
  - Criar usuário via chamada HTTP real: `POST http://localhost:8085/admin/realms/garage/users`.
  - Retornar resposta HTTP com status 201 e dados do usuário provisionado.
- **Rollback Compensatório na Saga**:
  - Provisionar no Keycloak e, em seguida, executar o delete compensatório comprovando a reversão, respondendo com status 502 (`REGISTRATION_ROLLBACK`).

---

## 📋 Passos de Execução
1. Atualizar `AuthLambdaSteps.java` com as chamadas HTTP reais via RestAssured contra o Keycloak local.
2. Executar `user_identity_management.feature` e `user_authentication.feature` via Maven / IntelliJ.
3. Validar que as requisições e respostas HTTP são impressas na íntegra no console (sem avisos de resposta sintética).
4. Submeter ao Auditor Sentinela Independente de Qualidade de Código.
5. Solicitar autorização do usuário para `git commit` e `git push`.
