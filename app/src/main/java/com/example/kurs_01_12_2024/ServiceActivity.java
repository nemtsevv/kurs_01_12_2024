package com.example.kurs_01_12_2024;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ServiceActivity extends AppCompatActivity {

    private CarDatabaseHelper dbHelper;
    private int carId;  // ID автомобиля
    private String carMake, carModel;
    private int carMileage;
    private Integer oilChangeMileage;
    private String oilChangeDate;

    private TextView textViewMake, textViewModel, textViewMileage, textViewOilChangeInfo, textViewNextOilChange, textViewInsuranceInfo, textViewTechCheckInfo;
    private ImageView imageViewLogo;

    private String insuranceAmount;
    private String insuranceDate;
    private int insuranceDurationMonths;  // Срок страховки: 6 или 12 месяцев

    private String techCheckDate;
    private double techCheckAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        // Получаем данные из Intent
        carId = getIntent().getIntExtra("carId", -1);
        carMake = getIntent().getStringExtra("carMake");
        carModel = getIntent().getStringExtra("carModel");
        carMileage = getIntent().getIntExtra("carMileage", -1);

        // Инициализация компонентов
        textViewOilChangeInfo = findViewById(R.id.oil_change_info);
        textViewNextOilChange = findViewById(R.id.next_oil_change_info);
        textViewInsuranceInfo = findViewById(R.id.insurance_info);
        textViewTechCheckInfo = findViewById(R.id.tech_check_info);

        // Получаем данные замены масла из базы данных
        dbHelper = new CarDatabaseHelper(this);
        Car car = dbHelper.getAllCars().stream().filter(c -> c.getId() == carId).findFirst().orElse(null);
        if (car != null) {
            oilChangeMileage = car.getOilChangeMileage();
            oilChangeDate = car.getOilChangeDate();
        }

        // Отображаем информацию о замене масла
        if (oilChangeMileage != null && oilChangeDate != null) {
            textViewOilChangeInfo.setText("Замена масла: " + oilChangeMileage + " км \n" + "Дата замены: " + oilChangeDate);
        } else {
            textViewOilChangeInfo.setText("Замена масла: Не проведена");
        }


        // Расчет следующей замены масла
        if (carMileage >= 0) {
            int nextOilChangeMileage = carMileage + 8000;
            textViewNextOilChange.setText("Следующая замена масла: " + nextOilChangeMileage + " км");
        }

        // Загружаем информацию о страховке
        loadInsuranceInfo();

        // Загружаем информацию о тех. осмотре
        loadTechCheckInfo();

        // Инициализация кнопок
        Button btnOilChange = findViewById(R.id.btn_oil_change);
        Button btnInsurance = findViewById(R.id.btn_insurance);
        Button btnTechnicalCheck = findViewById(R.id.btn_technical_check);

        // Устанавливаем обработчики кликов
        btnOilChange.setOnClickListener(v -> onOilChangeClick());
        btnInsurance.setOnClickListener(v -> onInsuranceClick());
        btnTechnicalCheck.setOnClickListener(v -> onTechnicalCheckClick());
    }



    private void changeOil() {
        // Покажем диалог для изменения замены масла
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Замена масла");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_oil, null);
        builder.setView(dialogView);

        EditText oilChangeMileageEditText = dialogView.findViewById(R.id.edittext_oil_change_mileage);
        EditText oilChangeDateEditText = dialogView.findViewById(R.id.edittext_oil_change_date);

        // Обработчик для выбора даты
        oilChangeDateEditText.setOnClickListener(v -> showDatePickerDialog(oilChangeDateEditText));

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String oilChangeMileageString = oilChangeMileageEditText.getText().toString();
            String oilChangeDate = oilChangeDateEditText.getText().toString();

            // Проверка на пустые значения
            if (!oilChangeMileageString.isEmpty() && !oilChangeDate.isEmpty()) {
                int oilChangeMileage = Integer.parseInt(oilChangeMileageString);
                String formattedDate = formatDateForDatabase(oilChangeDate);
                if (formattedDate != null) {
                    updateOilChangeInDatabase(oilChangeMileage, formattedDate);
                } else {
                    Toast.makeText(ServiceActivity.this, "Неверный формат даты", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ServiceActivity.this, "Пожалуйста, заполните все поля.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    // Метод для обновления информации о замене масла в базе данных
    private void updateOilChangeInDatabase(int newMileage, String newDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("oil_change_mileage", newMileage);
        values.put("oil_change_date", newDate);

        try {
            int rowsUpdated = db.update("cars", values, "car_id = ?", new String[]{String.valueOf(carId)});
            if (rowsUpdated > 0) {
                Toast.makeText(this, "Информация о замене масла обновлена", Toast.LENGTH_SHORT).show();
                textViewOilChangeInfo.setText("Замена масла: " + newMileage + " км \nДата замены: " + newDate);
            } else {
                Toast.makeText(this, "Ошибка обновления замены масла", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("DatabaseError", "Ошибка обновления замены масла", e);
        }
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

    private void showDatePickerDialog(EditText oilChangeDateEditText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = selectedYear + "-" + String.format(Locale.US, "%02d", selectedMonth + 1) + "-" + String.format(Locale.US, "%02d", selectedDay);
            oilChangeDateEditText.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }


    // Обработчик для кнопки "Замена масла"
    private void onOilChangeClick() {
        // Покажем диалог для ввода данных замены масла
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите данные замены масла");

        // Создаем view для диалога
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_oil, null);
        builder.setView(dialogView);

        // Инициализируем поля
        EditText mileageEditText = dialogView.findViewById(R.id.edittext_oil_change_mileage);
        EditText dateEditText = dialogView.findViewById(R.id.edittext_oil_change_date);

        // Устанавливаем обработчик для выбора даты
        dateEditText.setOnClickListener(v -> showDatePickerDialog(dateEditText));

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String mileage = mileageEditText.getText().toString();
            String date = dateEditText.getText().toString();

            // Проверка на пустые значения
            if (!mileage.isEmpty() && !date.isEmpty()) {
                try {
                    // Преобразуем пробег в целое число
                    int mileageValue = Integer.parseInt(mileage);

                    // Сохраняем данные о замене масла в базу
                    dbHelper.addOilChange(ServiceActivity.this, carId, mileageValue, date);

                    // Обновляем информацию о замене масла на экране
                    oilChangeMileage = mileageValue;
                    oilChangeDate = date;
                    updateOilChangeInfo();  // Обновляем информацию о замене масла на экране
                } catch (NumberFormatException e) {
                    Toast.makeText(ServiceActivity.this, "Ошибка: Пробег должен быть числом.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ServiceActivity.this, "Пожалуйста, заполните все поля.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


    // Метод для обновления информации о замене масла на экране
    private void updateOilChangeInfo() {
        Log.d("OilChangeInfo", "Mileage: " + oilChangeMileage + " Date: " + oilChangeDate);
        if (oilChangeMileage != null && oilChangeDate != null) {
            textViewOilChangeInfo.setText("Замена масла: " + oilChangeMileage + " км \n" + "Дата замены: " + oilChangeDate);
        } else {
            textViewOilChangeInfo.setText("Замена масла: Не проведена");
        }
    }


    // Обработчик для кнопки "Страховка"
    private void onInsuranceClick() {
        // Покажем диалог для ввода данных страховки
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите данные страховки");

        // Создаем view для диалога
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_insurance, null);
        builder.setView(dialogView);

        // Инициализируем поля
        EditText amountEditText = dialogView.findViewById(R.id.edittext_insurance_amount);
        EditText dateEditText = dialogView.findViewById(R.id.edittext_insurance_date);
        RadioButton radio6Months = dialogView.findViewById(R.id.radio_6_months);
        RadioButton radio12Months = dialogView.findViewById(R.id.radio_12_months);

        // Обработчик для выбора даты
        dateEditText.setOnClickListener(v -> showDatePickerDialog(dateEditText));

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String amount = amountEditText.getText().toString();
            String date = dateEditText.getText().toString();
            int duration = radio6Months.isChecked() ? 6 : (radio12Months.isChecked() ? 12 : 0);

            // Проверка на пустые значения и корректность срока
            if (!amount.isEmpty() && !date.isEmpty() && duration != 0) {
                insuranceAmount = amount;
                insuranceDate = date;
                insuranceDurationMonths = duration;

                // Сохраняем данные как расход
                dbHelper.addInsurance(ServiceActivity.this, carId, Double.parseDouble(insuranceAmount), "Страховка", insuranceDate, insuranceDurationMonths);
                updateInsuranceInfo();  // Обновляем информацию о страховке на экране
            } else {
                Toast.makeText(ServiceActivity.this, "Пожалуйста, заполните все поля.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // Обработчик для кнопки "Тех. осмотр"
    private void onTechnicalCheckClick() {
        // Покажем диалог для ввода данных тех. осмотра
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите данные тех. осмотра");

        // Создаем view для диалога
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_technical_check, null);
        builder.setView(dialogView);

        // Инициализируем поля
        EditText amountEditText = dialogView.findViewById(R.id.edittext_tech_check_amount);
        EditText dateEditText = dialogView.findViewById(R.id.edittext_tech_check_date);

        // Устанавливаем обработчик для выбора даты
        dateEditText.setOnClickListener(v -> showDatePickerDialog(dateEditText));

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String amount = amountEditText.getText().toString();
            String date = dateEditText.getText().toString();

            // Проверка на пустые значения
            if (!amount.isEmpty() && !date.isEmpty()) {
                techCheckAmount = Double.parseDouble(amount);
                techCheckDate = date;

                // Сохраняем данные о тех. осмотре в базу
                dbHelper.addTechnicalCheck(ServiceActivity.this, carId, techCheckAmount, techCheckDate);
                updateTechCheckInfo();  // Обновляем информацию о тех. осмотре на экране
            } else {
                Toast.makeText(ServiceActivity.this, "Пожалуйста, заполните все поля.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // Метод для обновления информации о тех. осмотре на экране
    private void updateTechCheckInfo() {
        // Форматируем дату окончания тех. осмотра (1 год)
        Calendar techCheckEndDate = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            techCheckEndDate.setTime(sdf.parse(techCheckDate));
            techCheckEndDate.add(Calendar.YEAR, 1);  // Добавляем 1 год к дате тех. осмотра

            // Вычисляем оставшиеся дни до окончания
            long currentTime = System.currentTimeMillis();
            long endTime = techCheckEndDate.getTimeInMillis();
            long diffInMillis = endTime - currentTime;
            long remainingDays = diffInMillis / (1000 * 60 * 60 * 24);

            // Отображаем информацию о тех. осмотре
            String techCheckInfo = "Стоимость тех. осмотра: " + techCheckAmount + " руб\n" +
                    "Дата тех. осмотра: " + techCheckDate + "\n" +
                    "До окончания срока: " + remainingDays + " дней";
            textViewTechCheckInfo.setText(techCheckInfo);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при расчете даты окончания тех. осмотра", Toast.LENGTH_SHORT).show();
        }
    }

    // Метод для загрузки информации о страховке
    private void loadInsuranceInfo() {
        // Получаем информацию из базы данных и обновляем UI
        List<Insurance> insuranceList = dbHelper.getInsuranceByCarId(carId);
        if (insuranceList != null && !insuranceList.isEmpty()) {
            Insurance insurance = insuranceList.get(0);  // Получаем первый элемент списка
            insuranceAmount = String.valueOf(insurance.getAmount());
            insuranceDate = insurance.getDate();
            insuranceDurationMonths = insurance.getDurationMonths();
            updateInsuranceInfo();  // Обновляем отображение страховки
        }
    }

    // Метод для обновления информации о страховке на экране
    private void updateInsuranceInfo() {
        // Форматируем дату окончания страховки
        Calendar insuranceEndDate = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            insuranceEndDate.setTime(sdf.parse(insuranceDate));
            insuranceEndDate.add(Calendar.MONTH, insuranceDurationMonths);  // Добавляем срок страховки

            // Вычисляем оставшиеся дни до окончания
            long currentTime = System.currentTimeMillis();
            long endTime = insuranceEndDate.getTimeInMillis();
            long diffInMillis = endTime - currentTime;
            long remainingDays = diffInMillis / (1000 * 60 * 60 * 24);

            // Отображаем информацию о страховке
            String insuranceInfo = "Сумма страховки: " + insuranceAmount + " руб\n" +
                    "Дата страховки: " + insuranceDate + "\n" +
                    "До окончания срока: " + remainingDays + " дней";
            textViewInsuranceInfo.setText(insuranceInfo);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при расчете даты окончания страховки", Toast.LENGTH_SHORT).show();
        }
    }

    // Метод для загрузки информации о тех. осмотре
    private void loadTechCheckInfo() {
        // Получаем информацию из базы данных и обновляем UI
        List<TechnicalCheck> techCheckList = dbHelper.getTechCheckByCarId(carId);
        if (techCheckList != null && !techCheckList.isEmpty()) {
            TechnicalCheck techCheck = techCheckList.get(0);  // Получаем первый элемент списка
            techCheckAmount = techCheck.getAmount();
            techCheckDate = techCheck.getDate();
            updateTechCheckInfo();  // Обновляем отображение тех. осмотра
        }
    }
}