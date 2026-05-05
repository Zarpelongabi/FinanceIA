package com.financeai.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DateHelper {
    
    public static String getFifthWorkingDay() {
        Calendar fifthDay = getFifthWorkingDayCalendar(Calendar.getInstance());
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(fifthDay.getTime());
    }

    public static Calendar getFifthWorkingDayCalendar(Calendar reference) {
        Calendar cal = (Calendar) reference.clone();
        
        // Tenta calcular para o mês da referência
        Calendar fifthDay = calculateFifthWorkingDay(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
        
        // Se a referência já passou do quinto dia útil deste mês, calcula para o próximo mês
        if (reference.after(fifthDay)) {
            cal.add(Calendar.MONTH, 1);
            fifthDay = calculateFifthWorkingDay(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
        }
        
        return fifthDay;
    }

    public static boolean isTodayFifthWorkingDay() {
        Calendar today = Calendar.getInstance();
        Calendar fifthDay = calculateFifthWorkingDay(today.get(Calendar.YEAR), today.get(Calendar.MONTH));
        
        return today.get(Calendar.YEAR) == fifthDay.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == fifthDay.get(Calendar.DAY_OF_YEAR);
    }

    private static Calendar calculateFifthWorkingDay(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        List<String> holidays = getHolidays(year);
        int workingDays = 0;
        
        while (workingDays < 5) {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            java.text.SimpleDateFormat sdfCheck = new java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault());
            String currentDayMonth = sdfCheck.format(cal.getTime());
            
            boolean isHoliday = holidays.contains(currentDayMonth);
            
            // Sábado agora conta como dia útil (legislação trabalhista para 5º dia útil)
            if (dayOfWeek != Calendar.SUNDAY && !isHoliday) {
                workingDays++;
            }
            if (workingDays < 5) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        return cal;
    }

    private static List<String> getHolidays(int year) {
        List<String> holidays = new ArrayList<>();
        holidays.add("01/01"); // Confraternização Universal
        holidays.add("21/04"); // Tiradentes
        holidays.add("01/05"); // Dia do Trabalho
        holidays.add("07/09"); // Independência
        holidays.add("12/10"); // Nsa. Sra. Aparecida
        holidays.add("02/11"); // Finados
        holidays.add("15/11"); // Proclamação da República
        holidays.add("20/11"); // Consciência Negra
        holidays.add("25/12"); // Natal
        
        // Feriados móveis (Cálculo simplificado da Páscoa para Carnaval e Corpus Christi)
        // Para um app real, poderíamos usar uma API, mas aqui calculamos os principais
        addVariableHolidays(holidays, year);
        
        return holidays;
    }

    private static void addVariableHolidays(List<String> holidays, int year) {
        // Cálculo da Páscoa (Algoritmo de Meeus/Jones/Butcher)
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;

        Calendar pascoa = Calendar.getInstance();
        pascoa.set(year, month - 1, day);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault());

        // Carnaval (47 dias antes da Páscoa)
        Calendar carnaval = (Calendar) pascoa.clone();
        carnaval.add(Calendar.DAY_OF_YEAR, -47);
        holidays.add(sdf.format(carnaval.getTime()));

        // Sexta-feira Santa (2 dias antes da Páscoa)
        Calendar sextaSanta = (Calendar) pascoa.clone();
        sextaSanta.add(Calendar.DAY_OF_YEAR, -2);
        holidays.add(sdf.format(sextaSanta.getTime()));

        // Corpus Christi (60 dias após a Páscoa)
        Calendar corpus = (Calendar) pascoa.clone();
        corpus.add(Calendar.DAY_OF_YEAR, 60);
        holidays.add(sdf.format(corpus.getTime()));
    }

    public static String getMonthName(int month) {
        String[] months = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        return months[month];
    }
}
