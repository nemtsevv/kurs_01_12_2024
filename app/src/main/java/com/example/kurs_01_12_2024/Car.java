package com.example.kurs_01_12_2024;

import android.os.Parcel;
import android.os.Parcelable;

public class Car {
    private int id;
    private String make;
    private String model;
    private int mileage;
    private Integer oilChangeMileage;
    private String oilChangeDate;

    // Конструктор для трех параметров (без oilChangeMileage и oilChangeDate)
    public Car(int id, String make, String model, int mileage) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.mileage = mileage;
        this.oilChangeMileage = null; // По умолчанию нет информации о пробеге для замены масла
        this.oilChangeDate = null;    // По умолчанию нет даты замены масла
    }

    // Конструктор с полными параметрами
    public Car(int id, String make, String model, int mileage, Integer oilChangeMileage, String oilChangeDate) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.mileage = mileage;
        this.oilChangeMileage = oilChangeMileage;
        this.oilChangeDate = oilChangeDate;
    }

    // Геттеры и сеттеры для всех полей
    public int getId() {
        return id;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public int getMileage() {
        return mileage;
    }

    public Integer getOilChangeMileage() {
        return oilChangeMileage;
    }

    public String getOilChangeDate() {
        return oilChangeDate;
    }

    // Сеттеры можно добавить по необходимости
    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public void setOilChangeMileage(Integer oilChangeMileage) {
        this.oilChangeMileage = oilChangeMileage;
    }

    public void setOilChangeDate(String oilChangeDate) {
        this.oilChangeDate = oilChangeDate;
    }
}

