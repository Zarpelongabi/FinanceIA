package com.financeai.utils;

import android.content.Context;
import com.financeai.database.AppDatabase;
import com.financeai.database.dao.TransactionDao;
import com.financeai.models.Transaction;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SpendingAnalyzer {

    public static class Alert {
        public enum Type { WARNING, DANGER, SUCCESS, INFO }
        public Type type;
        public String title;
        public String message;
        public String emoji;

        public Alert(Type type, String emoji, String title, String message) {
            this.type = type;
            this.emoji = emoji;
            this.title = title;
            this.message = message;
        }
    }

    public static class Forecast {
        public double projectedMonthlyTotal;
        public double remainingBudget;
        public double dailyBudgetRemaining;
        public int daysRemainingInMonth;
        public String recommendation;
    }

    private final Context context;
    private final AppDatabase db;

    public SpendingAnalyzer(Context context) {
        this.context = context;
        this.db = AppDatabase.getInstance(context);
    }

    // Gera alertas automáticos baseados no comportamento
    public List<Alert> generateAlerts() {
        List<Alert> alerts = new ArrayList<>();

        long[] today = getDayRange();
        long[] thisWeek = getWeekRange();
        long[] thisMonth = getMonthRange();
        long[] lastWeek = getLastWeekRange();

        double todayTotal = db.transactionDao().getTotalExpenseByPeriod(today[0], today[1]);
        double weekTotal = db.transactionDao().getTotalRealExpenseByPeriod(thisWeek[0], thisWeek[1]);
        double lastWeekTotal = db.transactionDao().getTotalRealExpenseByPeriod(lastWeek[0], lastWeek[1]);
        
        // Gastos reais (sem investimentos)
        double monthRealTotal = db.transactionDao().getTotalRealExpenseByPeriod(thisMonth[0], thisMonth[1]);
        // Total incluindo investimentos
        double monthFullTotal = db.transactionDao().getTotalExpenseByPeriod(thisMonth[0], thisMonth[1]);
        // Só investimentos
        double monthInvestments = monthFullTotal - monthRealTotal;

        int todayCount = db.transactionDao().getExpenseCountByPeriod(today[0], today[1]);

        // Inteligência Financeira: Elogio para Aportes
        if (monthInvestments > 0) {
            if (monthInvestments > monthRealTotal * 0.3) {
                alerts.add(new Alert(Alert.Type.SUCCESS, "💎",
                    "Mentalidade Milionária!",
                    "Você está investindo mais de 30% do que gasta. Seu eu do futuro agradece!"));
            } else {
                alerts.add(new Alert(Alert.Type.INFO, "🌱",
                    "Semente plantada",
                    "Bom trabalho ao separar dinheiro para o futuro. Continue assim!"));
            }
        }

        // Alerta de gasto impulsivo (muitos gastos em 1 dia)
        if (todayCount >= 4) {
            alerts.add(new Alert(Alert.Type.DANGER, "⚡",
                "Gasto impulsivo detectado!",
                String.format("Você fez %d compras hoje. Tome cuidado!", todayCount)));
        }

        // Comparação com semana anterior
        if (lastWeekTotal > 0 && weekTotal > lastWeekTotal * 1.2) {
            double pct = ((weekTotal - lastWeekTotal) / lastWeekTotal) * 100;
            alerts.add(new Alert(Alert.Type.WARNING, "📊",
                "Gasto acima do normal",
                String.format("Você gastou %.0f%% mais que na semana passada.", pct)));
        } else if (lastWeekTotal > 0 && weekTotal < lastWeekTotal * 0.8) {
            double pct = ((lastWeekTotal - weekTotal) / lastWeekTotal) * 100;
            alerts.add(new Alert(Alert.Type.SUCCESS, "🎉",
                "Ótimo controle!",
                String.format("Você gastou %.0f%% menos que na semana passada!", pct)));
        }

        // Alerta de limite mensal (Usa apenas o gasto real para não "punir" o investimento)
        double monthlyBudget = PreferencesHelper.getMonthlyBudget(context);
        if (monthlyBudget > 0) {
            double percentUsed = (monthRealTotal / monthlyBudget) * 100;
            if (percentUsed >= 90) {
                alerts.add(new Alert(Alert.Type.DANGER, "🚨",
                    "Limite quase atingido!",
                    String.format("Seus gastos reais ocupam %.0f%% do seu orçamento.", percentUsed)));
            } else if (percentUsed >= 75) {
                alerts.add(new Alert(Alert.Type.WARNING, "⚠️",
                    "Atenção ao orçamento",
                    String.format("%.0f%% do orçamento já foi usado em gastos fixos/variáveis.", percentUsed)));
            }
        }

        return alerts;
    }

    // Previsão de gastos para o fim do mês
    public Forecast generateForecast() {
        Forecast forecast = new Forecast();

        long[] thisMonth = getMonthRange();
        Calendar now = Calendar.getInstance();

        double monthTotal = db.transactionDao().getTotalRealExpenseByPeriod(thisMonth[0], thisMonth[1]);

        // Dias passados e restantes no mês
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
        int totalDays = now.getActualMaximum(Calendar.DAY_OF_MONTH);
        forecast.daysRemainingInMonth = totalDays - dayOfMonth;

        // Média diária atual
        double dailyAverage = dayOfMonth > 0 ? monthTotal / dayOfMonth : 0;

        // Projeção para o mês completo
        forecast.projectedMonthlyTotal = dailyAverage * totalDays;

        // Orçamento restante
        double budget = PreferencesHelper.getMonthlyBudget(context);
        forecast.remainingBudget = budget > 0 ? budget - monthTotal : 0;
        forecast.dailyBudgetRemaining = forecast.daysRemainingInMonth > 0
            ? forecast.remainingBudget / forecast.daysRemainingInMonth : 0;

        // Recomendação
        if (budget > 0 && forecast.projectedMonthlyTotal > budget) {
            forecast.recommendation = String.format(
                "Se continuar assim, vai ultrapassar o limite em R$ %.2f",
                forecast.projectedMonthlyTotal - budget);
        } else if (budget > 0) {
            forecast.recommendation = String.format(
                "Ótimo! Você pode gastar R$ %.2f por dia até o fim do mês.",
                forecast.dailyBudgetRemaining);
        }

        return forecast;
    }

    // Helpers para ranges de tempo
    public static long[] getDayRange() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);

        return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
    }

    public static long[] getWeekRange() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        return new long[]{start.getTimeInMillis(), System.currentTimeMillis()};
    }

    public static long[] getLastWeekRange() {
        Calendar start = Calendar.getInstance();
        start.add(Calendar.WEEK_OF_YEAR, -1);
        start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());
        start.set(Calendar.HOUR_OF_DAY, 0);

        Calendar end = Calendar.getInstance();
        end.add(Calendar.WEEK_OF_YEAR, -1);
        end.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        end.set(Calendar.HOUR_OF_DAY, 23);

        return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
    }

    public static long[] getMonthRange() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        return new long[]{start.getTimeInMillis(), System.currentTimeMillis()};
    }
}