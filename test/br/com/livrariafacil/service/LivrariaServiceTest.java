package br.com.livrariafacil.service;

import br.com.livrariafacil.model.Cliente;
import br.com.livrariafacil.model.ItemDeVenda;
import br.com.livrariafacil.model.Livro;
import br.com.livrariafacil.model.Venda;
import br.com.livrariafacil.repository.ClienteRepository;
import br.com.livrariafacil.repository.LivroRepository;
import br.com.livrariafacil.repository.VendaRepository;
import java.util.ArrayList;
import java.util.List;

public final class LivrariaServiceTest {
    private LivrariaServiceTest() {
    }

    public static void main(String[] args) {
        deveValidarCadastroDeLivro();
        deveValidarCpfDoCliente();
        deveAgruparItensDuplicadosAntesDeValidarEstoque();
        deveManterPrecoDoItemNoMomentoDaInclusao();
        deveRegistrarVendaComItensEPrecosPreservados();
        System.out.println("Todos os testes passaram.");
    }

    private static void deveValidarCadastroDeLivro() {
        FakeRepositories repos = new FakeRepositories();
        LivrariaService service = repos.service();
        expect(IllegalArgumentException.class, () -> service.cadastrarLivro(
                new Livro(1, "isbn", " ", "autor", "categoria", 5, 10, 1)));
        check(repos.livros.livrosSalvos.isEmpty(), "Livro inválido não deve ser salvo.");
    }

    private static void deveValidarCpfDoCliente() {
        FakeRepositories repos = new FakeRepositories();
        expect(IllegalArgumentException.class, () -> repos.service().cadastrarCliente(
                new Cliente(1, "Ana", "123", "", "")));
        check(repos.clientes.clientesSalvos.isEmpty(), "Cliente inválido não deve ser salvo.");
    }

    private static void deveAgruparItensDuplicadosAntesDeValidarEstoque() {
        FakeRepositories repos = new FakeRepositories();
        LivrariaService service = repos.service();
        Livro livro = new Livro(10, "isbn", "Livro", "Autor", "Categoria", 5, 12, 5);
        repos.livros.estoqueDisponivel = 3;
        expect(IllegalStateException.class, () -> service.finalizarVenda(
                new Cliente(1, "Ana", "12345678901", "", ""),
                List.of(new ItemDeVenda(livro, 2), new ItemDeVenda(livro, 2))));
        check(repos.livros.quantidadeConsultada == 4,
                "A verificação deve considerar a quantidade total do mesmo livro.");
        check(repos.vendas.vendaRegistrada == null, "Venda sem estoque não deve ser registrada.");
    }

    private static void deveManterPrecoDoItemNoMomentoDaInclusao() {
        Livro livro = new Livro(10, "isbn", "Livro", "Autor", "Categoria", 5, 12, 5);
        ItemDeVenda item = new ItemDeVenda(livro, 2);
        livro.setPrecoVenda(20);
        check(item.getSubtotal() == 24, "Subtotal deve preservar o preço original.");
    }

    private static void deveRegistrarVendaComItensEPrecosPreservados() {
        FakeRepositories repos = new FakeRepositories();
        repos.livros.estoqueDisponivel = 10;
        Livro livro = new Livro(10, "isbn", "Livro", "Autor", "Categoria", 5, 12, 10);
        List<ItemDeVenda> itens = List.of(
                new ItemDeVenda(livro, 1),
                new ItemDeVenda(livro, 2));

        Venda venda = repos.service().finalizarVenda(
                new Cliente(1, "Ana", "12345678901", "", ""), itens);

        check(repos.vendas.vendaRegistrada == venda, "A venda validada deve ser registrada.");
        check(venda.getItens().size() == 2, "As linhas originais devem ser mantidas.");
        check(venda.calcularTotal() == 36, "O total deve usar os preços unitários originais.");
    }

    private static void expect(Class<? extends Throwable> expected, Runnable action) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (expected.isInstance(actual)) {
                return;
            }
            throw new AssertionError("Exceção inesperada: " + actual, actual);
        }
        throw new AssertionError("Esperada exceção " + expected.getSimpleName());
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class FakeRepositories {
        private final FakeClienteRepository clientes = new FakeClienteRepository();
        private final FakeLivroRepository livros = new FakeLivroRepository();
        private final FakeVendaRepository vendas = new FakeVendaRepository();

        private LivrariaService service() {
            return new LivrariaService(clientes, livros, vendas);
        }
    }

    private static final class FakeClienteRepository implements ClienteRepository {
        private final List<Cliente> clientesSalvos = new ArrayList<>();

        @Override
        public void salvar(Cliente cliente) {
            clientesSalvos.add(cliente);
        }

        @Override
        public List<Cliente> obterTodos() {
            return List.copyOf(clientesSalvos);
        }
    }

    private static final class FakeLivroRepository implements LivroRepository {
        private final List<Livro> livrosSalvos = new ArrayList<>();
        private int estoqueDisponivel;
        private int quantidadeConsultada;

        @Override
        public void salvar(Livro livro) {
            livrosSalvos.add(livro);
        }

        @Override
        public List<Livro> obterTodos() {
            return List.copyOf(livrosSalvos);
        }

        @Override
        public boolean temEstoqueSuficiente(int idLivro, int quantidade) {
            quantidadeConsultada = quantidade;
            return estoqueDisponivel >= quantidade;
        }
    }

    private static final class FakeVendaRepository implements VendaRepository {
        private Venda vendaRegistrada;

        @Override
        public void registrar(Venda venda) {
            vendaRegistrada = venda;
        }
    }
}
