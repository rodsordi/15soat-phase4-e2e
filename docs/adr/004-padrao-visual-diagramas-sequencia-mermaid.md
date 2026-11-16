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
Decidiu-se **padronizar a construção e a renderização dos diagramas de sequência Mermaid** adotando as seguintes regras de apresentação visual:

1. **Quebra de Linhas em Payloads e Mensagens (`<br/>`)**:
   - Todo payload JSON, parâmetros de chamada ou descrições extensas nas setas devem ser formatados com `<br/>` em linhas curtas e verticais.
   - Exemplo:
     ```mermaid
     Atendente->>+Kong: POST /api/v1/customers<br/>{ document: "52998224725",<br/>  name, email }
     ```

2. **Ativação e Desativação Visual de Threads (Activation Boxes Estritas a Sistemas)**:
   - Uso obrigatório dos operadores de sufixo `+` (ativação de thread no participante receptor) e `-` (desativação de thread na resposta/retorno) em todas as chamadas síncronas em cascata:
     - `Atendente->>+Kong: POST ...` (abre thread no gateway)
     - `Kong->>+WO: Proxy Request` (abre thread no microsserviço)
     - `WO->>+DB: Persiste dados` (abre conexão com banco)
     - `DB-->>-WO: Confirmação` (fecha conexão com banco)
     - `WO-->>-Kong: Retorno HTTP` (fecha thread no microsserviço)
     - `Kong-->>-Atendente: Retorno final` (fecha thread no gateway)
   - **Atores Humanos e Broker Kafka sem Threads**: Atores humanos (`Cliente`, `Atendente`, `Mecânico`) e o barramento assíncrono (`Kafka`) não representam processos de execução contínua de software, portanto **nunca** devem receber barras de ativação (`+`/`-`).

3. **Fronteira Delimitada de Sessão Autenticada e Borda Segura**:
   - A nota de sessão perimetral deve delimitar com precisão a borda HTTP entre os atores chamadores e os microsserviços privados, referenciando explicitamente a obtenção de tokens via Keycloak e a autorização perimetral:
     - `Note over <Atores>,<Serviço>: 🔒 Borda Autenticada via Bearer JWT (vide user_authentication.md):<br/>Requisições utilizam token OIDC emitido pelo Keycloak (Role: ...)`
   - Deixar explícito que filas internas (Kafka) e bancos de dados não pertencem à sessão Bearer JWT.
   - Complementar no texto Markdown (fora do SVG) com um callout `[!NOTE]` contendo o link hipertexto navegável para `user_authentication.md` (emissão de credenciais).

4. **Anulação de Compressão de SVG e Garantia de Rolagem Horizontal (IntelliJ IDEA & Browsers)**:
   - Para impedir que IDEs (como IntelliJ IDEA / JCEF) ou visualizadores Markdown forcem `max-width: 100%` no SVG e reduzam as fontes a tamanhos ilegíveis, todo diagrama deve ser envolvido pelo seguinte padrão CSS:
     ```html
     <div style="overflow-x: auto; width: 100%;">
     <style>
       .mermaid svg {
         max-width: none !important;
         min-width: [LARGURA]px !important;
         width: [LARGURA]px !important;
       }
     </style>

     ```mermaid
     sequenceDiagram
     ...
     ```
     </div>
     ```
   - **Abstração Direta (Sem Gateway como Participante Pass-Through)**:
     - O participante intermediário de borda (Kong Gateway / API Gateway) é omitido como coluna repetitiva nos diagramas de domínio e coreografia, sendo declarado na nota inicial de borda segura com referência canônica: `🔒 Borda Autenticada via Bearer JWT (vide user_authentication.md): Requisições utilizam token OIDC`.
     - Isso elimina setas redundantes de proxy (`Ator -> Kong -> Microsserviço`) e reduz a largura total do diagrama, permitindo excelente legibilidade em IDEs (IntelliJ IDEA) e no GitHub.

   - **Matriz de Dimensionamento Otimizada por Quantidade de Participantes**:
     - **3 participantes**: `850px` a `900px` (ex: `service_catalog_management.md`, `material_management.md`, `materials_and_checklist.md`, `payment_webhook_processing.md`).
     - **4 participantes**: `1050px` (ex: `invoice_generation.md`, `execution_queue_and_repair.md`).
     - **5 participantes**: `1200px` a `1250px` (ex: `customer_vehicle_management.md`, `user_authentication.md`, `choreographed_saga.md` - Cenários 1 e 2).
     - **6 a 7 participantes / Ciclos Completos**: `1200px` a `1450px` (ex: `work_order_lifecycle.md`, `choreographed_saga.md` - Cenário 3).

## Consequências

### Positivas
- **Diagramas Claros e Compactos**: Eliminação de espaço horizontal vazio e leitura vertical fluida.
- **Riqueza Semântica UML**: As caixas de ativação demonstram graficamente quem está retendo tempo de processamento e conexões ativas.
- **Responsividade Aprimorada**: Redução para `700px` nos diagramas menores evita barras de rolagem desnecessárias no GitHub e em ferramentas de documentação.

### Trade-offs
- A adição de `<br/>` e operadores `+`/`-` exige atenção ao editar novas setas no Mermaid para manter a paridade de abertura e fechamento de blocos de ativação.
