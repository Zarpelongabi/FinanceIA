package com.financeai.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "transactions")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private double amount;
    private String categoryId;
    private String categoryName;
    private String establishment;
    private String notes;
    private long date;
    private boolean isExpense; // true = gasto, false = receita
    private boolean isRecurring;
    private String receiptImagePath;
    private boolean isOcrExtracted;
    private String type; // "manual", "ocr", "voice"

    // Construtor padrão para o Room
    public Transaction() {}

    @Ignore
    public Transaction(String title, double amount, String categoryId,
                       String establishment, long date, boolean isExpense) {
        this.title = title;
        this.amount = amount;
        this.categoryId = categoryId;
        this.establishment = establishment;
        this.date = date;
        this.isExpense = isExpense;
        this.type = "manual";
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getEstablishment() { return establishment; }
    public void setEstablishment(String establishment) { this.establishment = establishment; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public boolean isExpense() { return isExpense; }
    public void setExpense(boolean expense) { isExpense = expense; }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    public String getReceiptImagePath() { return receiptImagePath; }
    public void setReceiptImagePath(String receiptImagePath) { this.receiptImagePath = receiptImagePath; }

    public boolean isOcrExtracted() { return isOcrExtracted; }
    public void setOcrExtracted(boolean ocrExtracted) { isOcrExtracted = ocrExtracted; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}