# Plano de Implementação: Threads Visuais e Otimização de Largura no Diagrama Mermaid

> **Decisão do Usuário**: Opção 1 - Ativação em Cascata via Operadores `+` / `-` e Redução do min-width para 700px
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivo
Refatorar o diagrama de sequência do arquivo `src/test/resources/features/work-order/customer_vehicle_management.md`:
1. Adicionar ativações visuais de thread (`+` na requisição e `-` no retorno) em todos os fluxos de chamada em cascata (Atendente -> Kong -> WO -> DB).
2. Otimizar a largura mínima do container HTML (`min-width: 700px`), ajustando o espaçamento horizontal entre as 4 raias de componentes para uma leitura mais compacta e proporcional.

---

## 2. Mudanças a Serem Aplicadas

### Arquivo: `src/test/resources/features/work-order/customer_vehicle_management.md`
- Alterar `min-width: 900px` para `min-width: 700px`.
- Atualizar as mensagens entre participantes com operadores de ativação de thread:
  - **Fluxo 1 (Cadastro de Cliente)**:
    - `Atendente->>+Kong`
    - `Kong->>+WO`
    - `WO->>+DB`
    - `DB-->>-WO`
    - `WO-->>-Kong`
    - `Kong-->>-Atendente`
  - **Fluxo 2 (Consulta de Cliente)**:
    - `Atendente->>+Kong`
    - `Kong->>+WO`
    - `WO->>+DB`
    - `DB-->>-WO`
    - `WO-->>-Kong`
    - `Kong-->>-Atendente`
  - **Fluxo 3 (Cadastro de Veículo)**:
    - `Atendente->>+Kong`
    - `Kong->>+WO`
    - `WO->>+DB`
    - `DB-->>-WO`
    - `WO-->>-Kong`
    - `Kong-->>-Atendente`
  - **Fluxo 4 (Múltiplos Veículos)**:
    - `Atendente->>+Kong`
    - `Kong->>+WO`
    - `WO->>+DB`
    - `DB-->>-WO`
    - `WO-->>-Kong`
    - `Kong-->>-Atendente`
  - **Fluxo 5 (Sad Paths)**:
    - `Atendente->>+Kong`
    - `Kong->>+WO`
    - `WO->>+DB`
    - `DB-->>-WO`
    - `WO-->>-Kong`
    - `Kong-->>-Atendente`

---

## 3. Validação
- Verificar renderização sintática do diagrama Mermaid.
- Validar `git status` em `15soat-phase4-e2e`.
- Consultar o usuário para autorização de commit e push conforme a Diretriz Global 1.
