#language: pt
@e2e @security @cross-cutting @token-validation
Funcionalidade: Validação Transversal de Segurança e Autenticação de APIs
  Como sentinela de segurança do ecossistema de microsserviços da oficina mecânica
  Quero garantir que todas as APIs do ecossistema recusem requisições sem autenticação ou com tokens inválidos
  Para impedir acessos não autorizados e assegurar conformidade com a política Zero Trust (ADR-005)

  # ============================================================================
  # Esquema do Cenário 1: Rejeição de Requisição Sem Cabeçalho de Autenticação (401)
  # ============================================================================
  @unauthenticated @missing-token
  Esquema do Cenário: Bloqueio de requisição sem cabeçalho de autenticação em todas as APIs com HTTP 401
    Dado que a requisição não possui cabeçalho de autenticação
    Quando uma requisição "<metodo>" for enviada para a API "<servico>" no endpoint "<endpoint>"
    Então o código de status HTTP da resposta deve ser 401

    Exemplos:
      | servico    | metodo | endpoint                 |
      | work-order | GET    | /api/v1/customers        |
      | work-order | POST   | /api/v1/work-orders      |
      | work-order | GET    | /api/v1/materials        |
      | work-order | GET    | /api/v1/services         |
      | billing    | GET    | /api/v1/invoices         |
      | billing    | POST   | /api/v1/invoices         |
      | exec       | GET    | /api/v1/executions/queue |
      | exec       | POST   | /api/v1/executions/start |

  # ============================================================================
  # Esquema do Cenário 2: Rejeição de Requisição com Token Inválido ou Malformado (401)
  # ============================================================================
  @invalid-token @malformed-token
  Esquema do Cenário: Bloqueio de requisição com token JWT inválido ou malformado em todas as APIs com HTTP 401
    Dado que a requisição é configurada com um token de autenticação malformado
    Quando uma requisição "<metodo>" for enviada para a API "<servico>" no endpoint "<endpoint>"
    Então o código de status HTTP da resposta deve ser 401

    Exemplos:
      | servico    | metodo | endpoint                 |
      | work-order | GET    | /api/v1/customers        |
      | work-order | POST   | /api/v1/work-orders      |
      | billing    | GET    | /api/v1/invoices         |
      | billing    | POST   | /api/v1/invoices         |
      | exec       | GET    | /api/v1/executions/queue |
      | exec       | POST   | /api/v1/executions/start |

