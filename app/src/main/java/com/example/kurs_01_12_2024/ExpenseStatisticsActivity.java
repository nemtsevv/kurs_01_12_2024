package com.example.kurs_01_12_2024;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExpenseStatisticsActivity extends AppCompatActivity {

    private static final String TAG = "ExpenseStatisticsActivity";  // Лог для отладки

    private CarDatabaseHelper dbHelper;
    private Spinner spinnerCar;
    private EditText editTextStartDate, editTextEndDate;
    private RecyclerView recyclerView;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList;
    private TextView totalExpenseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_statistics);

        dbHelper = new CarDatabaseHelper(this);
        expenseList = new ArrayList<>();

        spinnerCar = findViewById(R.id.spinner_car);
        editTextStartDate = findViewById(R.id.edittext_start_date);
        editTextEndDate = findViewById(R.id.edittext_end_date);
        recyclerView = findViewById(R.id.recycler_view_expenses);
        totalExpenseTextView = findViewById(R.id.textview_total_expense);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(expenseList);
        recyclerView.setAdapter(expenseAdapter);

        loadCarsIntoSpinner();

        editTextStartDate.setOnClickListener(v -> showDatePickerDialog(editTextStartDate));
        editTextEndDate.setOnClickListener(v -> showDatePickerDialog(editTextEndDate));

        Button showExpensesButton = findViewById(R.id.button_show_expenses);
        showExpensesButton.setOnClickListener(view -> showExpenses());
    }

    private void loadCarsIntoSpinner() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("cars", new String[]{"car_id", "make", "model"}, null, null, null, null, null);

        List<String> carNames = new ArrayList<>();
        List<Integer> carIds = new ArrayList<>();

        if (cursor != null) {
            int carIdIndex = cursor.getColumnIndex("car_id");
            int makeIndex = cursor.getColumnIndex("make");
            int modelIndex = cursor.getColumnIndex("model");

            while (cursor.moveToNext()) {
                int carId = cursor.getInt(carIdIndex);
                String carMake = cursor.getString(makeIndex);
                String carModel = cursor.getString(modelIndex);
                carNames.add(carMake + " " + carModel);
                carIds.add(carId);
            }
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCar.setAdapter(adapter);
    }

    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            // Формируем строку даты в формате dd/MM/yyyy
            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;

            // Обновляем EditText с выбранной датой
            editText.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void showExpenses() {
        String selectedCar = spinnerCar.getSelectedItem().toString();
        int carId = getCarIdByName(selectedCar);
        String startDate = editTextStartDate.getText().toString();
        String endDate = editTextEndDate.getText().toString();

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, выберите даты", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Car selected: " + selectedCar);
        Log.d(TAG, "Start date: " + startDate);
        Log.d(TAG, "End date: " + endDate);

        loadExpensesFromDatabase(carId, startDate, endDate);
    }

    private int getCarIdByName(String carName) {
        String[] parts = carName.split(" ");  // Разделяем строку по пробелу

        if (parts.length < 2) {
            Log.e(TAG, "Ошибка: строка автомобиля не имеет ожидаемого формата (Марка Модель).");
            return -1;  // Возвращаем -1, если строка не в правильном формате
        }

        String make = parts[0];
        String model = parts[1];

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("cars", new String[]{"car_id"}, "make = ? AND model = ?", new String[]{make, model}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int carIdIndex = cursor.getColumnIndex("car_id");
            int carId = cursor.getInt(carIdIndex);
            cursor.close();
            return carId;
        }

        Log.e(TAG, "Ошибка: не удалось найти машину с маркой " + make + " и моделью " + model);
        return -1;
    }

    private void loadExpensesFromDatabase(int carId, String startDate, String endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Преобразуем даты в формат yyyy-MM-dd
        String formattedStartDate = dbHelper.formatDateForDatabase(startDate);
        String formattedEndDate = dbHelper.formatDateForDatabase(endDate);

        if (formattedStartDate == null || formattedEndDate == null) {
            Toast.makeText(this, "Ошибка формата даты", Toast.LENGTH_SHORT).show();
            return;
        }

        // SQL-запрос для получения расходов в указанном диапазоне дат
        String query = "SELECT * FROM expenses WHERE car_id = ? AND date BETWEEN ? AND ? ORDER BY date ASC";

        Log.d(TAG, "Executing query: " + query);

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(carId), formattedStartDate, formattedEndDate});

        expenseList.clear();  // Очищаем старые данные
        double totalAmount = 0.0;  // Переменная для хранения общей суммы

        // Обработка результатов запроса
        if (cursor != null) {
            int expenseIdIndex = cursor.getColumnIndex("expense_id");
            int amountIndex = cursor.getColumnIndex("amount");
            int dateIndex = cursor.getColumnIndex("date");
            int typeIndex = cursor.getColumnIndex("type");
            int termMonthsIndex = cursor.getColumnIndex("term_months");  // Добавлено для термина

            while (cursor.moveToNext()) {
                int expenseId = cursor.getInt(expenseIdIndex);
                double amount = cursor.getDouble(amountIndex);
                String date = cursor.getString(dateIndex);
                String type = cursor.getString(typeIndex);
                int termMonths = cursor.getInt(termMonthsIndex);  // Получаем срок

                // Логируем каждый расход
                Log.d(TAG, "Expense: " + expenseId + " | " + amount + " | " + date + " | " + type);

                // Используем исправленный конструктор класса Expense с 4 параметрами
                Expense expense = new Expense(amount, type, date, termMonths);
                expenseList.add(expense);

                // Добавляем сумму расхода к общей
                totalAmount += amount;
            }
            cursor.close();
        } else {
            Log.d(TAG, "No expenses found in the database.");
        }

        // Если нет расходов в указанном периоде
        if (expenseList.isEmpty()) {
            Toast.makeText(this, "Нет расходов за этот период", Toast.LENGTH_SHORT).show();
        }

        // Обновляем адаптер для отображения списка
        expenseAdapter.notifyDataSetChanged();

        // Отображаем общую сумму расходов с валютой
        totalExpenseTextView.setText("Общая сумма расходов: " + totalAmount + " BYN");
    }
}
