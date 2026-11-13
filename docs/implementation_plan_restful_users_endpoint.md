# Plano de Implementação: Adoção do Padrão RESTful Canônico (POST /users)

> **Decisão do Usuário**: Opção 1 - Adotar o Padrão RESTful Canônico (`POST /users`)
> **Status**: Aprovado para Execução

---

## 1. Contexto e Objetivo
Substituir o uso do endpoint RPC `POST /register` pelo recurso RESTful canônico `POST /users` no diagrama de sequência `src/test/resources/features/work-order/customer_vehicle_management.md`.
A Lambda desenvolvida na Phase 3 já aceita nativamente ambas as rotas (`POST /register` e `POST /users`), garantindo plena compatibilidade com a infraestrutura em nuvem existente.

---

## 2. Mudanças a Serem Aplicadas

### Arquivo: `src/test/resources/features/work-order/customer_vehicle_management.md`
- Atualizar a descrição da introdução:
  - De: `A AWS Lambda recebe a requisição pública via Function URL...`
  - Para: `A AWS Lambda recebe a requisição pública via Function URL (POST /users)...`
- Atualizar o Passo 1 do diagrama:
  - De: `Ator->>+Lambda: POST /register<br/>{ document: "52998224725",<br/>  name, email, password }`
  - Para: `Ator->>+Lambda: POST /users<br/>{ document: "52998224725",<br/>  name, email, password }`
- Atualizar o Passo 2 do diagrama (cenário de falha/rollback):
  - De: `Ator->>+Lambda: POST /register<br/>(Dados válidos)`
  - Para: `Ator->>+Lambda: POST /users<br/>(Dados válidos)`

---

## 3. Validação
- Verificar renderização sintática do diagrama Mermaid.
- Validar `git status` em `15soat-phase4-e2e`.
- Consultar o usuário para autorização de commit e push conforme a Diretriz Global 1.
