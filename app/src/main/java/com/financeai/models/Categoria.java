package com.financeai.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Representa as categorias de gastos no FinanceiroIA.
 */
@Entity(tableName = "categorias")
public class Categoria {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String nome;
    private String icone;
    private String corHex;

    public Categoria() {}

    public Categoria(String nome, String icone, String corHex) {
        this.nome = nome;
        this.icone = icone;
        this.corHex = corHex;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }

    public String getCorHex() { return corHex; }
    public void setCorHex(String corHex) { this.corHex = corHex; }
}
