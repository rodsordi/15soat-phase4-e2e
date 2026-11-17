# Plano de Implementação: Mapeamento da Lambda Auth no Diagrama de Contêineres (Nível 2 C4)

> **Decisão do Usuário**: Opção 1 - Mapeamento da Lambda Auth no Contêiner de Borda com Fluxo de Autenticação e Validação JWKS
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivo

No repositório `15soat-phase4-e2e`, a seção `### 2. Nível 2: Diagrama de Contêineres (Containers)` do `README.md` apresentava uma lacuna arquitetural: a função **Lambda de Autenticação (`fiap-auth` / `lambda-auth`)**, provisionada pelo repositório `15soat-phase4-iac-lamda`, não estava mapeada visualmente no diagrama Mermaid `C4Container`.

O objetivo desta alteração é incluir a **Lambda Auth Service** no boundary da Camada de Borda & Serverless (`iac-lambda`), evidenciando:
1. Os endpoints públicos de autenticação e emissão de tokens JWT RS256 (`POST /auth/login`, `POST /users`, `GET /.well-known/jwks.json`).
2. O fluxo dos atores de negócio (`customer` e `mechanic`) autenticando-se via `AWS HTTP API Gateway v2`.
3. O consumo dos serviços corporativos via NLB/Kong apresentando o Bearer Token JWT emitido.
4. A validação de tokens JWT através da consulta à chave pública exposta pelo endpoint JWKS do API Gateway.

---

## 2. Alinhamento com as ADRs

- **ADR-005** (`15soat-phase4-e2e/docs/adr/005-gestao-identidade-funcionarios-auditoria-jwt.md`): Centralização de identidade e autenticação com emissão de tokens OIDC/JWT assinados (RS256) e auditoria transacional por claims.
- **ADR-004** (`15soat-phase4-e2e/docs/adr/004-padrao-visual-diagramas-sequencia-mermaid.md`): Padronização visual em containers responsivos (`<div style="overflow-x: auto; zoom: 1.5; min-width: 900px;">`).

---

## 3. Modificações a Executar

- **Arquivo Alvo**: [`15soat-phase4-e2e/README.md`](file:///c:/git/fiap/phase4/15soat-phase4-e2e/README.md)
- **Alterações no bloco Mermaid `C4Container`**:
  - Atualizar o contêiner `apigw` para explicitar autenticação.
  - Inserir o nó `Container(lambda_auth, "Lambda Auth Service", "Node.js 20 / Lambda", "Autenticação de usuários, emissão de<br/>tokens JWT RS256 e endpoint JWKS.")`.
  - Adicionar as relações de autenticação dos atores `customer` e `mechanic` com `apigw`.
  - Adicionar a relação de invocação do `apigw` para `lambda_auth`.
  - Atualizar as relações com o `nlb` destacando o envio do `Bearer JWT`.
  - Adicionar a relação de validação de assinatura JWT pelo `kong` via JWKS pública no `apigw`.

---

## 4. Validação

- Conferir a integridade da sintaxe Mermaid do diagrama `C4Container`.
- Garantir que não haja quebras de renderização ou inconsistência de tags.
