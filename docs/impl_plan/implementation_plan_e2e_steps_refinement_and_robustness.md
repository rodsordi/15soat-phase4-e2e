# Plano de Implementação: Refinamento, Resiliência e Robustez dos Steps E2E

> **Decisão do Usuário**: Opção 1 - Pacote Completo de Excelência: Polling Reativo, Timeouts e Serialização Tipada
> **Status**: Aprovado para Execução

---

## 🎯 Objetivo
Elevar a robustez, performance e qualidade de código da suíte de testes de ponta a ponta (`15soat-phase4-e2e`):
1. Eliminar o anti-pattern *Sleepy Test* (`Thread.sleep`) em cenários assíncronos de Saga (`E2ESagaSteps.java`), substituindo por polling reativo resiliente.
2. Configurar timeouts HTTP de conexão (5s) e leitura (10s) no client RestAssured via `ScenarioContext.java`.
3. Adicionar serialização de payloads tipada e segura com `Map.of(...)` e Jackson, eliminando templates manuais de texto propensos a falhas de formatação.
4. Criar a classe de ciclo de vida `ScenarioHooks.java` (`@Before` e `@After`) para demarcação de início, rastreamento de métricas e status final dos cenários.

---

## 🏗️ Especificações Técnicas

### 1. Polling Reativo Resiliente em `E2ESagaSteps.java`
- Substituição dos 5 pontos de `Thread.sleep` (1000ms e 500ms) por um utilitário de polling não bloqueante:
  - Intervalo de sondagem de 100ms e timeout de 5s.
  - Liberação imediata assim que a condição de negócio ou transição de status no Kafka for satisfeita.

### 2. Timeouts HTTP Nativos em `ScenarioContext.java`
- Configuração global no RestAssured:
  ```java
  RestAssured.config = RestAssuredConfig.config()
          .httpClient(HttpClientConfig.httpClientConfig()
                  .setParam("http.connection.timeout", 5000)
                  .setParam("http.socket.timeout", 10000));
  ```

### 3. Serialização Tipada em Mapas Imutáveis
- Substituição de strings manuais por estruturas chave-valor (`Map.of(...)`) passadas diretamente ao RestAssured para serialização JSON confiável.

### 4. Ciclo de Vida do Cucumber (`ScenarioHooks.java`)
- Criação de `br.com.fiap.phase4.e2e.hooks.ScenarioHooks`:
  - `@Before`: Log informativo com nome e tags do cenário, e registro do timestamp inicial.
  - `@After`: Log do tempo total de execução e status do cenário (PASSED/FAILED).

---

## 📋 Passos de Execução
1. Atualizar `ScenarioContext.java` com a configuração de timeouts do HttpClient.
2. Refatorar `E2ESagaSteps.java` substituindo `Thread.sleep` por polling reativo e payloads tipados.
3. Criar `ScenarioHooks.java` no pacote `br.com.fiap.phase4.e2e.hooks`.
4. Executar os testes unitários (`EnvironmentConfigTest`) e a suíte completa BDD (`CucumberTestRunner`).
5. Validar a redução do tempo total de execução e a estabilidade da suíte.
6. Submeter as alterações ao Auditor Sentinela Independente de Qualidade de Código.
7. Solicitar permissão do usuário para `git commit` e `git push`.
