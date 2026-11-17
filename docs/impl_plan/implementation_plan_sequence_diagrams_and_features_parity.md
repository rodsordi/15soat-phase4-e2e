# Plano de Implementação: Harmonização e Paridade dos Diagramas de Sequência com Cenários BDD

> **Decisão do Usuário**: Opção 1 - Harmonização Visual dos Sad Paths e Cenários Faltantes nos Diagramas de Sequência Mermaid (ADR-004 & Living Documentation)
> **Status**: Aprovado para Execução

---

## 1. Visão Geral e Objetivos

Garantir 100% de paridade bidirecional entre os arquivos de especificação de cenários BDD (`.feature`) e os diagramas de sequência Mermaid incorporados na documentação viva (`.md`) do módulo `15soat-phase4-e2e`.

Todos os diagramas enriquecidos devem seguir rigorosamente as diretrizes da **ADR-004**:
- Quebra de linhas vertical `<br/>` em mensagens e payloads.
- Caixas de ativação de thread (`+` / `-`) estritamente em participantes de software (sistemas e serviços).
- Atores humanos e brokers Kafka sem caixas de ativação de thread.
- Borda segura declarada via nota perimetral (`🔒 Borda Autenticada via Bearer JWT`).
- Omissão de gateway pass-through como coluna participante repetitiva.
- Envelopamento em container responsivo com rolagem horizontal explícita (`<div style="overflow-x: auto; width: 100%;">`).

---

## 2. Escopo de Alterações por Módulo

### 2.1. `user_identity_management.md` (IAM & Onboarding)
- **Cenário a Adicionar/Evidenciar**: Cenário 2 do Gherkin (`@cadastro-funcionario-invalido` - Tentativa de cadastro de funcionário com documento inválido é rejeitada com código 400 `INVALID_DOCUMENT`).
- **Implementação**: Adicionar bloco visual demonstrando a validação de formato e algoritmo Módulo 11 (CPF) na Lambda antes de interagir com o Keycloak, retornando `400 Bad Request` com ProblemDetail RFC 7807.

### 2.2. `invoice_generation.md` (Billing & Checkout)
- **Cenários a Adicionar/Evidenciar**:
  - Cenário 2 (`@idempotencia`): Retorno da fatura existente sem duplicar registro perante mesma Ordem de Serviço.
  - Cenários 3 e 4 (`@valor-invalido` e `@cpf-invalido`): Rejeição de fatura com valor menor ou igual a zero (400) e CPF inválido (422).
  - Cenários 5 e 6 (`@consulta` e `@not-found`): Consulta por ID / por Ordem de Serviço (200 OK) e retorno 404 para fatura inexistente.
  - Cenário 8 (`@rbac`): Rejeição com 401 Unauthorized para requisições sem token Bearer válido.
- **Implementação**: Estruturar o diagrama em seções claras e harmonizadas com blocos para emissão/idempotência, validações de domínio/sad paths, consultas HATEOAS e geração de checkout Pix Mercado Pago.

### 2.3. `payment_webhook_processing.md` (Webhooks & Reconciliação)
- **Cenário a Adicionar/Evidenciar**: Cenário 3 do Gherkin (`@idempotencia-webhook` - Processamento idempotente de notificações de webhook repetidas com confirmação 200 `PROCESSED` e preservação de estado `PAID` sem emissão duplicada de eventos).
- **Implementação**: Adicionar Bloco 3 demonstrando o reenvio de webhook com payload idêntico, consulta de concorrência/idempotência no banco e resposta imediata 200 OK sem publicação espúria no Kafka.

### 2.4. `material_management.md` (Catálogo de Materiais)
- **Cenário a Adicionar/Evidenciar**: Cenário 4 do Gherkin (`@material-duplicado` - Rejeição ao cadastrar material com SKU já existente com código 400 `RESOURCE_ALREADY_EXISTS`).
- **Implementação**: Adicionar bloco visual demonstrando a tentativa de inserção com SKU repetido, validação de unicidade no banco e retorno 400 com RFC 7807.

### 2.5. `service_catalog_management.md` (Catálogo de Serviços)
- **Cenário a Adicionar/Evidenciar**: Cenário 3 do Gherkin (`@servico-duplicado` - Rejeição ao cadastrar serviço com código já existente com código 400 `RESOURCE_ALREADY_EXISTS`).
- **Implementação**: Adicionar bloco visual demonstrando a tentativa de inserção com código repetido, validação de unicidade no banco e retorno 400 com RFC 7807.

---

## 3. Validação e Quality Gate

1. **Validação Determinística de Compilação e Testes**:
   - Executar `mvn test -Dtest=EnvironmentConfigTest`
   - Executar `mvn test -Dtest=CucumberTestRunner -Denv=local`
   - Garantir que todos os 57 cenários BDD permaneçam 100% verdes.
2. **Quality Gate Autônomo (Sentinel Subagent)**:
   - Acionar auditoria sentinela independente para validar conformidade com ADR-004, integridade sintática Mermaid e ausência de regressões.
3. **Consulta de Permissão Git (Diretriz Global #1)**:
   - Apresentar o resultado final ao usuário e solicitar autorização antes de qualquer commit/push.
