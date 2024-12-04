package com.example.kurs_01_12_2024;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CarDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "carDatabase.db";
    private static final int DATABASE_VERSION = 4; // Обновляем версию базы данных

    private static final String TABLE_CARS = "cars";
    private static final String COLUMN_CAR_ID = "car_id";
    private static final String COLUMN_CAR_MAKE = "make";
    private static final String COLUMN_CAR_MODEL = "model";
    private static final String COLUMN_CAR_MILEAGE = "mileage";
    private static final String COLUMN_OIL_CHANGE_MILEAGE = "oil_change_mileage";
    private static final String COLUMN_OIL_CHANGE_DATE = "oil_change_date";

    private static final String TABLE_EXPENSES = "expenses";
    private static final String COLUMN_EXPENSE_ID = "expense_id";
    private static final String COLUMN_EXPENSE_CAR_ID = "car_id";
    private static final String COLUMN_EXPENSE_AMOUNT = "amount";
    private static final String COLUMN_EXPENSE_TYPE = "type";
    private static final String COLUMN_EXPENSE_DATE = "date";
    private static final String COLUMN_EXPENSE_TERM_MONTHS = "term_months"; // Новый столбец для срока страховки

    private static final String CREATE_TABLE_CARS = "CREATE TABLE " + TABLE_CARS + " (" +
            COLUMN_CAR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CAR_MAKE + " TEXT NOT NULL, " +
            COLUMN_CAR_MODEL + " TEXT NOT NULL, " +
            COLUMN_CAR_MILEAGE + " INTEGER NOT NULL, " +
            COLUMN_OIL_CHANGE_MILEAGE + " INTEGER, " +
            COLUMN_OIL_CHANGE_DATE + " TEXT);";

    private static final String CREATE_TABLE_EXPENSES = "CREATE TABLE " + TABLE_EXPENSES + " (" +
            COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_EXPENSE_CAR_ID + " INTEGER NOT NULL, " +
            COLUMN_EXPENSE_AMOUNT + " REAL NOT NULL, " +
            COLUMN_EXPENSE_TYPE + " TEXT NOT NULL, " +
            COLUMN_EXPENSE_DATE + " TEXT NOT NULL, " +
            COLUMN_EXPENSE_TERM_MONTHS + " INTEGER, " + // Добавляем столбец term_months
            "FOREIGN KEY(" + COLUMN_EXPENSE_CAR_ID + ") REFERENCES " + TABLE_CARS + "(" + COLUMN_CAR_ID + "));";

    public CarDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Добавляем метод для обновления пробега автомобиля
    public void updateCarMileage(int carId, int newMileage) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CAR_MILEAGE, newMileage);

        int rowsUpdated = db.update(TABLE_CARS, values, COLUMN_CAR_ID + " = ?", new String[]{String.valueOf(carId)});
        if (rowsUpdated > 0) {
            Log.d("CarDatabaseHelper", "Пробег обновлен для автомобиля с ID: " + carId);
        } else {
            Log.e("CarDatabaseHelper", "Ошибка обновления пробега для автомобиля с ID: " + carId);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("CarDatabaseHelper", "Creating tables...");
        db.execSQL(CREATE_TABLE_CARS);
        db.execSQL(CREATE_TABLE_EXPENSES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("CarDatabaseHelper", "Upgrading database...");
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_CARS + " ADD COLUMN " + COLUMN_OIL_CHANGE_MILEAGE + " INTEGER;");
            db.execSQL("ALTER TABLE " + TABLE_CARS + " ADD COLUMN " + COLUMN_OIL_CHANGE_DATE + " TEXT;");
        }
        if (oldVersion < 4) {
            // Добавляем новый столбец term_months в таблицу расходов
            db.execSQL("ALTER TABLE " + TABLE_EXPENSES + " ADD COLUMN " + COLUMN_EXPENSE_TERM_MONTHS + " INTEGER;");
        }
    }



    // Метод для получения всех автомобилей
    public List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CARS, null, null, null, null, null, null);

        if (cursor != null) {
            int makeIndex = cursor.getColumnIndexOrThrow(COLUMN_CAR_MAKE);
            int modelIndex = cursor.getColumnIndexOrThrow(COLUMN_CAR_MODEL);
            int mileageIndex = cursor.getColumnIndexOrThrow(COLUMN_CAR_MILEAGE);
            int oilChangeMileageIndex = cursor.getColumnIndexOrThrow(COLUMN_OIL_CHANGE_MILEAGE);
            int oilChangeDateIndex = cursor.getColumnIndexOrThrow(COLUMN_OIL_CHANGE_DATE);
            int idIndex = cursor.getColumnIndexOrThrow(COLUMN_CAR_ID);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(idIndex);
                String make = cursor.getString(makeIndex);
                String model = cursor.getString(modelIndex);
                int mileage = cursor.getInt(mileageIndex);
                Integer oilChangeMileage = cursor.isNull(oilChangeMileageIndex) ? null : cursor.getInt(oilChangeMileageIndex);
                String oilChangeDate = cursor.isNull(oilChangeDateIndex) ? null : cursor.getString(oilChangeDateIndex);

                cars.add(new Car(id, make, model, mileage, oilChangeMileage, oilChangeDate));
            }
            cursor.close();
        }
        return cars;
    }

    // Метод для добавления записи о страховке
    public void addInsurance(Context context, int carId, double amount, String type, String date, int termMonths) {
        // Проверяем корректность значений срока страховки
        if (termMonths != 6 && termMonths != 12) {
            Toast.makeText(context, "Срок страховки должен быть 6 или 12 месяцев.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Добавляем запись в базу данных
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXPENSE_CAR_ID, carId);
        values.put(COLUMN_EXPENSE_AMOUNT, amount);
        values.put(COLUMN_EXPENSE_TYPE, type);
        values.put(COLUMN_EXPENSE_DATE, date);
        values.put(COLUMN_EXPENSE_TERM_MONTHS, termMonths);  // Сохраняем срок страховки

        long newRowId = db.insert(TABLE_EXPENSES, null, values);

        if (newRowId == -1) {
            Toast.makeText(context, "Ошибка при добавлении страховки", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Страховка добавлена", Toast.LENGTH_SHORT).show();
        }
    }

    // Метод для добавления записи о тех. осмотре
    public void addTechnicalCheck(Context context, int carId, double amount, String date) {
        // Проверяем корректность значения стоимости
        if (amount <= 0) {
            Toast.makeText(context, "Стоимость тех. осмотра должна быть больше нуля.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Добавляем запись в базу данных
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXPENSE_CAR_ID, carId);
        values.put(COLUMN_EXPENSE_AMOUNT, amount);
        values.put(COLUMN_EXPENSE_TYPE, "Тех. осмотр");  // Указываем тип как тех. осмотр
        values.put(COLUMN_EXPENSE_DATE, date);  // Дата тех. осмотра
        values.put(COLUMN_EXPENSE_TERM_MONTHS, 12);  // Стандартный срок тех. осмотра - 1 год

        long newRowId = db.insert(TABLE_EXPENSES, null, values);

        if (newRowId == -1) {
            Toast.makeText(context, "Ошибка при добавлении записи о тех. осмотре", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Тех. осмотр добавлен", Toast.LENGTH_SHORT).show();
        }
    }

    // Метод для получения расходов по carId (например, для получения страховки или тех. осмотра)
    public List<Expense> getExpensesByCarId(int carId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXPENSES, null, COLUMN_EXPENSE_CAR_ID + " = ?",
                new String[]{String.valueOf(carId)}, null, null, null);

        if (cursor != null) {
            int amountIndex = cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_AMOUNT);
            int typeIndex = cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_TYPE);
            int dateIndex = cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_DATE);
            int termMonthsIndex = cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_TERM_MONTHS);

            while (cursor.moveToNext()) {
                double amount = cursor.getDouble(amountIndex);
                String type = cursor.getString(typeIndex);
                String date = cursor.getString(dateIndex);
                int termMonths = cursor.getInt(termMonthsIndex);

                expenses.add(new Expense(amount, type, date, termMonths));
            }
            cursor.close();
        }
        return expenses;
    }

    // Метод для получения страховки по carId
    public List<Insurance> getInsuranceByCarId(int carId) {
        List<Insurance> insuranceList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXPENSES, null, COLUMN_EXPENSE_CAR_ID + " = ? AND " + COLUMN_EXPENSE_TYPE + " = ?",
                new String[]{String.valueOf(carId), "Страховка"}, null, null, null);

        if (cursor != null) {
            int amountIndex = cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_AMOUNT);
            int dateIndex = cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_DATE);
            int termMonthsIndex = cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_TERM_MONTHS);

            while (cursor.moveToNext()) {
                double amount = cursor.getDouble(amountIndex);
                String date = cursor.getString(dateIndex);
                int termMonths = cursor.getInt(termMonthsIndex);

                insuranceList.add(new Insurance(amount, date, termMonths));
            }
            cursor.close();
        }
        return insuranceList;
    }

    // Метод для получения тех. осмотра по carId
    public List<TechnicalCheck> getTechCheckByCarId(int carId) {
        List<TechnicalCheck> techCheckList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXPENSES, null, COLUMN_EXPENSE_CAR_ID + " = ? AND " + COLUMN_EXPENSE_TYPE + " = ?",
                new String[]{String.valueOf(carId), "Тех. осмотр"}, null, null, null);

        if (cursor != null) {
            int amountIndex = cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_AMOUNT);
            int dateIndex = cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_DATE);

            while (cursor.moveToNext()) {
                double amount = cursor.getDouble(amountIndex);
                String date = cursor.getString(dateIndex);

                techCheckList.add(new TechnicalCheck(amount, date));
            }
            cursor.close();
        }
        return techCheckList;
    }

    // Метод для преобразования даты в формат yyyy-MM-dd
    public String formatDateForDatabase(String date) {
        try {
            SimpleDateFormat[] inputFormats = {
                    new SimpleDateFormat("d/M/yyyy", Locale.getDefault()),
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                    new SimpleDateFormat("M/d/yyyy", Locale.getDefault()),
                    new SimpleDateFormat("MM/d/yyyy", Locale.getDefault())
            };

            for (SimpleDateFormat format : inputFormats) {
                try {
                    Date parsedDate = format.parse(date);
                    if (parsedDate != null) {
                        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        return outputFormat.format(parsedDate);
                    }
                } catch (ParseException e) {
                    // Пробуем следующий формат
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Добавляем данные о замене масла в базу данных
    public void addOilChange(Context context, int carId, int mileage, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Обновляем только существующие данные для замены масла
        values.put("oil_change_mileage", mileage); // Пробег для замены масла
        values.put("oil_change_date", date); // Дата замены масла

        try {
            // Обновляем запись в таблице Cars по carId
            int rowsUpdated = db.update("Cars", values, "car_id = ?", new String[]{String.valueOf(carId)});

            if (rowsUpdated > 0) {
                Log.d("CarDatabaseHelper", "Oil change data updated for car_id: " + carId);
            } else {
                Log.e("CarDatabaseHelper", "Failed to update oil change data for car_id: " + carId);
                Toast.makeText(context, "Ошибка при обновлении данных замены масла", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("CarDatabaseHelper", "Error updating oil change", e);
            Toast.makeText(context, "Ошибка при обновлении данных замены масла", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }






    // Метод для преобразования даты из формата yyyy-MM-dd в формат, читаемый человеком (например, dd/MM/yyyy)
    public String formatDateForDisplay(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("d/MM/yyyy", Locale.getDefault());

            Date parsedDate = inputFormat.parse(date);
            if (parsedDate != null) {
                return outputFormat.format(parsedDate); // Возвращаем строку в формате dd/MM/yyyy
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;  // Если ошибка при форматировании, возвращаем оригинальную дату
    }

    public void deleteCar(int carId) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_EXPENSES, COLUMN_EXPENSE_CAR_ID + " = ?", new String[]{String.valueOf(carId)});
        db.delete(TABLE_CARS, COLUMN_CAR_ID + " = ?", new String[]{String.valueOf(carId)});
    }
}
