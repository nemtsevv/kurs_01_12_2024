package com.example.kurs_01_12_2024;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "car_service_channel";
    private static final String CHANNEL_NAME = "Car Service Notifications";

    // Создаем канал уведомлений (для Android 8.0 и выше)
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Проверка на разрешение
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                CharSequence name = "Oil Change Notifications";
                String description = "Notifications about oil change needs";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);

                // Создаем канал уведомлений
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Отправка уведомления с уникальным ID для каждого автомобиля
    public static void sendNotification(Context context, Car car) {
        // Код для отправки уведомления
        Intent intent = new Intent(context, MainActivity.class);

        // Используем FLAG_IMMUTABLE для создания неизменяемого PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE);


        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Замена масла")
                .setContentText("Необходима замена масла для " + car.getMake() + " " + car.getModel())
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        // Безопасное отправление уведомления
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(car.getId(), notification);  // Используем ID автомобиля для уникальности уведомлений
        } catch (SecurityException e) {
            Log.e("NotificationHelper", "Permission denied: " + e.getMessage());
            // Можно запросить разрешение, если это необходимо
        }
    }
}
