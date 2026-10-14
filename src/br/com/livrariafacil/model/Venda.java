package br.com.livrariafacil.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Venda {
    private int id;
    private final Cliente cliente;
    private final LocalDateTime data;
    private final List<ItemDeVenda> itens = new ArrayList<>();

    public Venda(int id, Cliente cliente) {
        this.id = id;
        this.cliente = Objects.requireNonNull(cliente, "Cliente é obrigatório.");
        this.data = LocalDateTime.now();
    }

    public void adicionarItem(ItemDeVenda item) {
        itens.add(Objects.requireNonNull(item, "Item de venda é obrigatório."));
    }

    public double calcularTotal() {
        return itens.stream().mapToDouble(ItemDeVenda::getSubtotal).sum();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public LocalDateTime getData() {
        return data;
    }

    public List<ItemDeVenda> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
