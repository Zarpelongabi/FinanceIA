package com.financeai.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Representa uma transação financeira no sistema FinanceiroIA.
 */
@Entity(tableName = "transacoes")
public class Transacao {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String titulo;
    private double valor;
    private long data;
    private String categoriaNome;
    private String estabelecimento;

    // Construtor padrão para o Room
    public Transacao() {}

    @Ignore
    public Transacao(String titulo, double valor, long data, String categoriaNome, String estabelecimento) {
        this.titulo = titulo;
        this.valor = valor;
        this.data = data;
        this.categoriaNome = categoriaNome;
        this.estabelecimento = estabelecimento;
    }

    // Getters e Setters traduzidos
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public long getData() { return data; }
    public void setData(long data) { this.data = data; }

    public String getCategoriaNome() { return categoriaNome; }
    public void setCategoriaNome(String categoriaNome) { this.categoriaNome = categoriaNome; }

    public String getEstabelecimento() { return estabelecimento; }
    public void setEstabelecimento(String estabelecimento) { this.estabelecimento = estabelecimento; }
}
