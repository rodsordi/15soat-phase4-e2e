# ADR-004: Padrão Visual e Responsividade dos Diagramas de Sequência Mermaid

## Status
**Aprovado**

## Contexto
O projeto E2E adota documentação viva através de diagramas de sequência Mermaid incorporados em arquivos `.md` correspondentes a cada feature (ex: `customer_vehicle_management.md`).
A implementação inicial apresentava os seguintes desafios visuais:
1. **Espaçamento Excessivo entre Componentes**:
   - Mensagens com payloads inline muito longos (ex: `POST /api/v1/customers { document: "52998224725", name, email }`) forçavam o motor de renderização do Mermaid a afastar horizontalmente as colunas dos participantes.
   - O container HTML anterior utilizava aninhamento de múltiplas tags (`<div><div>...</div></div>`) com largura fixa indiscriminada (`min-width: 900px`) sem zoom, o que causava o anti-pattern *Div Soup* e vazios visuais quando aplicado a fluxos curtos, além de forçar barra de rolagem desnecessária em telas de laptops ou janelas lado a lado (split screen).
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

4. **Padronização de Viewport com 1 Div Simples em Linha Única, Zoom 1.5 e Rolagem Horizontal**:
   - Todo diagrama Mermaid de médio ou grande porte (diagramas de sequência de ponta a ponta, diagramas ERD relacionais ou arquiteturas de múltiplos serviços) deve ser encapsulado obrigatoriamente por **exatamente uma única tag `<div>`**, declarada em **uma única linha**, garantindo rolagem horizontal (`overflow-x: auto;`), dimensionamento com largura mínima (`min-width: 900px;`) e fator de ampliação visual de 150% (`zoom: 1.5;`):
````markdown
<div style="overflow-x: auto; zoom: 1.5; min-width: 900px;">

```mermaid
...
```

</div>
````
   - **Regras Estritas de Higiene**:
     - **Linha Única**: A tag de abertura `<div style="overflow-x: auto; zoom: 1.5; min-width: 900px;">` deve constar inteira em uma única linha, sem quebras dentro da tag.
     - **Sem Divs Aninhadas ("Div Soup")**: É terminantemente proibido aninhar múltiplos contêineres (`<div><div>...</div></div>`).
     - **Sem Tags `<style>` Globais**: Proibido injetar blocos `<style>` com regras globais (`.mermaid svg { ... }`).
   - **Exceção para Diagramas Pequenos**: Diagramas pequenos (poucos nós, fluxo linear simples de 2 a 3 participantes ou estados curtos) **não devem** utilizar a div, permanecendo em sintaxe pura *Pure Mermaid* para evitar barras de rolagem artificiais em diagramas concisos.
   - **Legibilidade e Anti-Compressão Complementares**: A legibilidade visual interna dos diagramas complementa o zoom através de modelagem limpa do Mermaid:
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
- **Legibilidade Ampliada & Zoom Confortável**: Fator de escala de 150% (`zoom: 1.5;`) garante leitura nítida e confortável de tipografias e payloads em IDEs e monitores de alta resolução.
- **Scroll Horizontal Seguro**: Viewport delimitado com `overflow-x: auto;` e `min-width: 900px;` evita quebras ou cortes de diagramas extensos.
- **Zero Div Soup**: Estrutura contida estritamente em 1 única div em 1 linha, eliminando aninhamentos e tags `<style>`.
- **Rigor e Fidelidade Semântica UML**: Diferenciação clara entre chamadas síncronas bloqueantes (`->>`) e mensageria assíncrona fire-and-forget (`--)`).
- **Segregação Arquitetural Visual**: O uso de `box` nativo delimita instantaneamente fronteiras de infraestrutura e responsabilidade.
- **Riqueza Semântica UML**: As caixas de ativação demonstram graficamente quem está retendo tempo de processamento e conexões ativas.

### Trade-offs
- A adição de `<br/>`, operadores `+`/`-`, agrupamentos `box` e setas assíncronas `--)` exige consistência ao editar novas features para manter a paridade estética e semântica do projeto.
