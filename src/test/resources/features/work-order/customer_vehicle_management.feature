# language: pt
@e2e @cliente @veiculo @ordem-de-servico
Funcionalidade: Gestão de Clientes e Veículos Vinculados
  Como atendente da oficina mecânica
  Quero cadastrar e consultar clientes e seus respectivos veículos
  Para manter a base cadastral atualizada e viabilizar a abertura de ordens de serviço

  # ============================================================================
  # Contexto: Disponibilidade Operacional do Serviço de Ordens de Serviço
  # ============================================================================
  Contexto:
    Dado que o serviço de Ordem de Serviço está em execução e operacional

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
