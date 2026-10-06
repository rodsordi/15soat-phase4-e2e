# Plano de Implementação: Documentação Viva do Ciclo de Vida de Funcionários (auth-lambda)

> **Decisão do Usuário**: Opção 1 - Criar o Mapeamento Canônico de Gestão de Identidades de Funcionários na pasta `auth-lambda/`
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivo
Mapear o fluxo de ciclo de vida de colaboradores (funcionários/atendentes da oficina) que estava ausente na suíte de testes BDD da Phase 4.
- Criação de `employee_identity_management.feature` expressando as regras de negócio em BDD (Gherkin em português, voz ativa, papéis claros e Data Tables).
- Criação de `employee_identity_management.md` com diagrama de sequência Mermaid detalhado contendo caixas de ativação (`+`/`-`), quebras de linha com `<br/>` e dimensionamento proporcional de largura.

---

## 2. Documentos a Serem Criados

### 2.1. `src/test/resources/features/auth-lambda/employee_identity_management.feature`
- **Cenário 1**: Cadastro de funcionário por administrador via Lambda / Keycloak (`POST /users` com role `EMPLOYEE`).
- **Cenário 2**: Tentativa de cadastro de funcionário com CPF inválido (rejeição com RFC 7807).
- **Cenário 3**: Autenticação de funcionário e emissão de token JWT contendo claim `roles: ["EMPLOYEE"]`.
- **Cenário 4**: Bloqueio de acesso para funcionário com credenciais inválidas.

### 2.2. `src/test/resources/features/auth-lambda/employee_identity_management.md`
- Diagrama Mermaid de 4 participantes (`Admin`, `Lambda Auth & User Management`, `Keycloak IdP`, `Keycloak DB`).
- Dimensionamento proporcional (`min-width: 750px`).
- Etapas:
  1. Provisionamento de Funcionário pelo Admin.
  2. Validação Módulo 11 (CPF) e regras de autorização.
  3. Login e Emissão de Token JWT.
  4. Falha na autenticação (credenciais inválidas).

---

## 3. Validação
- Verificar renderização sintática do diagrama Mermaid.
- Confirmar conformidade com as regras globais (PT-BR na doc, sem commits automáticos).
- Validar `git status -s` e solicitar autorização do usuário antes de commit/push.
