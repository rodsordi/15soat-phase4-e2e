# language: pt
@e2e @cliente @veiculo @ordem-de-servico
Funcionalidade: Gestão de Clientes e Veículos Vinculados
  Como atendente da oficina mecânica
  Quero cadastrar e consultar clientes e seus respectivos veículos
  Para manter a base cadastral atualizada e viabilizar a abertura de ordens de serviço

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço e Atendente Autenticado
  # ============================================================================
  Contexto:
    Dado que o serviço de Ordem de Serviço está em execução e operacional
    E que o atendente da oficina está devidamente autenticado com perfil "EMPLOYEE"

  # ============================================================================
  # Cenário 1: Cadastro de Cliente
  # ============================================================================
  @cadastro-cliente
  Cenário: Cadastro de novo cliente por documento
    Dado o cliente com os seguintes dados cadastrais:
      | document | 52998224725          |
      | name     | Rodrigo Sordi        |
      | email    | rodrigo@fiap.com.br  |
    Quando o cliente é cadastrado
    Então o cliente deve ser registrado com sucesso
    E a resposta deve conter identificador único gerado e documento "52998224725"

  # ============================================================================
  # Cenário 2: Consulta dos Dados Cadastrais do Cliente
  # ============================================================================
  @consulta-cliente
  Cenário: Consulta dos dados cadastrais de cliente por documento
    Dado o cliente com documento "52998224725" cadastrado
    Quando o cliente com documento "52998224725" é consultado
    Então a consulta deve retornar os seguintes dados do cliente:
      | document | 52998224725         |
      | name     | Rodrigo Sordi       |
      | email    | rodrigo@fiap.com.br |

  # ============================================================================
  # Cenário 3: Cadastro e Vínculo de Veículo ao Cliente
  # ============================================================================
  @cadastro-veiculo
  Cenário: Vinculação de veículo ao cliente
    Dado o cliente com documento "52998224725" cadastrado
    Quando o veículo com os seguintes dados é vinculado ao cliente "52998224725":
      | licensePlate | BRA2E19 |
      | make         | Toyota  |
      | model        | Corolla |
      | year         | 2023    |
    Então o veículo deve ser registrado com sucesso
    E a resposta do veículo deve confirmar a placa "BRA2E19" e o documento "52998224725"

  # ============================================================================
  # Cenário 4: Consulta de Veículo por Placa
  # ============================================================================
  @consulta-veiculo
  Cenário: Consulta de veículo por placa
    Dado um cliente cadastrado com documento "52998224725" e veículo "BRA2E19"
    Quando o veículo com placa "BRA2E19" é consultado
    Então a consulta deve retornar os seguintes dados do veículo:
      | licensePlate     | BRA2E19     |
      | customerDocument | 52998224725 |
      | make             | Toyota      |
      | model            | Corolla     |

  # ============================================================================
  # Cenário 5: Vinculação de Múltiplos Veículos ao Mesmo Cliente (Relação 1:N)
  # ============================================================================
  @multiplos-veiculos @cardinalidade-1-n
  Cenário: Vinculação de um segundo veículo ao mesmo cliente
    Dado um cliente cadastrado com documento "52998224725" e veículo "BRA2E19"
    Quando o veículo com os seguintes dados é vinculado ao cliente "52998224725":
      | licensePlate | XYZ9F88 |
      | make         | Honda   |
      | model        | Civic   |
      | year         | 2024    |
    Então o veículo deve ser registrado com sucesso
    E a consulta de veículo pela placa "XYZ9F88" deve confirmar o vínculo com o cliente "52998224725"
    E a consulta de veículo pela placa "BRA2E19" deve confirmar o vínculo com o cliente "52998224725"

  # ============================================================================
  # Cenário 6: Tentativa de Cadastro de Cliente com Documento Duplicado (Sad Path)
  # ============================================================================
  @excecao @cliente-duplicado
  Cenário: Rejeição ao cadastrar cliente com documento já existente
    Dado o cliente com documento "52998224725" cadastrado
    E o cliente com os seguintes dados cadastrais:
      | document | 52998224725         |
      | name     | Outro Nome          |
      | email    | outro@fiap.com.br   |
    Quando o cliente é cadastrado
    Então o erro retornado deve corresponder a:
      | status    | 400                                                |
      | errorCode | CUSTOMER_ALREADY_EXISTS                            |
      | title     | Business Rule Violation                            |
      | detail    | Customer already exists with document: 52998224725 |

  # ============================================================================
  # Cenário 7: Tentativa de Vínculo de Veículo a Cliente Inexistente (Sad Path)
  # ============================================================================
  @excecao @cliente-inexistente
  Cenário: Rejeição ao vincular veículo a cliente não cadastrado
    Quando o veículo com os seguintes dados é vinculado ao cliente "99999999999":
      | licensePlate | NFD1A23 |
      | make         | Ford    |
      | model        | Ka      |
      | year         | 2020    |
    Então o erro retornado deve corresponder a:
      | status    | 400                                           |
      | errorCode | CUSTOMER_NOT_FOUND                             |
      | title     | Business Rule Violation                        |
      | detail    | Customer not found with document: 99999999999 |

  # ============================================================================
  # Cenário 8: Tentativa de Cadastro de Veículo com Placa Duplicada (Sad Path)
  # ============================================================================
  @excecao @veiculo-duplicado
  Cenário: Rejeição ao cadastrar veículo com placa já existente
    Dado um cliente cadastrado com documento "52998224725" e veículo "BRA2E19"
    Quando o veículo com os seguintes dados é vinculado ao cliente "52998224725":
      | licensePlate | BRA2E19 |
      | make         | Toyota  |
      | model        | Corolla |
      | year         | 2023    |
    Então o erro retornado deve corresponder a:
      | status    | 400                                                  |
      | errorCode | VEHICLE_ALREADY_EXISTS                               |
      | title     | Business Rule Violation                              |
      | detail    | Vehicle already exists with license plate: BRA2E19   |

  # ============================================================================
  # Cenário 9: Tentativa de Operação sem Autenticação (Segurança / RBAC)
  # ============================================================================
  @seguranca @nao-autenticado
  Cenário: Tentativa de cadastro de cliente por operador não autenticado é negada
    Dado que o operador não possui token de autenticação válido
    Quando o cliente com documento "52998224725", nome "Anônimo" e email "anonimo@fiap.com.br" tenta ser cadastrado
    Então o código de status HTTP da resposta deve ser 401

  # ============================================================================
  # Cenário 10: Atualização Parcial de Cliente via PATCH RESTful
  # ============================================================================
  @atualizacao-cliente @patch
  Cenário: Atualização cadastral de cliente via PATCH
    Dado o cliente com documento "52998224725" cadastrado
    Quando o cliente com documento "52998224725" tem seu nome atualizado para "Rodrigo Sordi Atualizado"
    Então a consulta deve retornar os seguintes dados do cliente:
      | document | 52998224725                |
      | name     | Rodrigo Sordi Atualizado   |
      | email    | rodrigo@fiap.com.br        |

  # ============================================================================
  # Cenário 11: Exclusão Segura de Veículo por Identificador Canônico (DELETE /{id})
  # ============================================================================
  @exclusao-veiculo @delete
  Cenário: Exclusão de veículo sem ordens de serviço ativas por ID
    Dado um cliente cadastrado com documento "52998224725" e veículo "XYZ9F88"
    Quando o veículo com placa "XYZ9F88" é excluído por seu identificador único
    Então a resposta de exclusão deve retornar o código HTTP 204

  # ============================================================================
  # Cenário 12: Exclusão Segura de Cliente sem Vínculos por Identificador Canônico (DELETE /{id})
  # ============================================================================
  @exclusao-cliente @delete
  Cenário: Exclusão de cliente sem veículos e sem ordens de serviço por ID
    Dado o cliente com documento "12345678909" cadastrado sem veículos
    Quando o cliente com documento "12345678909" é excluído por seu identificador único
    Então a resposta de exclusão deve retornar o código HTTP 204

  # ============================================================================
  # Cenário 13: Bloqueio de Exclusão de Cliente com Veículos Associados (422 Unprocessable Entity)
  # ============================================================================
  @excecao @exclusao-cliente-com-veiculos
  Cenário: Rejeição ao excluir cliente que ainda possui veículos vinculados
    Dado um cliente cadastrado com documento "52998224725" e veículo "BRA2E19"
    Quando o cliente com documento "52998224725" tenta ser excluído por seu identificador único
    Então o erro retornado deve corresponder a:
      | status    | 422                                                                             |
      | errorCode | CUSTOMER_HAS_VEHICLES                                                           |
      | title     | Business Rule Violation                                                         |
      | detail    | Customer has associated vehicles and cannot be deleted. Remove vehicles first. |
