# Plano de Implementação: Validação Transversal de Segurança e Autenticação de APIs (Token Inválido)

> **Decisão do Usuário**: Opção 1 - Centralização em Feature BDD Transversal de Segurança com Scenario Outline
> **Status**: Aprovado para Execução

---

## 1. Contexto e Motivação

No ecossistema de microsserviços da Fase 4 (`api-work-order`, `api-billing`, `api-exec`), as requisições que acessam endpoints protegidos exigem tokens OIDC/JWT válidos emitidos pelo Identity Provider (Keycloak) ou validados pelo API Gateway / Lambda Authorizer.

Para garantir que **todas as APIs rejeitem de forma consistente (HTTP 401 Unauthorized)** requisições sem credenciais válidas (token ausente ou token malformado/inválido), sem violar os princípios da **ADR-002** (não poluir especificações de domínio com testes repetitivos de infraestrutura) e da **ADR-003** (uso eficiente de tabelas e esquemas de cenários no Cucumber), centralizamos esta governança em uma especificação de segurança transversal dedicada.

---

## 2. Escopo da Implementação

1. **Criação do Pacote e Especificação BDD**:
   - `src/test/resources/features/security/cross_api_authentication_guardrails.feature`
   - Esquema do Cenário (`Scenario Outline`) cobrindo as 3 APIs principais (`work-order`, `billing`, `exec`) em cenários de token ausente e token malformado/inválido.

2. **Criação do Diagrama de Sequência Normativo (ADR-004)**:
   - `src/test/resources/features/security/cross_api_authentication_guardrails.md`
   - Diagrama Mermaid responsivo com CSS de proteção contra compressão, sem ativação em atores humanos, detalhando o bloqueio imediato na camada Spring Security / Resource Server Filter.

3. **Implementação de Steps Cucumber Reutilizáveis**:
   - `src/test/java/br/com/fiap/phase4/e2e/steps/CrossApiSecuritySteps.java`
   - Mapeamento dinâmico de serviço (`work-order`, `billing`, `exec`), método HTTP (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`), endpoint e injeção controlada de cabeçalho `Authorization` (ausente vs. inválido/malformado).

4. **Validação do Quality Gate (Diretriz Global #7)**:
   - Execução determinística dos testes com `CucumberTestRunner`.
   - Auditoria autônoma com o subagente `Code Reviewer Sentinel`.
