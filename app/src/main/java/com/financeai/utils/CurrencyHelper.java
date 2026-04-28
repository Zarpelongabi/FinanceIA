package com.financeai.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyHelper {
    public static String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return formatter.format(amount);
    }

    public static String format(double amount) {
        return formatCurrency(amount);
    }

    public static String formatCurrency(double amount, String currencyCode) {
        return String.format("%s %.2f", currencyCode, amount);
    }
}
