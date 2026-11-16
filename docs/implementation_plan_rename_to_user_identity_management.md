# Plano de Implementação: Renomeação para user_identity_management (.md e .feature)

> **Decisão do Usuário**: Opção 1 - Renomeação Completa em Paridade: .md e .feature
> **Status**: Aprovado para Execução

---

## 1. Contexto & Objetivos

Renomear de forma coordenada e consistente os artefatos de especificação e teste em `15soat-phase4-e2e` do antigo domínio restrito a "employee" para o domínio canônico e unificado de **Gestão de Identidade e Autenticação de Usuários (`user_identity_management`)**, abrangendo Clientes (`CUSTOMER`) e Funcionários (`EMPLOYEE`):

1. **Arquivo de Diagrama Mermaid**:
   - `src/test/resources/features/auth-lambda/employee_identity_management.md` $\longrightarrow$ `src/test/resources/features/auth-lambda/user_identity_management.md`
   - Atualizar a referência canônica interna para `(Referência: user_identity_management.feature)`.
2. **Arquivo de Especificação BDD (Gherkin)**:
   - `src/test/resources/features/auth-lambda/employee_identity_management.feature` $\longrightarrow$ `src/test/resources/features/auth-lambda/user_identity_management.feature`
   - Atualizar o cabeçalho para *"Funcionalidade: Gestão de Identidade e Autenticação de Usuários via Serverless & Keycloak"* e incluir a tag `@usuario`.
3. **Compatibilidade dos Steps Java**:
   - Garantir que [AuthLambdaSteps.java](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/java/br/com/fiap/phase4/e2e/steps/AuthLambdaSteps.java) mantenha cobertura e aderência total aos steps do feature sem necessidade de refatoração destrutiva.
4. **Limpeza Git**:
   - Remover os arquivos antigos para evitar duplicatas residuais no working tree.

---

## 2. Critérios de Aceite

1. `user_identity_management.md` e `user_identity_management.feature` existem e mantêm paridade 1:1.
2. Os arquivos antigos `employee_identity_management.*` são excluídos do sistema de arquivos e do rastreamento Git.
3. `mvn process-test-resources test-compile` finaliza com **BUILD SUCCESS** (31 recursos copiados para `target/test-classes`).
4. Homologação com parecer **APROVADO** pelo subagente `code-reviewer-sentinel-subagent`.
