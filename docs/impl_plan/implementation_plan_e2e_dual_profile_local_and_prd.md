# Plano de Implementação: Suporte aos Perfis Local e PRD no Harness E2E

> **Decisão do Usuário**: Opção 1 - Paridade Estrita com Fase 3 & Multi-Environment Properties (application-local.properties e application-prd.properties)
> **Status**: Aprovado para Execução

---

## 1. Visão Geral

Estruturar o projeto de testes de ponta a ponta (`15soat-phase4-e2e`) para operar sob dois perfis de execução formalmente segregados:
1. **`local`**: Conecta diretamente às instâncias dos microsserviços expostas no Docker Compose (`localhost:8080`, `localhost:8081`, `localhost:8083`), utilizando tokens mock válidos reconhecidos pelo `mod-security` e graceful handling para autenticação serverless quando o Keycloak local não estiver ativo.
2. **`prd`**: Conecta via modo Black-Box externo ao API Gateway / Ingress Controller remoto (`gateway.url`), com suporte a cabeçalho de Virtual Host (`gateway.host=api.garage.fiap.com.br`) e autorização integrada.

A arquitetura replica o padrão consolidado da Fase 3 (`E2eConfig` e arquivos `application-{env}.properties`).

---

## 2. Arquivos Impactados

1. `src/test/resources/application.properties`:
   - Configurações base compartilhadas (`e2e.env=local`, timeouts HTTP, logging).
2. `src/test/resources/application-local.properties`:
   - Configurações direcionadas ao Docker Compose local:
     - `workorder.url=http://localhost:8080`
     - `billing.url=http://localhost:8081`
     - `exec.url=http://localhost:8083`
     - `auth.lambda.url=http://localhost:8080`
     - `auth.token=Bearer mock_employee_jwt_token`
3. `src/test/resources/application-prd.properties`:
   - Configurações direcionadas ao cluster remoto / API Gateway:
     - `gateway.url=${API_GATEWAY_URL:http://localhost:8000}`
     - `gateway.host=${API_GATEWAY_HOST:api.garage.fiap.com.br}`
     - `auth.token=${E2E_AUTH_TOKEN:}`
4. `src/test/java/br/com/fiap/phase4/e2e/config/EnvironmentConfig.java`:
   - Carregamento automático de `application.properties` e `application-{env}.properties` via ClassLoader.
   - Resolução recursiva de placeholders `${VAR:default}` a partir de propriedades do sistema e variáveis de ambiente.
   - Fornecimento dos métodos seletores de URLs e tokens por serviço.
5. `src/test/java/br/com/fiap/phase4/e2e/steps/AuthLambdaSteps.java`:
   - No perfil `local`, assegurar mock transparente ou graceful bypass quando Keycloak/Lambda não estiverem provisionados no Docker Compose, permitindo execução 100% verde da suíte completa.
6. `src/test/java/br/com/fiap/phase4/e2e/context/ScenarioContext.java`:
   - Vinculação dos tokens de autenticação configurados no perfil ativo.

---

## 3. Roteiro de Execução

1. Criar os arquivos de propriedades em `15soat-phase4-e2e/src/test/resources/`.
2. Refatorar `EnvironmentConfig.java` para implementar o mecanismo canônico de carregamento e interpolação de propriedades.
3. Ajustar `ScenarioContext.java` e `AuthLambdaSteps.java` para respeitar o perfil ativo e garantir compatibilidade local e remota.
4. Executar os testes no perfil `local` via `mvn test -Dtest=CucumberTestRunner -Denv=local`.
5. Validar que a suíte atinge 100% de aprovação (todos os cenários verdes).
6. Submeter ao Quality Gate autônomo com o subagente auditor sentinela.
7. Solicitar permissão explícita ao usuário para commit e push.
