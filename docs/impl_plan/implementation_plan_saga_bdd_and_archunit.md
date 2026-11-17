# Plano de Implementação: Alinhamento BDD da Saga Distribuída e Testes Arquiteturais ArchUnit

> **Decisão do Usuário**: Opção 1 - Alinhamento Canônico dos Cenários BDD da Saga (Fim do Bypass Oculto) + Testes Arquiteturais com ArchUnit no Domínio
> **Status**: Aprovado para Execução

---

## 1. Contexto e Motivação
- Nos cenários BDD da Saga distribuída (`complete_choreographed_saga.feature` e `compensating_saga_rollback.feature`), o ciclo de vida saltava diretamente da criação para a aprovação, mascarando regras de negócio e forçando a existência de código defensivo em `E2ESagaSteps.java` com transições forçadas via `try/catch`.
- No microsserviço `15soat-phase4-api-work-order`, a dependência de ArchUnit já constava no `pom.xml` raiz, mas nenhuma verificação arquitetural estava ativa no módulo `domain` para assegurar o isolamento do núcleo da Clean Architecture.

---

## 2. Escopo das Alterações

### A. Repositório `15soat-phase4-e2e`
1. **Refatorar Cenários de BDD**:
   - Atualizar `complete_choreographed_saga.feature` e `compensating_saga_rollback.feature` para utilizar abertura com `DataTable` e declarar as transições de diagnóstico e cálculo de orçamento antes da aprovação do cliente.
2. **Limpeza em `E2ESagaSteps.java`**:
   - Remover as transições ocultas manuais em bloco `try/catch` dentro de `approveBudget()`.
   - Garantir que a aprovação seja executada de forma limpa sobre o estado `WAITING_APPROVAL`.

### B. Repositório `15soat-phase4-api-work-order`
1. **Configuração de Dependência no `domain/pom.xml`**:
   - Incluir a dependência de `archunit-junit5` (com escopo `test`) no módulo `domain`.
2. **Criação da Suíte de Testes Arquiteturais**:
   - Criar `HexagonalArchitectureArchTest.java` com regras estritas:
     - Regra 1: O pacote `domain.model` e `domain.usecase` não deve depender de frameworks web (`org.springframework.web..`, `jakarta.servlet..`).
     - Regra 2: Todas as portas em `domain.port.in` e `domain.port.out` devem ser estritamente interfaces.
     - Regra 3: Os casos de uso em `domain.usecase` devem residir no pacote canônico e respeitar as convenções arquiteturais.

---

## 3. Critérios de Aceite e Verificação
- Suíte `mvn test` em `15soat-phase4-api-work-order` 100% verde incluindo os novos testes do ArchUnit.
- Suíte `mvn test` em `15soat-phase4-e2e` com 40/40 cenários BDD 100% verdes.
- Zero `git commit` ou `git push` automáticos sem permissão prévia do usuário.
