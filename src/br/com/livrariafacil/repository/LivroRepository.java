package br.com.livrariafacil.repository;

import br.com.livrariafacil.model.Livro;
import java.util.List;

public interface LivroRepository {
    void salvar(Livro livro);

    List<Livro> obterTodos();

    boolean temEstoqueSuficiente(int idLivro, int quantidade);
}
