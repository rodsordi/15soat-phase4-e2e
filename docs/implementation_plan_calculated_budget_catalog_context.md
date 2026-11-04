# Plano de Implementação: Precificação Dinâmica no Catálogo e Remoção de Valores Hardcoded

> **Decisão do Usuário**: Opção 1 - DataTables Estruturadas no Contexto & Remoção Total de Valores Hardcoded
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivos

O cálculo financeiro de uma Ordem de Serviço na plataforma distribuída é de responsabilidade interna dos serviços de Catálogo e Faturamento (composição de serviços de mão de obra + materiais/peças aplicados). 

Este plano estabelece:
1. **Configuração Explícita do Catálogo no `Contexto:`**:
   - Adicionar os serviços padronizados (`Troca de Óleo e Filtro`, `Revisão de Freios`) via `DataTable` estruturada.
   - Adicionar os materiais padronizados (`Óleo Sintético 5W30`, `Pastilha de Freio Dianteira`) via `DataTable` estruturada.
2. **Remoção de Valores Hardcoded nos Cenários**:
   - Na Fase 3: remover o valor numérico `450.00` de modo que a geração do orçamento seja declarada sem parâmetros artificiais (`Quando o mecânico conclui o diagnóstico e o faturamento gera o orçamento`).
   - Na Fase 4: remover o valor numérico `450.00` para que o cliente aprove a ordem sem enviar valores monetários arbitrários na requisição (`Quando o próprio cliente aprova o orçamento da Ordem de Serviço`).
3. **Ajuste nos Step Definitions Java (`WorkOrderSteps.java`)**:
   - Implementar steps com `List<Map<String, String>>` para carga em lote de serviços e materiais.
   - Atualizar a emissão de requisições de aprovação e geração de orçamento para não injetar montantes arbitrários quando o valor não for fornecido.
4. **Alinhamento do Diagrama de Sequência (`work_order_lifecycle.md`)**:
   - Ajustar a requisição HTTP da aprovação do cliente na Fase 4 para enviar apenas `{ status: "APPROVED" }` sem hardcode de `totalAmount`.
5. **Conformidade Global**:
   - Código Java, métodos e anotações estritamente em **Inglês** (User Rule 2).
   - Zero operações automáticas de commit/push sem autorização prévia explícita (User Rule 1).

---

## 2. Etapas de Execução

1. Atualizar `work_order_lifecycle.feature` adicionando as DataTables no Contexto e limpando os valores monetários das Fases 3 e 4.
2. Atualizar `WorkOrderSteps.java` com os métodos `ensureServicesExistInCatalog`, `ensureMaterialsExistInCatalog`, `finishBudgetCalculated` e `customerApprovesBudget`.
3. Ajustar o payload do PATCH de aprovação em `work_order_lifecycle.md` para `{ status: "APPROVED" }`.
4. Executar `mvn test-compile` e o runner Cucumber dry-run para validar compilação e matching de todos os passos.
