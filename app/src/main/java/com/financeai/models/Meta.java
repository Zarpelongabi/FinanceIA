package com.financeai.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Representa uma meta financeira (gamificação) no FinanceiroIA.
 */
@Entity(tableName = "metas")
public class Meta {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String nome;
    private double valorObjetivo;
    private double valorAtual;
    private long dataLimite;
    private long dataCriacao;
    private String icone;

    public Meta() {}

    @Ignore
    public Meta(String nome, double valorObjetivo, double valorAtual, long dataLimite, String icone) {
        this.nome = nome;
        this.valorObjetivo = valorObjetivo;
        this.valorAtual = valorAtual;
        this.dataLimite = dataLimite;
        this.dataCriacao = System.currentTimeMillis();
        this.icone = icone;
    }

    public long getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(long dataCriacao) { this.dataCriacao = dataCriacao; }

    // Getters e Setters traduzidos
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getValorObjetivo() { return valorObjetivo; }
    public void setValorObjetivo(double valorObjetivo) { this.valorObjetivo = valorObjetivo; }

    public double getValorAtual() { return valorAtual; }
    public void setValorAtual(double valorAtual) { this.valorAtual = valorAtual; }

    public long getDataLimite() { return dataLimite; }
    public void setDataLimite(long dataLimite) { this.dataLimite = dataLimite; }

    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }
}
