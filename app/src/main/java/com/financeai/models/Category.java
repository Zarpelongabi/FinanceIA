package com.financeai.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String emoji;
    private String color;       // hex color
    private double monthlyLimit;
    private boolean isCustom;
    private String keywords;    // JSON array de palavras-chave para auto-categorização

    public Category() {}

    @Ignore
    public Category(String name, String emoji, String color, double monthlyLimit) {
        this.name = name;
        this.emoji = emoji;
        this.color = color;
        this.monthlyLimit = monthlyLimit;
        this.isCustom = false;
    }

    // Categorias padrão do sistema
    public static Category[] getDefaultCategories() {
        return new Category[]{
            createDefault("Alimentação", "🍔", "#1D9E75", 500.0,
                "mcdonalds,burguer king,ifood,rappi,mercado,supermercado,restaurante,lanchonete,padaria,peixaria,extra,carrefour,walmart,atacadao"),
            createDefault("Transporte", "🚌", "#185FA5", 200.0,
                "uber,99,cabify,onibus,passagem,combustivel,posto,gasolina,estacionamento,metro,trem"),
            createDefault("Lazer", "🎬", "#D4537E", 300.0,
                "cinema,netflix,spotify,amazon prime,disney,shopping,loja,roupa,sapato,livro"),
            createDefault("Contas", "⚡", "#BA7517", 400.0,
                "energia,luz,agua,gas,internet,telefone,aluguel,condominio,iptu"),
            createDefault("Saúde", "💊", "#E24B4A", 200.0,
                "farmacia,drogaria,medico,consulta,hospital,clinica,plano de saude,droga raia"),
            createDefault("Educação", "📚", "#533AB7", 300.0,
                "escola,faculdade,curso,livro,papelaria,material escolar,alura,udemy"),
            createDefault("Outros", "📦", "#888780", 0.0, "")
        };
    }

    private static Category createDefault(String name, String emoji, String color,
                                           double limit, String keywords) {
        Category c = new Category(name, emoji, color, limit);
        c.setKeywords(keywords);
        return c;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public double getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(double monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public String getDisplayName() {
        return emoji + " " + name;
    }
}