# Relatório de refatoração — Livraria Fácil

**Projeto:** Livraria Fácil Core

**Linguagem:** Java 17

**IDE/projeto:** NetBeans, projeto Java Ant

**Objetivo:** separar modelos e regras de negócio do sistema desktop para possibilitar seu reaproveitamento em uma futura aplicação web.

## 1. Princípios SOLID aplicados

| Princípio | Classes afetadas | Aplicação e justificativa |
|---|---|---|
| **SRP — Responsabilidade única** | `Cliente`, `Livro`, `ItemDeVenda`, `Venda`, `LivrariaService`, `ClienteRepository`, `LivroRepository`, `VendaRepository` | As entidades representam conceitos do domínio sem elementos de tela ou persistência. `LivrariaService` concentra os casos de uso e validações de negócio, sem abrir conexão ou controlar componentes Swing. Os contratos de repositório delimitam as operações de persistência por tipo de entidade. Essa separação permite alterar a interface ou a tecnologia de banco sem misturar essas mudanças às regras. |
| **OCP — Aberto/fechado** | `LivrariaService`, `ILivrariaService`, contratos `ClienteRepository`, `LivroRepository` e `VendaRepository` | O serviço trabalha com abstrações. Novos adaptadores, como uma implementação JDBC, ORM ou API, podem ser adicionados por meio dos contratos sem modificar as regras existentes. |
| **LSP — Substituição de Liskov** | Contratos `ClienteRepository`, `LivroRepository`, `VendaRepository`; dublês usados em `LivrariaServiceTest` | Os testes fornecem implementações em memória dos contratos no lugar de adaptadores reais. Isso demonstra a substituição no limite de persistência. Não foi criada hierarquia de entidades nem se afirma que existam implementações de produção desses contratos neste módulo. |
| **ISP — Segregação de interfaces** | `ClienteRepository`, `LivroRepository`, `VendaRepository` | As operações foram separadas por responsabilidade de persistência, evitando que um consumidor dependa de métodos de repositório que não usa. |
| **DIP — Inversão de dependência** | `LivrariaService`, `ILivrariaService`, contratos de repositório | `LivrariaService` recebe suas dependências pelo construtor e depende das interfaces de repositório, não das implementações concretas de DAO ou de `ConnectionFactory`. `ILivrariaService` fornece uma abstração para consumidores do serviço. A composição concreta das dependências fica a cargo da aplicação. |

## 2. Refatorações e classes/métodos afetados

O código foi preparado em um **novo projeto independente**, preservando o projeto desktop original. As classes Swing não foram copiadas para o núcleo.

| Classe ou método | Refatoração |
|---|---|
| `Cliente`, `Livro` | Modelos de domínio sem dependência da interface Swing ou de JDBC. Em `Livro.reduzirEstoque`, quantidades não positivas agora são recusadas, além de quantidades superiores ao estoque disponível. |
| `ItemDeVenda` | O preço unitário é capturado na criação do item e o subtotal é calculado a partir dele. Isso impede que alterações posteriores no cadastro de um livro alterem o valor de uma venda já montada. A quantidade e o livro são validados no construtor. |
| `Venda` | A lista de itens é exposta como visão não modificável, evitando alterações externas que contornem o domínio. A data é representada por `LocalDateTime`. |
| `LivrariaService.cadastrarLivro` | Valida livro, título, preço finito e positivo e estoque não negativo antes de delegar ao repositório. |
| `LivrariaService.cadastrarCliente` | Valida cliente, nome e CPF com exatamente 11 dígitos antes de delegar ao repositório. |
| `LivrariaService.finalizarVenda` | Valida cliente, itens e livros cadastrados; soma as quantidades de linhas repetidas do mesmo livro para verificar o estoque total solicitado; preserva as linhas e preços unitários da venda; só encaminha a venda para persistência após as validações. |
| `LivrariaService.listarLivros`, `listarClientes` | Devolvem cópias não modificáveis das listas retornadas pelos repositórios. |
| `ClienteRepository`, `LivroRepository`, `VendaRepository` | Novos contratos que substituem a dependência do serviço desktop em interfaces/implementações DAO acopladas à infraestrutura. `VendaRepository.registrar` estabelece o limite para persistir a venda e atualizar estoque atomicamente. |
| `ILivrariaService` | Contrato para os casos de uso disponibilizados pelo serviço, desacoplando também seus consumidores da implementação concreta. |

### Padrões e organização

- **Repository:** os três contratos isolam os casos de uso da forma de acesso aos dados. A futura camada web poderá receber adaptadores para esses contratos sem conhecer detalhes de SQL.
- **Injeção de dependência por construtor:** `LivrariaService` recebe explicitamente os repositórios obrigatórios. Isso elimina a criação de DAOs concretos dentro do serviço e facilita testes com implementações em memória.
- **Separação por camadas/portas:** modelos e regras ficam no núcleo; persistência e interface são fronteiras externas. Não foi incluído adaptador JDBC nem interface web neste projeto. A implementação de `VendaRepository.registrar` ainda deverá assegurar a transação no banco.

### Testes realizados

`LivrariaServiceTest` verifica a rejeição de livro inválido e CPF inválido, o cálculo de estoque agregado para linhas duplicadas, a não persistência de venda sem estoque e a preservação do preço unitário original. Os testes usam repositórios em memória e não necessitam de banco de dados.

Validação executada neste ambiente: compilação das fontes e dos testes com `javac --release 17` e execução da classe de testes; todos os testes passaram. O Ant não estava instalado, portanto o alvo `ant clean test jar` não foi executado aqui.

## 3. Evidências do repositório GitHub

- **Repositório privado:** [Antoniokobe/livrariafacil-core](https://github.com/Antoniokobe/livrariafacil-core)
- **Branch publicada:** [`main`](https://github.com/Antoniokobe/livrariafacil-core/tree/main)
- **Este projeto e o relatório:** [arquivos na branch main](https://github.com/Antoniokobe/livrariafacil-core/tree/main)
- **Histórico de commits:** [commits da branch main](https://github.com/Antoniokobe/livrariafacil-core/commits/main)
- **Commits de entrega:** `6892a2f` (projeto e testes), `bf4e115` (primeiras evidências no README) e `3fdd9d3` (detalhamento das classes no README).
