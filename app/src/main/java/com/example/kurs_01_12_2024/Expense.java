package com.example.kurs_01_12_2024;

public class Expense {
    private double amount;
    private String type;
    private String date;
    private int termMonths;

    // Конструктор, который принимает 4 параметра (amount, type, date, termMonths)
    public Expense(double amount, String type, String date, int termMonths) {
        this.amount = amount;
        this.type = type;  // Тип расхода (например, "Страховка" или "Тех. осмотр")
        this.date = date;
        this.termMonths = termMonths;
    }

    // Геттеры
    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public int getTermMonths() {
        return termMonths;
    }
}
