# Plano de Implementação: Resolução Estrutural de Ponta a Ponta para Suíte E2E Completa

> **Decisão do Usuário**: Opção 1 - Resolução Estrutural de Ponta a Ponta
> **Status**: Aprovado para Execução

---

## 1. Visão Geral do Escopo
Alinhar os microsserviços e o repositório de testes `15soat-phase4-e2e` para garantir a execução 100% verde de todos os 53 cenários E2E contra o ambiente de produção gerenciado em nuvem (EKS/AWS).

---

## 2. Itens a Serem Implementados

### 2.1. Microsserviço `15soat-phase4-api-work-order`
1. **Configuração Kafka (`KafkaConfiguration.java`)**:
   * Configurar `ErrorHandlingDeserializer` como deserializador principal de chaves e valores.
   * Definir `VALUE_DEFAULT_TYPE` como `br.com.fiap.phase4.commons.kafka.event.EventEnvelope`.
   * Habilitar pacotes confiáveis (`*`).
2. **Helm Deployment (`deployment.yaml`)**:
   * Garantir que `startupProbe` esteja devidamente configurado para evitar timeouts durante a inicialização da JVM 25.
3. **Validação Local**:
   * Executar `mvn clean test` no microserviço para assegurar regressão zero.

### 2.2. Repositório de Testes `15soat-phase4-e2e`
1. **Isolamento de Massa e Idempotência**:
   * **`CustomerSteps.java`**: Garantir geração dinâmica ou tratamento idempotente quando o documento já existir no RDS.
   * **`VehicleSteps.java`**: Garantir que veículos cadastrados possuam placas geradas dinamicamente ou reutilizem veículos existentes.
   * **`ServiceCatalogSteps.java` e `MaterialCatalogSteps.java`**: Utilizar códigos dinâmicos ou verificar existência prévia para evitar `AlreadyExistsException`.
2. **Harmonização de Erros RFC 7807 (`work_order_lifecycle.feature`)**:
   * Ajustar as expectativas de código de erro de transição inválida de `422` para `400 Bad Request`, em estrita conformidade com a `ADR-001` e o `CommonsExceptionAdvice`.
3. **Parametrização e Headers de `AuthLambdaSteps.java`**:
   * Garantir que as requisições de autorização incluam o header virtual `Host: api.garage.fiap.com.br` quando roteadas através do Kong Ingress.

---

## 3. Critérios de Aceite e Validação
1. Compilação e testes unitários 100% verdes no `15soat-phase4-api-work-order`.
2. Deploy e rollout estável do `api-work-order` no cluster EKS.
3. Execução da suíte completa de testes E2E (`CucumberTestRunner`) com 53 cenários verdes contra o cluster.
