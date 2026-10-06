# language: pt
@e2e @servico @catalogo @crud
Funcionalidade: Gestão do Catálogo de Serviços
  Como administrador da oficina
  Quero cadastrar e consultar serviços do catálogo da oficina
  Para precificar e padronizar as manutenções solicitadas nas ordens de serviço

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço de Ordens de Serviço
  # ============================================================================
  Contexto:
    Dado que o serviço de Ordem de Serviço está em execução e operacional

  # ============================================================================
  # Cenário 1: Cadastro de Serviço no Catálogo
  # ============================================================================
  @cadastro-servico
  Cenário: Cadastro de serviço no catálogo
    Dado o serviço com os seguintes dados:
      | code  | SRV-OIL-01             |
      | name  | Troca de Óleo e Filtro |
      | price | 150.00                 |
    Quando o serviço é cadastrado no catálogo
    Então o serviço deve ser registrado com sucesso no catálogo

  # ============================================================================
  # Cenário 2: Consulta de Serviço no Catálogo por Código
  # ============================================================================
  @consulta-servico
  Cenário: Consulta de serviço no catálogo por código
    Dado o serviço com código "SRV-OIL-01" cadastrado no catálogo
    Quando o serviço com código "SRV-OIL-01" é consultado
    Então a consulta deve retornar os seguintes dados do serviço:
      | code  | SRV-OIL-01             |
      | name  | Troca de Óleo e Filtro |
      | price | 150.00                 |

  # ============================================================================
  # Cenário 3: Tentativa de Cadastro de Serviço com Código Duplicado (Sad Path)
  # ============================================================================
  @excecao @servico-duplicado
  Cenário: Rejeição ao cadastrar serviço com código já existente
    Dado o serviço com código "SRV-OIL-01" cadastrado no catálogo
    E o serviço com os seguintes dados:
      | code  | SRV-OIL-01             |
      | name  | Troca de Óleo Repetida |
      | price | 160.00                 |
    Quando o serviço é cadastrado no catálogo
    Então o erro retornado deve corresponder a:
      | status    | 409                                           |
      | errorCode | RESOURCE_ALREADY_EXISTS                       |
      | title     | Resource Conflict                             |
      | detail    | Service with code SRV-OIL-01 already exists   |
