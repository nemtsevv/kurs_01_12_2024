package com.example.kurs_01_12_2024;

public class Insurance {
    private double amount;
    private String date;
    private int durationMonths;

    public Insurance(double amount, String date, int durationMonths) {
        this.amount = amount;
        this.date = date;
        this.durationMonths = durationMonths;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public int getDurationMonths() {
        return durationMonths;
    }
}