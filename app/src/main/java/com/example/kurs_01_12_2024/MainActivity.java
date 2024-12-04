package com.example.kurs_01_12_2024;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private CarDatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Car> carList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация канала уведомлений
        NotificationHelper.createNotificationChannel(this);

        // Проверка разрешений для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        dbHelper = new CarDatabaseHelper(this);
        carList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        carAdapter = new CarAdapter(this, carList, new CarAdapter.OnCarClickListener() {
            @Override
            public void onCarClick(Car car) {
                openCarDetailActivity(car);
            }

            @Override
            public void onDeleteCarClick(Car car) {
                deleteCar(car.getId());
            }
        });
        recyclerView.setAdapter(carAdapter);

        loadCarsFromDatabase(); // Загрузка данных из базы данных

        // Периодическая задача для проверки пробега
        scheduleMileageCheck();

        // Инициализация кнопки "+" для выбора действия (добавить автомобиль или статистика расходов)
        FloatingActionButton fabAddCar = findViewById(R.id.fab);
        fabAddCar.setOnClickListener(v -> showOptionsDialog());  // Открываем диалог выбора действия
    }

    private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите действие")
                .setItems(new String[]{"Добавить автомобиль", "Статистика расходов"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showAddCarDialog();
                            break;
                        case 1:
                            openExpenseStatisticsActivity();
                            break;
                    }
                });
        builder.create().show();
    }

    private void openExpenseStatisticsActivity() {
        Intent intent = new Intent(MainActivity.this, ExpenseStatisticsActivity.class);
        startActivity(intent);  // Открываем активность статистики расходов
    }

    private void showAddCarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_car, null);
        builder.setView(dialogView);

        Spinner spinnerMake = dialogView.findViewById(R.id.spinner_make);
        Spinner spinnerModel = dialogView.findViewById(R.id.spinner_model);
        EditText editTextMileage = dialogView.findViewById(R.id.edittext_mileage);

        ArrayAdapter<CharSequence> makeAdapter = ArrayAdapter.createFromResource(this,
                R.array.car_makes, android.R.layout.simple_spinner_item);
        makeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMake.setAdapter(makeAdapter);

        spinnerMake.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int modelsArrayId;
                switch (position) {
                    case 0:
                        modelsArrayId = R.array.audi_models;
                        break;
                    case 1:
                        modelsArrayId = R.array.bmw_models;
                        break;
                    case 2:
                        modelsArrayId = R.array.toyota_models;
                        break;
                    case 3:
                        modelsArrayId = R.array.honda_models;
                        break;
                    case 4:
                        modelsArrayId = R.array.ford_models;
                        break;
                    default:
                        modelsArrayId = R.array.default_models;
                        break;
                }
                ArrayAdapter<CharSequence> modelAdapter = ArrayAdapter.createFromResource(MainActivity.this,
                        modelsArrayId, android.R.layout.simple_spinner_item);
                modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerModel.setAdapter(modelAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        builder.setTitle("Добавить автомобиль")
                .setPositiveButton("Добавить", (dialog, which) -> {
                    String make = spinnerMake.getSelectedItem().toString();
                    String model = spinnerModel.getSelectedItem().toString();
                    String mileageStr = editTextMileage.getText().toString();

                    int mileage = Integer.parseInt(mileageStr);

                    addCar(make, model, mileage);
                })
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void addCar(String make, String model, int mileage) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("make", make);
        values.put("model", model);
        values.put("mileage", mileage);

        long newRowId = db.insert("cars", null, values);
        if (newRowId != -1) {
            // Создаем объект Car с тремя параметрами
            Car car = new Car((int) newRowId, make, model, mileage);
            carList.add(car);
            carAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Автомобиль добавлен", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Car added: " + make + " " + model + ", mileage: " + mileage);
        } else {
            Toast.makeText(this, "Ошибка добавления автомобиля", Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "Error adding car to database.");
        }
    }

    private void loadCarsFromDatabase() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("cars", null, null, null, null, null, null);

        if (cursor != null) {
            try {
                int idIndex = cursor.getColumnIndexOrThrow("car_id");
                int makeIndex = cursor.getColumnIndexOrThrow("make");
                int modelIndex = cursor.getColumnIndexOrThrow("model");
                int mileageIndex = cursor.getColumnIndexOrThrow("mileage");

                carList.clear();  // Очищаем список перед загрузкой новых данных

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idIndex);
                    String make = cursor.getString(makeIndex);
                    String model = cursor.getString(modelIndex);
                    int mileage = cursor.getInt(mileageIndex);

                    Car car = new Car(id, make, model, mileage);

                    carList.add(car);
                }

                cursor.close();
                carAdapter.notifyDataSetChanged();  // Уведомляем адаптер о новых данных
            } catch (Exception e) {
                Log.e("MainActivity", "Error loading cars from DB: " + e.getMessage());
            }
        } else {
            Log.e("MainActivity", "Cursor is null, no cars loaded.");
        }
    }

    public void deleteCar(int carId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.deleteCar(carId);  // Удаляем автомобиль
        loadCarsFromDatabase(); // Обновляем список после удаления
        Toast.makeText(this, "Автомобиль удален", Toast.LENGTH_SHORT).show();
    }

    private void openCarDetailActivity(Car car) {
        Intent intent = new Intent(MainActivity.this, CarDetailActivity.class);
        intent.putExtra("carId", car.getId());  // Передаем ID автомобиля
        intent.putExtra("carMake", car.getMake());  // Передаем марку автомобиля
        intent.putExtra("carModel", car.getModel());  // Передаем модель автомобиля
        intent.putExtra("carMileage", car.getMileage());  // Передаем пробег автомобиля
        startActivityForResult(intent, 1);  // Ожидаем результат, например, изменение пробега
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            int carId = data.getIntExtra("carId", -1);
            int updatedMileage = data.getIntExtra("updated Mileage", -1);

            if (carId != -1 && updatedMileage != -1) {
                updateCarMileageInList(carId, updatedMileage);
            }
        }
    }

    private void updateCarMileageInList(int carId, int updatedMileage) {
        for (Car car : carList) {
            if (car.getId() == carId) {
                car.setMileage(updatedMileage);  // Обновляем пробег в объекте Car
                carAdapter.notifyDataSetChanged();  // Уведомляем адаптер о изменении данных
                Toast.makeText(this, "Пробег обновлен", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    private void scheduleMileageCheck() {
        // Создаем задачу, которая будет проверять автомобили на пробег и отправлять уведомления
        WorkManager workManager = WorkManager.getInstance(this);
        PeriodicWorkRequest mileageCheckRequest = new PeriodicWorkRequest.Builder(MileageCheckWorker.class, 1, TimeUnit.DAYS)
                .build();

        // Запускаем работу
        workManager.enqueue(mileageCheckRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешения получены", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Разрешения не получены. Без уведомлений.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

