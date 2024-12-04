package com.example.kurs_01_12_2024;

public class TechnicalCheck {
    private double amount;
    private String date;

    public TechnicalCheck(double amount, String date) {
        this.amount = amount;
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }
}