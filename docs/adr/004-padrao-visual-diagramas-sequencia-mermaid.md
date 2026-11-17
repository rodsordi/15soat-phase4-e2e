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

4. **Padrão "Pure Mermaid First" (Erradicação de Divs e Tags de Estilo)**:
   - Os diagramas Mermaid devem ser emitidos **exclusivamente na sintaxe nativa e limpa de Markdown** (fenced code block ```mermaid ... ```).
   - **É terminantemente proibido adicionar `<div>` ao redor dos diagramas Mermaid ou injetar tags `<style>` com regras globais** (como `.mermaid svg { ... }`), prevenindo o anti-pattern "Div Soup", garantindo portabilidade entre IDEs (IntelliJ, VS Code) e renderizadores nativos (GitHub, GitLab, Docusaurus) e evitando bloqueios por políticas de segurança CSP.
   - **Legibilidade e Anti-Compressão Nativas**: A legibilidade visual de diagramas densos é obtida estritamente através da modelagem e sintaxe intrínseca do Mermaid:
     - Quebras de linha com `<br/>` em mensagens e payloads (mantendo as lifelines próximas horizontalmente).
     - Empilhamento de rótulos dos participantes (`Nome<br/>(Papel)`).
     - Numeração automática com `autonumber`.

5. **Abstração Direta (Sem Gateway como Participante Pass-Through)**:
   - O participante intermediário de borda (Kong Gateway / API Gateway) é omitido como coluna repetitiva nos diagramas de domínio e coreografia, sendo declarado na nota inicial de borda segura com referência canônica: `🔒 Borda Autenticada via Bearer JWT (vide user_authentication.md): Requisições utilizam token OIDC`.
   - Isso elimina setas redundantes de proxy (`Ator -> Kong -> Microsserviço`) e reduz a largura total do diagrama, permitindo excelente legibilidade em IDEs (IntelliJ IDEA) e no GitHub.

6. **Semântica UML Assíncrona (`--)`) e Agrupamentos por `box`**:
   - **Mensageria Orientada a Eventos (`--)`)**: Toda publicação ou consumo assíncrono via Apache Kafka (ou SQS/SNS) deve utilizar a seta pontilhada assíncrona do Mermaid (`--)`), reservando setas sólidas síncronas (`->>`) e retornos síncronos (`-->>`) estritamente para chamadas RPC/HTTP/REST e retornos diretos.
   - **Agrupamentos Arquiteturais Nativos (`box`)**: Utilização de contêineres nativos `box <cor> <Título> ... end` para agrupar visualmente participantes por camada arquitetural (ex: *Atores de Negócio*, *Cluster Amazon EKS*, *Mensageria Gerenciada*, *Gateway Externo*), sem recorrer a tags HTML ou divs.

## Consequências

### Positivas
- **Pure Markdown & Zero Div Soup**: Documentação limpa, portátil e renderizada nativamente em qualquer plataforma sem poluição de tags HTML.
- **Rigor e Fidelidade Semântica UML**: Diferenciação clara entre chamadas síncronas bloqueantes (`->>`) e mensageria assíncrona fire-and-forget (`--)`).
- **Segregação Arquitetural Visual**: O uso de `box` nativo delimita instantaneamente fronteiras de infraestrutura e responsabilidade.
- **Diagramas Claros e Compactos**: Eliminação de espaço horizontal vazio e leitura vertical fluida.
- **Riqueza Semântica UML**: As caixas de ativação demonstram graficamente quem está retendo tempo de processamento e conexões ativas.
- **Responsividade Aprimorada**: Sem barras de rolagem artificiais geradas por estilos inline.

### Trade-offs
- A adição de `<br/>`, operadores `+`/`-`, agrupamentos `box` e setas assíncronas `--)` exige consistência ao editar novas features para manter a paridade estética e semântica do projeto.
