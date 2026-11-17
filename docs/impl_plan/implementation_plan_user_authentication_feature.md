# Plano de Implementação: Criação de user_authentication.feature e Paridade BDD 1:1

> **Decisão do Usuário**: Opção 1 - Paridade Estrita 1:1 com user_authentication.md e Desacoplamento de user_identity_management.feature
> **Status**: Aprovado para Execução

---

## 🎯 Objetivo
Criar a especificação executável BDD `user_authentication.feature` com paridade estrita 1:1 aos 4 cenários modelados no diagrama de sequência [user_authentication.md](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/resources/features/auth-lambda/user_authentication.md), expurgando os cenários residuais de login de `user_identity_management.feature` e enriquecendo os steps em `AuthLambdaSteps.java`.

---

## 🏗️ Especificações Técnicas

### 1. Novo Arquivo BDD `user_authentication.feature`
- **Localização**: `15soat-phase4-e2e/src/test/resources/features/auth-lambda/user_authentication.feature`
- **Tags**: `@e2e @auth @lambda @autenticacao @oidc`
- **Cenários Modelados**:
  1. `@login-admin`: Administrador realiza login e recebe token JWT com papel `ADMIN`.
  2. `@login-funcionario`: Funcionário/Atendente realiza login e recebe token JWT com papel `EMPLOYEE`.
  3. `@login-cliente`: Cliente realiza login com seu CPF e recebe token JWT com papel `CUSTOMER`.
  4. `@login-falha`: Tentativa de login com senha incorreta é negada (`401 Unauthorized` com `ProblemDetail: INVALID_CREDENTIALS`).

### 2. Purificação de `user_identity_management.feature`
- Remoção dos cenários de login (antigos cenários 3 e 4), restringindo o arquivo exclusivamente à gestão/provisionamento de identidades (cadastro de funcionário com sucesso e rejeição por CPF inválido).

### 3. Enriquecimento de `AuthLambdaSteps.java`
- Adição de anotações Cucumber para suportar as 3 personas (`ADMIN`, `EMPLOYEE`, `CUSTOMER`) e o cenário de erro de autenticação genérico.

### 4. Atualização de `README.md`
- Registro de `user_authentication.feature` na seção 5 do README.

---

## 📋 Passos de Execução
1. Criar `user_authentication.feature`.
2. Purificar `user_identity_management.feature`.
3. Atualizar `AuthLambdaSteps.java`.
4. Atualizar `README.md`.
5. Executar `mvn clean process-test-resources test-compile`.
6. Submeter ao auditor independente (Code Reviewer Sentinel).
7. Solicitar permissão do usuário para commit e push.
