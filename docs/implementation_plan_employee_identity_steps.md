# Plano de Implementação: Implementação dos Steps de employee_identity_management.feature

> **Decisão do Usuário**: Opção 1 - Implementação em AuthLambdaSteps.java com Suporte Black-Box & RFC 7807
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivo
Implementar os Step Definitions em Java correspondentes aos cenários declarados em `src/test/resources/features/auth-lambda/employee_identity_management.feature`:
1. Provisionamento de Funcionário pelo Administrador (`POST /users`).
2. Validação algorítmica de CPF inválido e retorno de ProblemDetail (RFC 7807).
3. Autenticação/Login de Funcionário com emissão de token contendo claim `EMPLOYEE` (`POST /auth/login`).
4. Rejeição de login com credenciais incorretas (status 401 e ProblemDetail).

---

## 2. Mudanças a Serem Aplicadas

### 2.1. Arquivo: `src/test/java/br/com/fiap/phase4/e2e/config/EnvironmentConfig.java`
- Adicionar método `getAuthLambdaBaseUrl()` para apontar para a Lambda Function URL ou Gateway local.

### 2.2. Arquivo: `src/test/java/br/com/fiap/phase4/e2e/steps/AuthLambdaSteps.java`
- Adicionar step `@Dado("que a função serverless de autenticação e o Keycloak estão em execução e operacionais")`.
- Adicionar step `@Dado("um administrador autenticado no sistema")`.
- Adicionar step `@Quando("o administrador cadastra um novo funcionário com os seguintes dados:")`.
- Adicionar step `@Quando("o administrador tenta cadastrar um funcionário com os seguintes dados:")`.
- Adicionar step `@Então("o funcionário deve ser provisionado com sucesso no Keycloak")`.
- Adicionar step `@E("a resposta deve confirmar a criação com status {int} e papel {string}")`.
- Adicionar step `@E("a resposta deve conter os seguintes detalhes do problema RFC 7807:")`.
- Adicionar step `@Dado("um funcionário cadastrado com documento {string} e senha {string}")`.
- Adicionar step `@Dado("um funcionário cadastrado com documento {string}")`.
- Adicionar step `@Quando("o funcionário realiza a autenticação com as credenciais:")`.
- Adicionar step `@Quando("o funcionário tenta realizar autenticação com senha inválida:")`.
- Adicionar step `@Então("a autenticação deve ser realizada com sucesso com código HTTP {int}")`.
- Adicionar step `@E("o token JWT emitido deve conter a claim de papéis com {string}")`.

---

## 3. Validação
- Compilar o código com `mvn test-compile`.
- Validar `git status -s`.
- Consultar o usuário para autorização de commit e push conforme a Diretriz Global 1.
