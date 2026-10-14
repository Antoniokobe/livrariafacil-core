package br.com.livrariafacil.service;

import br.com.livrariafacil.model.Cliente;
import br.com.livrariafacil.model.ItemDeVenda;
import br.com.livrariafacil.model.Livro;
import br.com.livrariafacil.model.Venda;
import br.com.livrariafacil.repository.ClienteRepository;
import br.com.livrariafacil.repository.LivroRepository;
import br.com.livrariafacil.repository.VendaRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class LivrariaService implements ILivrariaService {
    private static final Pattern CPF_VALIDO = Pattern.compile("\\d{11}");

    private final ClienteRepository clienteRepository;
    private final LivroRepository livroRepository;
    private final VendaRepository vendaRepository;

    public LivrariaService(ClienteRepository clienteRepository,
                           LivroRepository livroRepository,
                           VendaRepository vendaRepository) {
        this.clienteRepository = Objects.requireNonNull(clienteRepository);
        this.livroRepository = Objects.requireNonNull(livroRepository);
        this.vendaRepository = Objects.requireNonNull(vendaRepository);
    }

    @Override
    public void cadastrarLivro(Livro livro) {
        Objects.requireNonNull(livro, "Livro não pode ser nulo.");
        if (livro.getTitulo() == null || livro.getTitulo().isBlank()) {
            throw new IllegalArgumentException("Título do livro é obrigatório.");
        }
        if (!Double.isFinite(livro.getPrecoVenda()) || livro.getPrecoVenda() <= 0) {
            throw new IllegalArgumentException("Preço de venda deve ser maior que zero.");
        }
        if (livro.getQuantidadeEstoque() < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo.");
        }
        livroRepository.salvar(livro);
    }

    @Override
    public List<Livro> listarLivros() {
        return List.copyOf(livroRepository.obterTodos());
    }

    @Override
    public void cadastrarCliente(Cliente cliente) {
        Objects.requireNonNull(cliente, "Cliente não pode ser nulo.");
        if (cliente.getNome() == null || cliente.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do cliente é obrigatório.");
        }
        if (cliente.getCpf() == null || !CPF_VALIDO.matcher(cliente.getCpf()).matches()) {
            throw new IllegalArgumentException("CPF inválido. Deve conter 11 dígitos.");
        }
        clienteRepository.salvar(cliente);
    }

    @Override
    public List<Cliente> listarClientes() {
        return List.copyOf(clienteRepository.obterTodos());
    }

    @Override
    public Venda finalizarVenda(Cliente cliente, List<ItemDeVenda> itens) {
        Objects.requireNonNull(cliente, "Cliente não pode ser nulo.");
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Carrinho vazio. Adicione itens antes de finalizar.");
        }

        Map<Integer, Integer> quantidadePorLivro = new java.util.LinkedHashMap<>();
        Map<Integer, Livro> livrosPorId = new java.util.LinkedHashMap<>();
        for (ItemDeVenda item : itens) {
            Objects.requireNonNull(item, "O carrinho não pode conter itens nulos.");
            Livro livro = item.getLivro();
            if (livro.getId() <= 0) {
                throw new IllegalArgumentException("Livro da venda deve estar cadastrado.");
            }
            if (!Double.isFinite(item.getPrecoUnitario()) || item.getPrecoUnitario() <= 0) {
                throw new IllegalArgumentException("Preço unitário deve ser maior que zero.");
            }
            quantidadePorLivro.merge(livro.getId(), item.getQuantidade(), Math::addExact);
            livrosPorId.putIfAbsent(livro.getId(), livro);
        }

        for (Map.Entry<Integer, Integer> entrada : quantidadePorLivro.entrySet()) {
            Livro livro = livrosPorId.get(entrada.getKey());
            if (!livroRepository.temEstoqueSuficiente(livro.getId(), entrada.getValue())) {
                throw new IllegalStateException(
                    "Estoque insuficiente para: \"" + livro.getTitulo()
                    + "\" (disponível: " + livro.getQuantidadeEstoque()
                    + ", solicitado: " + entrada.getValue() + ")"
                );
            }
        }

        Venda venda = new Venda(0, cliente);
        itens.forEach(venda::adicionarItem);
        vendaRepository.registrar(venda);
        return venda;
    }

    @Override
    public double calcularTotalVenda(List<ItemDeVenda> itens) {
        if (itens == null || itens.isEmpty()) {
            return 0.0;
        }
        return itens.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ItemDeVenda::getSubtotal)
                .sum();
    }
}
