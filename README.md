# Livraria Fácil Core

Projeto Java NetBeans (Ant) com as regras de negócio e o modelo da Livraria Fácil, separado da interface desktop. A camada pode ser reutilizada por uma futura aplicação web, que implementará os contratos de repositório e será responsável pela composição das dependências.

## Abrir e validar

Abra esta pasta no NetBeans como projeto Java existente. Requer JDK 17 ou superior e Apache Ant.

```text
ant clean test jar
```

O JAR é gerado em `dist/LivrariaFacilCore.jar`. Os testes usam repositórios falsos em memória e não precisam de MySQL.

## Estrutura

- `model`: entidades e itens de negócio, sem componentes Swing.
- `repository`: contratos de persistência, sem dependência de JDBC.
- `service`: validações e casos de uso da livraria, com dependências recebidas no construtor.
- `test`: testes de negócio executáveis sem framework ou dependência externa.

## Relatório de refatoração e SOLID

### Princípios SOLID aplicados

- **SRP (responsabilidade única):** `LivrariaService` concentra validações e casos de uso; `Cliente`, `Livro`, `ItemDeVenda` e `Venda` representam o domínio; os contratos `ClienteRepository`, `LivroRepository` e `VendaRepository` isolam persistência. Classes de interface Swing, montagem de telas e `LivrariaController` não foram trazidas para o núcleo.
- **DIP (inversão de dependência):** `LivrariaService` recebe os três repositórios por construtor e depende somente de abstrações; os consumidores do serviço podem usar `ILivrariaService`. O núcleo não cria DAOs, abre conexão nem configura banco. A futura aplicação web pode fornecer implementações JDBC, ORM ou API.
- **ISP (segregação de interfaces):** cada contrato de repositório representa um conjunto coeso de operações de um agregado, em vez de obrigar a camada de negócio a depender de uma interface grande com operações não usadas.
- **OCP (aberto/fechado):** novas implementações de persistência podem ser adicionadas por meio dos contratos sem alterar as regras do serviço.

### Refatorações realizadas

- Extraídos `Cliente`, `Livro`, `ItemDeVenda` e `Venda` para o núcleo sem código de tela ou formatação de interface.
- Substituídas as dependências concretas do serviço desktop por `ClienteRepository`, `LivroRepository` e `VendaRepository`. A criação de dependências fica a cargo da aplicação que compõe o sistema.
- Removido o preço de subtotal mutável de `ItemDeVenda`: o item guarda o preço unitário observado na inclusão, evitando que uma alteração posterior do cadastro mude o valor de uma venda em andamento.
- `LivrariaService.finalizarVenda` agora rejeita referências inválidas, agrega quantidades repetidas do mesmo livro antes da verificação de estoque e mantém cada linha/preço original ao enviar a venda ao repositório.
- A persistência de uma venda e a baixa de estoque são explicitamente definidas como uma operação atômica em `VendaRepository.registrar`. A implementação de infraestrutura deverá garantir essa transação.
- `Venda.getItens` expõe uma visão não modificável, evitando que consumidores alterem a venda sem passar pelo domínio.
- Eliminados os construtores que montavam automaticamente serviços/DAOs concretos. Essa composição é responsabilidade do ponto de entrada do aplicativo, não do serviço.

### Padrões e arquitetura

O projeto usa **Repository** para isolar acesso a dados e **injeção de dependência por construtor** para desacoplar os casos de uso da infraestrutura. A separação segue camadas de domínio, aplicação e portas de persistência. Não inclui adaptador JDBC nem interface web; ambos podem ser implementados sem modificar `LivrariaService`.

## Evidências do GitHub

- Repositório: a publicar como `Antoniokobe/livrariafacil-core` (privado).
- URL e evidências de publicação serão preenchidas após a criação e o envio do repositório.
