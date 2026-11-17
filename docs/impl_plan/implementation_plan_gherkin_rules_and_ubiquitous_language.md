# Plano de Implementação: Estruturação de Regras de Negócio e Linguagem Ubíqua no Gherkin

> **Decisão do Usuário**: Opção 1 - Linguagem Ubíqua, Eliminação de Redundância e Estrutura com Regras de Negócio (Rule Keyword)
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

Esta evolução aprimora a semântica da especificação executável em `work_order_lifecycle.feature` adotando as melhores práticas do Cucumber moderno e Domain-Driven Design (DDD):

1. **Agrupamento por Regras de Negócio (`Regra:`)**:
   - Organizar os cenários sob suas respectivas regras canônicas de domínio:
     - **Regra 1**: *O fluxo operacional da Ordem de Serviço deve evoluir sequencialmente através das 8 fases canônicas*.
     - **Regra 2**: *O cliente possui autoridade soberana para decidir sobre a aprovação ou recusa do orçamento*.
     - **Regra 3**: *Nenhum veículo pode ser liberado sem prévia confirmação de quitação financeira*.
2. **Harmonização da Linguagem Ubíqua no Cenário 2**:
   - Substituir a terminologia técnica `Dado uma Ordem de Serviço existente com o status "WAITING_APPROVAL"` pelo passo em linguagem de domínio:
     `Dado uma Ordem de Serviço com diagnóstico concluído aguardando aprovação do cliente`
3. **Eliminação de Asserções Redundantes**:
   - Simplificar o passo de validação da criação na Fase 1 para `Então a Ordem de Serviço deve ser criada com sucesso`, mantendo a verificação estrita de `customerDocument`, `licensePlate` e `status: RECEIVED` na `DataTable` subsequente.
4. **Step Definitions Java (`WorkOrderSteps.java`)**:
   - Adicionar o binding `@Entao("a Ordem de Serviço deve ser criada com sucesso")`.
   - Assegurar que `@Dado("uma Ordem de Serviço com diagnóstico concluído aguardando aprovação do cliente")` esteja perfeitamente vinculado.
5. **Conformidade Global**:
   - Todo código e comentários em Java estritamente em **Inglês** (User Rule 2).
   - Zero operações automáticas de commit ou push sem autorização prévia expressa (User Rule 1).

---

## 2. Etapas de Execução

1. Atualizar `work_order_lifecycle.feature` aplicando as palavras-chave `Regra:`, a linguagem ubíqua no Cenário 2 e a remoção de redundância na Fase 1.
2. Adicionar os bindings necessários em `WorkOrderSteps.java`.
3. Validar a compilação via `mvn test-compile` e o runner `CucumberTestRunner` em modo dry-run.
