package com.example.kurs_01_12_2024;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CarDetailActivity extends AppCompatActivity {

    private CarDatabaseHelper dbHelper;
    private int carId;  // ID автомобиля
    private String carMake, carModel;
    private int carMileage;

    private TextView textViewMake, textViewModel, textViewMileage;
    private ImageView imageViewLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        dbHelper = new CarDatabaseHelper(this);

        // Получаем данные из Intent
        carId = getIntent().getIntExtra("carId", -1);
        carMake = getIntent().getStringExtra("carMake");
        carModel = getIntent().getStringExtra("carModel");
        carMileage = getIntent().getIntExtra("carMileage", -1);

        // Инициализируем Views
        textViewMake = findViewById(R.id.car_make);
        textViewModel = findViewById(R.id.car_model);
        textViewMileage = findViewById(R.id.car_mileage);
        imageViewLogo = findViewById(R.id.car_logo);

        // Установим данные в текстовые поля
        textViewMake.setText("Марка: " + carMake);
        textViewModel.setText("Модель: " + carModel);
        textViewMileage.setText("Пробег: " + carMileage + " км");

        // Устанавливаем логотип
        int logoResId = getLogoResId(carMake);
        imageViewLogo.setImageResource(logoResId);

        // Найдем кнопку
        FloatingActionButton fab = findViewById(R.id.fab_add_details);

        // Устанавливаем обработчик клика
        fab.setOnClickListener(v -> showOptionsDialog());
    }

    // Метод для отображения диалога с выбором действий
    private void showOptionsDialog() {
        String[] actions = new String[] {
                "Удалить автомобиль",
                "Добавить расход",
                "Изменить пробег",
                "Обслуживание"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите действие")
                .setItems(actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                deleteCar();
                                break;
                            case 1:
                                addExpense();
                                break;
                            case 2:
                                updateMileage();
                                break;
                            case 3:  // Обработка действия "Обслуживание"
                                openServiceActivity();
                                break;
                        }
                    }
                });

        builder.create().show();
    }

    // Метод для открытия активности "Обслуживание"
    private void openServiceActivity() {
        Intent intent = new Intent(CarDetailActivity.this, ServiceActivity.class);

        // Передаем пробег в Intent
        intent.putExtra("carMileage", carMileage);
        intent.putExtra("carMake", carMake);
        intent.putExtra("carModel", carModel);
        intent.putExtra("carId", carId);  // если это необходимо

        startActivity(intent);  // Запуск новой активности
    }

    // Метод для удаления автомобиля
    private void deleteCar() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.deleteCar(carId);  // Удаляем автомобиль

        // Уведомляем родительское Activity (MainActivity) об удалении
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        // Возвращаем результат в MainActivity
        finish();  // Завершаем активность

        Toast.makeText(this, "Автомобиль удален", Toast.LENGTH_SHORT).show();
    }

    // Метод для добавления расхода
    private void addExpense() {
        // Создадим и покажем диалог для добавления расхода
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить расход");

        // Инфлятируем диалоговое окно с полями
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        Spinner expenseTypeSpinner = dialogView.findViewById(R.id.spinner_expense_type);
        EditText expenseAmountEditText = dialogView.findViewById(R.id.edittext_expense_cost);
        EditText expenseDateEditText = dialogView.findViewById(R.id.edittext_expense_date);

        // Заполняем Spinner типами расходов
        ArrayAdapter<CharSequence> expenseTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.expense_types, android.R.layout.simple_spinner_item);
        expenseTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expenseTypeSpinner.setAdapter(expenseTypeAdapter);

        // Обработчик для выбора даты
        expenseDateEditText.setOnClickListener(v -> showDatePickerDialog(expenseDateEditText));

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String expenseType = expenseTypeSpinner.getSelectedItem().toString();
            String expenseAmountString = expenseAmountEditText.getText().toString();
            String expenseDate = expenseDateEditText.getText().toString();

            // Проверка на пустые значения
            if (!expenseAmountString.isEmpty() && !expenseDate.isEmpty()) {
                double expenseAmount = Double.parseDouble(expenseAmountString);
                String formattedDate = formatDateForDatabase(expenseDate);
                if (formattedDate != null) {
                    addExpenseToDatabase(expenseType, expenseAmount, formattedDate);
                } else {
                    Toast.makeText(CarDetailActivity.this, "Неверный формат даты", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CarDetailActivity.this, "Пожалуйста, заполните все поля.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    // Метод для добавления расхода в базу данных
    private void addExpenseToDatabase(String expenseType, double expenseAmount, String expenseDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("car_id", carId);
        values.put("type", expenseType);
        values.put("amount", expenseAmount);
        values.put("date", expenseDate);

        try {
            long newRowId = db.insert("expenses", null, values);
            if (newRowId != -1) {
                Toast.makeText(this, "Расход добавлен", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ошибка добавления расхода", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("DatabaseError", "Ошибка добавления расхода", e);
        }
    }

    // Метод для отображения DatePickerDialog
    private void showDatePickerDialog(EditText expenseDateEditText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = selectedYear + "-" + String.format(Locale.US, "%02d", selectedMonth + 1) + "-" + String.format(Locale.US, "%02d", selectedDay);
            expenseDateEditText.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    // Метод для форматирования даты в формат yyyy-MM-dd
    private String formatDateForDatabase(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            inputFormat.setLenient(false);
            inputFormat.parse(date);  // Проверка на корректность даты
            return date;  // Если дата валидна, возвращаем в том же формате
        } catch (Exception e) {
            return null;  // Если дата некорректна, возвращаем null
        }
    }

    // Метод для изменения пробега автомобиля
    private void updateMileage() {
        // Покажем диалог для изменения пробега
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменить пробег");

        // Создадим EditText для ввода нового пробега
        EditText mileageEditText = new EditText(this);
        mileageEditText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(mileageEditText);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String newMileageString = mileageEditText.getText().toString();
            if (!newMileageString.isEmpty()) {
                int newMileage = Integer.parseInt(newMileageString);
                updateMileageInDatabase(newMileage);
            } else {
                Toast.makeText(CarDetailActivity.this, "Пожалуйста, введите новый пробег.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    // Метод для обновления пробега в базе данных
    private void updateMileageInDatabase(int newMileage) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("mileage", newMileage);

        try {
            int rowsUpdated = db.update("cars", values, "car_id = ?", new String[]{String.valueOf(carId)});
            if (rowsUpdated > 0) {
                Toast.makeText(this, "Пробег обновлен", Toast.LENGTH_SHORT).show();
                textViewMileage.setText("Пробег: " + newMileage + " км");

                // Обновим пробег в MainActivity (если нужно передать результат в MainActivity)
                Intent resultIntent = new Intent();
                resultIntent.putExtra("newMileage", newMileage);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Ошибка обновления пробега", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("DatabaseError", "Ошибка обновления пробега", e);
        }
    }

    // Метод для получения изображения логотипа марки автомобиля
    private int getLogoResId(String carMake) {
        switch (carMake) {
            case "Audi":
                return R.drawable.audi_logo;
            case "BMW":
                return R.drawable.bmw_logo;
            case "Toyota":
                return R.drawable.toyota_logo;
            case "Honda":
                return R.drawable.honda_logo;
            case "Ford":
                return R.drawable.ford_logo;
            // Добавьте дополнительные марки по мере необходимости
            default:
                return R.drawable.default_logo;
        }
    }
}
