# Plano de Implementação: Suporte a Virtual Host no Harness E2E

> **Decisão do Usuário**: Opção 1 - Parametrização de Virtual Host no Harness E2E (EnvironmentConfig / ScenarioContext)
> **Status**: Aprovado para Execução

---

## 1. Visão Geral

Habilitar a passagem customizada do cabeçalho HTTP `Host` no harness de testes automatizados (`15soat-phase4-e2e`), permitindo a execução de testes ponta a ponta contra clusters remotos (Amazon EKS) protegidos por Ingress Controllers baseados em nomes (*Virtual Hosting* L7, como o Kong Ingress Controller com host `api.garage.fiap.com.br`), em estrita conformidade com o Protocolo Black-Box Externo.

---

## 2. Arquivos Impactados

1. `src/test/java/br/com/fiap/phase4/e2e/config/EnvironmentConfig.java`:
   - Adição do método estático `getGatewayHost()` lendo `-Dgateway.host` ou a variável de ambiente `API_GATEWAY_HOST`.
2. `src/test/java/br/com/fiap/phase4/e2e/context/ScenarioContext.java`:
   - Injeção automática do cabeçalho `Host` nas instâncias de `RequestSpecification` retornadas por `givenAuth(baseUrl)`.
3. `src/test/java/br/com/fiap/phase4/e2e/steps/WorkOrderSteps.java`:
   - Injeção do cabeçalho `Host` na verificação de health check em `theWorkOrderServiceIsAvailable()`.

---

## 3. Roteiro de Execução

1. Atualizar [`EnvironmentConfig.java`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/java/br/com/fiap/phase4/e2e/config/EnvironmentConfig.java).
2. Atualizar [`ScenarioContext.java`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/java/br/com/fiap/phase4/e2e/context/ScenarioContext.java).
3. Atualizar [`WorkOrderSteps.java`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/src/test/java/br/com/fiap/phase4/e2e/steps/WorkOrderSteps.java).
4. Executar os 13 cenários de teste da funcionalidade de Clientes e Veículos via Maven:
   ```bash
   mvn test -Dtest=CucumberTestRunner -Denv=prd -Dgateway.url=http://a52cdd6ca29234584a3f669f8ab0bdca-b26d259a2e2462b3.elb.us-east-1.amazonaws.com -Dgateway.host=api.garage.fiap.com.br -Dcucumber.filter.tags="@cliente or @veiculo"
   ```
5. Coletar os relatórios e logs de execução, evidenciando o status 100% verde dos casos de teste de clientes e veículos do microsserviço `api-work-order`.
