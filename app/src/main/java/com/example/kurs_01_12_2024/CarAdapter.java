package com.example.kurs_01_12_2024;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {
    private Context context;
    private List<Car> carList;
    private OnCarClickListener onCarClickListener;

    public interface OnCarClickListener {
        void onCarClick(Car car);
        void onDeleteCarClick(Car car);
    }

    public CarAdapter(Context context, List<Car> carList, OnCarClickListener listener) {
        this.context = context;
        this.carList = carList;
        this.onCarClickListener = listener;
    }

    @Override
    public CarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.car_item, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CarViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.carMake.setText(car.getMake());
        holder.carModel.setText(car.getModel());
        holder.carMileage.setText(String.valueOf(car.getMileage()));

        // Устанавливаем изображение логотипа автомобиля
        String make = car.getMake();
        int logoResId = getCarLogoResId(make);
        holder.carLogo.setImageResource(logoResId);

        holder.itemView.setOnClickListener(v -> onCarClickListener.onCarClick(car));
        holder.itemView.setOnLongClickListener(v -> {
            onCarClickListener.onDeleteCarClick(car);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    private int getCarLogoResId(String make) {
        switch (make.toLowerCase()) {
            case "audi": return R.drawable.audi_logo;
            case "bmw": return R.drawable.bmw_logo;
            case "toyota": return R.drawable.toyota_logo;
            case "honda": return R.drawable.honda_logo;
            case "ford": return R.drawable.ford_logo;
            default: return R.drawable.default_logo;
        }
    }

    public void updateCars(List<Car> newCars) {
        this.carList = newCars;
        notifyDataSetChanged();  // Обновляем отображение
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        TextView carMake, carModel, carMileage;
        ImageView carLogo;

        public CarViewHolder(View itemView) {
            super(itemView);
            carMake = itemView.findViewById(R.id.car_make);
            carModel = itemView.findViewById(R.id.car_model);
            carMileage = itemView.findViewById(R.id.car_mileage);
            carLogo = itemView.findViewById(R.id.car_logo);
        }
    }
}
