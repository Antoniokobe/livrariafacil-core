package br.com.livrariafacil.service;

import br.com.livrariafacil.model.Cliente;
import br.com.livrariafacil.model.ItemDeVenda;
import br.com.livrariafacil.model.Livro;
import br.com.livrariafacil.model.Venda;
import java.util.List;

public interface ILivrariaService {
    void cadastrarLivro(Livro livro);

    List<Livro> listarLivros();

    void cadastrarCliente(Cliente cliente);

    List<Cliente> listarClientes();

    Venda finalizarVenda(Cliente cliente, List<ItemDeVenda> itens);

    double calcularTotalVenda(List<ItemDeVenda> itens);
}
