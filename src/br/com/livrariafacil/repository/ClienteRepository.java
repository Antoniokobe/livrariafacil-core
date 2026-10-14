package br.com.livrariafacil.repository;

import br.com.livrariafacil.model.Cliente;
import java.util.List;

public interface ClienteRepository {
    void salvar(Cliente cliente);

    List<Cliente> obterTodos();
}
