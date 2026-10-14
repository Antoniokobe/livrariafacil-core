package br.com.livrariafacil.model;

import java.util.Objects;

public class ItemDeVenda {
    private final Livro livro;
    private final int quantidade;
    private final double precoUnitario;

    public ItemDeVenda(Livro livro, int quantidade) {
        this.livro = Objects.requireNonNull(livro, "Livro é obrigatório.");
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        this.quantidade = quantidade;
        this.precoUnitario = livro.getPrecoVenda();
    }

    public Livro getLivro() {
        return livro;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public double getSubtotal() {
        return precoUnitario * quantidade;
    }
}
