package com.financeai.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "goals")
public class Goal {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private double targetAmount;
    private double currentAmount;
    private String deadline;
    private String category;

    public Goal(String title, double targetAmount, double currentAmount, String deadline, String category) {
        this.title = title;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
        this.category = category;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public double getTargetAmount() { return targetAmount; }
    public double getCurrentAmount() { return currentAmount; }
    public String getDeadline() { return deadline; }
    public String getCategory() { return category; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }
}
