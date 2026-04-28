package com.financeai.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.financeai.R;
import com.financeai.activities.MainActivity;

public class NotificationHelper {

    private static final String CHANNEL_ID = "financeai_alerts";
    private static final String CHANNEL_NAME = "Alertas FinanceAI";
    private static final String CHANNEL_DESC = "Alertas inteligentes de gastos";
    private static int notificationId = 1000;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    public static void sendAlert(Context context, String emoji, String title, String message) {
        if (!PreferencesHelper.isNotificationsEnabled(context)) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(emoji + " " + title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        NotificationManager manager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId++, builder.build());
        }
    }

    public static void sendImpulsiveBuyingAlert(Context context, int count) {
        sendAlert(context, "⚡", "Gasto impulsivo!",
            String.format("Você já fez %d compras hoje. Faça uma pausa e avalie se todas são necessárias.", count));
    }

    public static void sendBudgetAlert(Context context, double percentage) {
        sendAlert(context, "🚨", "Limite de orçamento",
            String.format("Você já usou %.0f%% do orçamento mensal. Cuidado com os gastos!", percentage));
    }

    public static void sendWeeklyReport(Context context, double weekTotal, double comparison) {
        String compText = comparison > 0
            ? String.format("%.0f%% mais que semana passada", comparison)
            : String.format("%.0f%% menos que semana passada", Math.abs(comparison));

        sendAlert(context, "📊", "Resumo semanal",
            String.format("Você gastou R$ %.2f esta semana — %s.", weekTotal, compText));
    }

    public static void sendGoalAchievedAlert(Context context, String goalTitle) {
        sendAlert(context, "🏆", "Meta alcançada!",
            String.format("Parabéns! Você atingiu a meta: %s", goalTitle));
    }

    public static void sendEconomyTip(Context context, String tip) {
        sendAlert(context, "💡", "Dica de economia", tip);
    }
}