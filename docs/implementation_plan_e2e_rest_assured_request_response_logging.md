# Plano de Implementação: Logging Completo de Requisições e Respostas no RestAssured (E2E)

> **Decisão do Usuário**: Opção 1 - Filtros Nativos de Logging com Pretty-Print Centralizado em `ScenarioContext`
> **Status**: Aprovado para Execução

---

## 🎯 Objetivo
Habilitar a impressão detalhada de todos os dados de requisição (Método HTTP, URI, Headers, Cookies, Query Params, Payload/Body formatado) e de resposta (Status Line, Headers, Cookies e Payload/Body formatado) em todas as chamadas HTTP executadas via RestAssured na suíte de testes de ponta a ponta (`15soat-phase4-e2e`).

---

## 🏗️ Especificações Técnicas

### 1. Registro de Filtros Nativos do RestAssured
- Inclusão dos filtros `RequestLoggingFilter(LogDetail.ALL)` e `ResponseLoggingFilter(LogDetail.ALL)` de forma centralizada em `ScenarioContext`:
  - `RequestLoggingFilter`: Intercepta e imprime no `System.out` o método, URI completa, todos os cabeçalhos (incluindo `Authorization` e `Host`), parâmetros e o corpo da mensagem.
  - `ResponseLoggingFilter`: Intercepta e imprime no `System.out` o código e mensagem de status HTTP, tempo de resposta, cabeçalhos e corpo JSON formatado.

### 2. Tratamento para Respostas Sintéticas / Mock
- No método `ScenarioContext.setResponse(Response response)`, interceptar instâncias de resposta geradas em memória (ex: `ResponseBuilder` do perfil local de autenticação) e imprimir no console seu status code, cabeçalhos e body JSON com pretty-print, garantindo visibilidade 100% homogênea em todos os cenários.

---

## 📋 Passos de Execução
1. Atualizar `ScenarioContext.java` com a configuração estática e centralizada dos filtros de logging do RestAssured (`LogDetail.ALL`).
2. Implementar a interceptação e formatação no método `setResponse(...)`.
3. Executar `mvn test -Dtest=EnvironmentConfigTest` e `mvn test -Dtest=CucumberTestRunner -Denv=local`.
4. Verificar no log de saída do Maven se as requisições e respostas HTTP são impressas detalhadamente no console.
5. Submeter ao Auditor Sentinela Independente de Qualidade de Código.
6. Consultar permissão do usuário para `git commit` e `git push`.
