package com.financeai.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.financeai.R;
import com.financeai.activities.MainActivity;

/**
 * Trabalhador responsável por disparar a notificação do 5º dia útil.
 */
public class NotificacaoTrabalhador extends Worker {

    private static final String CHANNEL_ID = "FINANCEIRO_IA_ALERTA";

    public NotificacaoTrabalhador(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        dispararNotificacao();
        return Result.success();
    }

    private void dispararNotificacao() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Alertas FinanceiroIA", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notificações de lembrete de salário e 5º dia útil");
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("O salário está chegando! 💰")
                .setContentText("Amanhã é o 5º dia útil. Já planejou suas metas para este mês?")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(1, builder.build());
    }
}
