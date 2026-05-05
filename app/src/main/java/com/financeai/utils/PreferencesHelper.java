package com.financeai.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {

    private static final String PREFS_NAME = "vortex_prefs";
    private static final String KEY_MONTHLY_BUDGET = "monthly_budget";
    private static final String KEY_ECONOMY_MODE = "economy_mode";
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_SCORE = "user_score";
    private static final String KEY_FIRST_RUN = "first_run";
    private static final String KEY_BALANCE = "current_balance";
    private static final String KEY_PRIMARY_COLOR = "primary_color";
    private static final String KEY_HIDE_BALANCE = "hide_balance";
    private static final String KEY_SALARY = "salary";
    private static final String KEY_LAST_SALARY_MONTH = "last_salary_month";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static double getMonthlyBudget(Context context) {
        return Double.longBitsToDouble(getPrefs(context).getLong(KEY_MONTHLY_BUDGET,
            Double.doubleToLongBits(3000.0)));
    }

    public static void setMonthlyBudget(Context context, double budget) {
        getPrefs(context).edit()
            .putLong(KEY_MONTHLY_BUDGET, Double.doubleToLongBits(budget))
            .apply();
    }

    public static boolean isEconomyMode(Context context) {
        return getPrefs(context).getBoolean(KEY_ECONOMY_MODE, false);
    }

    public static void setEconomyMode(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_ECONOMY_MODE, enabled).apply();
    }

    public static boolean isDarkTheme(Context context) {
        return getPrefs(context).getBoolean(KEY_DARK_THEME, false);
    }

    public static void setDarkTheme(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_DARK_THEME, enabled).apply();
    }

    public static String getUserName(Context context) {
        return getPrefs(context).getString(KEY_USER_NAME, "Usuário");
    }

    public static void setUserName(Context context, String name) {
        getPrefs(context).edit().putString(KEY_USER_NAME, name).apply();
    }

    public static boolean isNotificationsEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public static void setNotificationsEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public static int getUserScore(Context context) {
        return getPrefs(context).getInt(KEY_SCORE, 0);
    }

    public static void addScore(Context context, int points) {
        int current = getUserScore(context);
        getPrefs(context).edit().putInt(KEY_SCORE, current + points).apply();
    }

    public static boolean isFirstRun(Context context) {
        return getPrefs(context).getBoolean(KEY_FIRST_RUN, true);
    }

    public static void setFirstRunDone(Context context) {
        getPrefs(context).edit().putBoolean(KEY_FIRST_RUN, false).apply();
    }

    public static double getCurrentBalance(Context context) {
        return Double.longBitsToDouble(getPrefs(context).getLong(KEY_BALANCE,
            Double.doubleToLongBits(0.0)));
    }

    public static void setCurrentBalance(Context context, double balance) {
        getPrefs(context).edit()
            .putLong(KEY_BALANCE, Double.doubleToLongBits(balance))
            .apply();
    }

    public static int getPrimaryColor(Context context) {
        return getPrefs(context).getInt(KEY_PRIMARY_COLOR, context.getResources().getColor(com.financeai.R.color.primary_eco));
    }

    public static void setPrimaryColor(Context context, int color) {
        getPrefs(context).edit().putInt(KEY_PRIMARY_COLOR, color).apply();
    }

    public static boolean isHideBalance(Context context) {
        return getPrefs(context).getBoolean(KEY_HIDE_BALANCE, false);
    }

    public static void setHideBalance(Context context, boolean hide) {
        getPrefs(context).edit().putBoolean(KEY_HIDE_BALANCE, hide).apply();
    }

    public static double getSalary(Context context) {
        return (double) getPrefs(context).getFloat(KEY_SALARY, 0f);
    }

    public static void setSalary(Context context, double salary) {
        getPrefs(context).edit()
            .putFloat(KEY_SALARY, (float) salary)
            .apply();
    }

    public static String getLastSalaryMonth(Context context) {
        return getPrefs(context).getString(KEY_LAST_SALARY_MONTH, "");
    }

    public static void setLastSalaryMonth(Context context, String monthYear) {
        getPrefs(context).edit().putString(KEY_LAST_SALARY_MONTH, monthYear).apply();
    }

    // Retorna uma versão mais clara da cor primária para gradientes e destaques
    public static int getPrimaryLightColor(Context context) {
        int color = getPrimaryColor(context);
        return adjustAlpha(color, 0.7f); // 70% de opacidade ou clareamento
    }

    // Retorna uma versão mais escura da cor primária para status bar
    public static int getPrimaryDarkColor(Context context) {
        float[] hsv = new float[3];
        android.graphics.Color.colorToHSV(getPrimaryColor(context), hsv);
        hsv[2] *= 0.7f; // Reduz o brilho em 30%
        return android.graphics.Color.HSVToColor(hsv);
    }

    // Retorna uma cor de superfície (fundo de card) baseada na cor primária (bem escura)
    public static int getSurfaceColor(Context context) {
        float[] hsv = new float[3];
        android.graphics.Color.colorToHSV(getPrimaryColor(context), hsv);
        hsv[1] *= 0.4f; // Reduz a saturação
        hsv[2] = 0.12f; // Brilho bem baixo para ser um fundo escuro
        return android.graphics.Color.HSVToColor(hsv);
    }

    private static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(android.graphics.Color.alpha(color) * factor);
        int red = android.graphics.Color.red(color);
        int green = android.graphics.Color.green(color);
        int blue = android.graphics.Color.blue(color);
        return android.graphics.Color.argb(alpha, red, green, blue);
    }
}