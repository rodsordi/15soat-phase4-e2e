# Plano de Implementação: Remoção de auth_token_authorizer e Alinhamento Estrito com a Fase 4

> **Decisão do Usuário**: Opção 1 - Remoção Completa do auth_token_authorizer.md e do Teste BDD Órfão
> **Status**: Aprovado para Execução

---

## 🎯 Objetivo
Expurgar definitivamente o resíduo legado da Fase 3 relativo ao `Lambda Token Authorizer` (`auth_token_authorizer.md` e `auth_token_authorizer.feature`), alinhando 100% da suíte de testes e documentação de arquitetura aos requisitos formais do Tech Challenge da Fase 4 da FIAP (3 microsserviços, Saga distribuída, mensageria e autenticação OIDC).

---

## 🏗️ Especificações Técnicas

### 1. Arquivos a Serem Removidos
- `src/test/resources/features/auth-lambda/auth_token_authorizer.md`
- `src/test/resources/features/auth-lambda/auth_token_authorizer.feature`

### 2. Refatoração de Código (`AuthLambdaSteps.java`)
- Remover os métodos associados exclusivamente ao BDD legado de authorizer:
  - `authServiceOperational()`
  - `requestSentWithToken(String authToken)`
  - `authorizerReturnsEffect(String effect)`
- Preservar integralmente os steps necessários para `user_identity_management.feature` (cadastro de funcionários, saga de clientes e rollback no Keycloak).

### 3. Harmonização de Referências a Tokens JWT
- Atualizar todas as menções em callouts `[!NOTE]` e notas de sequência Mermaid nos diagramas de negócio:
  - Remover qualquer menção residual a `auth_token_authorizer.md`.
  - Manter a referência canônica unificada para obtenção de tokens em `auth_token_issuance.md`.
- Atualizar `docs/adr/004-padrao-visual-diagramas-sequencia-mermaid.md` e `README.md`.

---

## 📋 Passos de Execução
1. Deletar `auth_token_authorizer.md` e `auth_token_authorizer.feature`.
2. Refatorar `AuthLambdaSteps.java` removendo os métodos obsoletos.
3. Atualizar callouts e notas perimetrais nos diagramas (`billing`, `exec`, `work-order`, `user_identity_management`).
4. Atualizar `ADR-004` e `README.md`.
5. Validar compilação e testes via `mvn process-test-resources test-compile`.
6. Submeter ao auditor independente (Code Reviewer Sentinel).
7. Solicitar permissão do usuário para commit e push.
