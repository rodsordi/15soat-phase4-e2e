# ADR-004: Padrão Visual e Responsividade dos Diagramas de Sequência Mermaid

## Status
**Aprovado**

## Contexto
O projeto E2E adota documentação viva através de diagramas de sequência Mermaid incorporados em arquivos `.md` correspondentes a cada feature (ex: `customer_vehicle_management.md`).
A implementação inicial apresentava os seguintes desafios visuais:
1. **Espaçamento Excessivo entre Componentes**:
   - Mensagens com payloads inline muito longos (ex: `POST /api/v1/customers { document: "52998224725", name, email }`) forçavam o motor de renderização do Mermaid a afastar horizontalmente as colunas dos participantes.
   - O container HTML possuía uma largura mínima fixa (`min-width: 900px`), o que causava vazios visuais desnecessários em diagramas com apenas 4 participantes e forçava barra de rolagem horizontal desnecessária em telas de laptops ou janelas lado a lado (split screen).
2. **Ausência de Indicação de Ciclo de Vida da Thread (Activation Boxes)**:
   - As linhas de vida dos componentes permaneciam tracejadas do início ao fim, sem indicar quando um componente estava ativamente alocando recursos, executando processamento ou aguardando resposta de I/O em banco de dados.

## Decisão
Decidiu-se **padronizar a construção dos diagramas de sequência Mermaid** adotando as seguintes regras de apresentação visual:

1. **Quebra de Linhas em Payloads e Mensagens (`<br/>`)**:
   - Todo payload JSON, parâmetros de chamada ou descrições extensas nas setas devem ser formatados com `<br/>` em linhas curtas e verticais.
   - Exemplo:
     ```mermaid
     Atendente->>+Kong: POST /api/v1/customers<br/>{ document: "52998224725",<br/>  name, email }
     ```
2. **Ativação e Desativação Visual de Threads (Activation Boxes)**:
   - Uso obrigatório dos operadores de sufixo `+` (ativação de thread no participante receptor) e `-` (desativação de thread na resposta/retorno) em todas as chamadas síncronas em cascata:
     - `Atendente->>+Kong: POST ...` (abre thread no gateway)
     - `Kong->>+WO: Proxy Request` (abre thread no microsserviço)
     - `WO->>+DB: Persiste dados` (abre conexão com banco)
     - `DB-->>-WO: Confirmação` (fecha conexão com banco)
     - `WO-->>-Kong: Retorno HTTP` (fecha thread no microsserviço)
     - `Kong-->>-Atendente: Retorno final` (fecha thread no gateway)
3. **Dimensionamento Proporcional da Largura Mínima (`min-width`)**:
   - A largura mínima do container HTML deve ser calibrada proporcionalmente ao número de raias de participantes:
     - **3 a 4 participantes**: `min-width: 700px` (ex: `customer_vehicle_management.md`).
     - **5 participantes**: `min-width: 900px` a `950px` (ex: `execution_queue_and_repair.md`).
     - **6 ou mais participantes / Sagas distribuídas**: `min-width: 1100px` (ex: `complete_choreographed_saga.md`).

## Consequências

### Positivas
- **Diagramas Claros e Compactos**: Eliminação de espaço horizontal vazio e leitura vertical fluida.
- **Riqueza Semântica UML**: As caixas de ativação demonstram graficamente quem está retendo tempo de processamento e conexões ativas.
- **Responsividade Aprimorada**: Redução para `700px` nos diagramas menores evita barras de rolagem desnecessárias no GitHub e em ferramentas de documentação.

### Trade-offs
- A adição de `<br/>` e operadores `+`/`-` exige atenção ao editar novas setas no Mermaid para manter a paridade de abertura e fechamento de blocos de ativação.
