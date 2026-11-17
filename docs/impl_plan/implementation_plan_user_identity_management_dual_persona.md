# Plano de Implementação: Gestão de Identidade e Autenticação de Usuários (Clientes e Funcionários)

> **Decisão do Usuário**: Opção 1 - Diagrama Unificado de IAM: Cliente & Funcionário com Segregação RBAC
> **Status**: Aprovado para Execução

---

## 1. Contexto & Objetivos

Transformar o diagrama de sequência contido em `features/auth-lambda/employee_identity_management.md` em um fluxo abrangente de **Gestão de Identidades e Autenticação de Usuários (IAM)** via AWS Lambda Serverless e Keycloak (IdP).
O fluxo abrangerá com rigor visual e semântico as duas personas fundamentais do sistema:
1. **Cliente (`CUSTOMER`)**: Cadastro perimetral com validação de CPF (Módulo 11 da Receita Federal) e login OIDC com emissão de token JWT contendo `roles: ["CUSTOMER"]`.
2. **Funcionário (`EMPLOYEE`)**: Provisionamento administrativo por operador com privilégio `ADMIN` e login OIDC com emissão de token JWT contendo `roles: ["EMPLOYEE"]`.
3. **Defesas e Sad Paths**: Rejeição de documentos inválidos (`INVALID_DOCUMENT`) e bloqueio de autenticação por credenciais incorretas (`INVALID_CREDENTIALS`).

O cliente ocupará mandatoriamente a primeira coluna (extremo esquerdo) do diagrama, e a nota de borda segura manterá o vínculo canônico com `auth_token_authorizer.md`.

---

## 2. Detalhes das Alterações no `employee_identity_management.md`

1. **Título e Introdução**:
   - Atualizado para *"Gestão de Identidade e Autenticação de Usuários (Clientes e Funcionários via Serverless & Keycloak)"*.
   - Explicação concisa da segregação de papéis RBAC (`CUSTOMER`, `EMPLOYEE`, `ADMIN`).
2. **Topologia dos Participantes**:
   - `actor Cliente as Cliente<br/>(CUSTOMER)` (Coluna 1)
   - `actor Func as Funcionário<br/>(Atendente: EMPLOYEE)` (Coluna 2)
   - `actor Admin as Administrador /<br/>RH (ADMIN)` (Coluna 3)
   - `participant Lambda as AWS Lambda<br/>(garage-auth-handler)` (Coluna 4)
   - `participant KC as Keycloak IdP<br/>(Realm: garage)` (Coluna 5)
   - `participant KCDB as PostgreSQL<br/>(keycloak_db)` (Coluna 6)
3. **Seções do Diagrama**:
   - `Note over Cliente,KCDB: 1. Onboarding e Autenticação do Cliente (CUSTOMER)`
   - `Note over Admin,KCDB: 2. Provisionamento e Autenticação do Funcionário (EMPLOYEE)`
   - `Note over Cliente,Lambda: 3. Sad Path: Tentativa de Cadastro com CPF Inválido`
   - `Note over Cliente,KC: 4. Sad Path: Tentativa de Login com Senha Incorreta`
4. **Dimensionamento Responsivo**:
   - Ajustar a largura mínima CSS do SVG para `1400px` conforme a matriz de 6 participantes da ADR-004.
5. **Callout e Borda Segura**:
   - Preservar o callout `[!NOTE]` com link para `auth_token_authorizer.md`.
   - Manter a nota perimetral `🔒 Sessão Autenticada via Token Authorizer (vide auth_token_authorizer.md)`.

---

## 3. Critérios de Aceite

1. O diagrama renderiza perfeitamente no Mermaid sem loops ou ativações órfãs de threads.
2. `Cliente` está posicionado na primeira coluna (mais à esquerda).
3. Tanto `CUSTOMER` quanto `EMPLOYEE` possuem seus fluxos de cadastro e autenticação OIDC explicitados.
4. `mvn process-test-resources test-compile` finaliza com **BUILD SUCCESS**.
5. Auditor Sentinela independente (`code-reviewer-sentinel-subagent`) emite parecer de **APROVADO**.
