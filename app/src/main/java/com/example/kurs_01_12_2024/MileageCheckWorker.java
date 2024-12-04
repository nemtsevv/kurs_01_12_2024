package com.example.kurs_01_12_2024;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MileageCheckWorker extends Worker {

    private CarDatabaseHelper dbHelper;

    public MileageCheckWorker(Context context, WorkerParameters workerParams) {
        super(context, workerParams);
        dbHelper = new CarDatabaseHelper(context);
    }

    @Override
    public Result doWork() {
        checkCarsMileage();
        return Result.success();
    }

    private void checkCarsMileage() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                "cars",
                new String[]{"car_id", "make", "model", "mileage", "oil_change_mileage", "oil_change_date"},
                null, null, null, null, null
        );

        if (cursor != null) {
            try {
                int idIndex = cursor.getColumnIndexOrThrow("car_id");
                int makeIndex = cursor.getColumnIndexOrThrow("make");
                int modelIndex = cursor.getColumnIndexOrThrow("model");
                int mileageIndex = cursor.getColumnIndexOrThrow("mileage");
                int nextOilChangeMileageIndex = cursor.getColumnIndexOrThrow("oil_change_mileage");
                int oilChangeDateIndex = cursor.getColumnIndexOrThrow("oil_change_date");

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idIndex);
                    String make = cursor.getString(makeIndex);
                    String model = cursor.getString(modelIndex);
                    int mileage = cursor.getInt(mileageIndex);
                    Integer nextOilChangeMileage = cursor.isNull(nextOilChangeMileageIndex) ? null : cursor.getInt(nextOilChangeMileageIndex);
                    String oilChangeDate = cursor.getString(oilChangeDateIndex);

                    // Логируем данные автомобиля и условия для уведомления
                    Log.d("MileageCheckWorker", "Car ID: " + id + ", Make: " + make + ", Model: " + model
                            + ", Mileage: " + mileage + ", Next Oil Change Mileage: " + nextOilChangeMileage
                            + ", Oil Change Date: " + oilChangeDate);

                    // Проверка пробега
                    if (nextOilChangeMileage != null && nextOilChangeMileage - mileage < 1000) {
                        // Логируем, если условие выполнено
                        Log.d("MileageCheckWorker", "Sending notification for car: " + make + " " + model);

                        // Создаем объект Car для передачи в NotificationHelper
                        Car car = new Car(id, make, model, mileage, nextOilChangeMileage, oilChangeDate);

                        // Отправка уведомления
                        NotificationHelper.sendNotification(getApplicationContext(), car);
                    }
                }
            } catch (Exception e) {
                Log.e("MileageCheckWorker", "Error checking mileage: ", e);
            } finally {
                cursor.close();
            }
        }
    }
}
