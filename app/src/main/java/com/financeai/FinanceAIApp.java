package com.financeai;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.google.firebase.FirebaseApp;

public class FinanceAIApp extends Application {
    public static final String CHANNEL_ID = "finance_alerts";

    @Override
    public void onCreate() {
        super.onCreate();
        // FirebaseApp.initializeApp(this); // Desativado até ter o google-services.json
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Finance Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for budget goals and spending alerts");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
