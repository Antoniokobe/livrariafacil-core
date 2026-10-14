package br.com.livrariafacil.repository;

import br.com.livrariafacil.model.Venda;

public interface VendaRepository {
    /**
     * Registra a venda e atualiza o estoque como uma única operação atômica.
     */
    void registrar(Venda venda);
}
