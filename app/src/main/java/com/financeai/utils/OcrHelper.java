package com.financeai.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import com.financeai.database.AppDatabase;
import com.financeai.models.Category;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrHelper {

    private static final String TAG = "OcrHelper";

    // Resultado do OCR extraído
    public static class OcrResult {
        public String rawText;
        public double amount;
        public String establishment;
        public String date;
        public String suggestedCategory;
        public String suggestedCategoryEmoji;
        public boolean success;
    }

    public interface OcrCallback {
        void onSuccess(OcrResult result);
        void onFailure(String error);
    }

    // Processa imagem com ML Kit
    public static void processImage(Context context, Bitmap bitmap, OcrCallback callback) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        recognizer.process(image)
            .addOnSuccessListener(visionText -> {
                new Thread(() -> {
                    OcrResult result = parseReceipt(context, visionText.getText());
                    callback.onSuccess(result);
                }).start();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "OCR failed", e);
                callback.onFailure(e.getMessage());
            });
    }

    // Extrai informações relevantes do texto OCR
    private static OcrResult parseReceipt(Context context, String rawText) {
        OcrResult result = new OcrResult();
        result.rawText = rawText;
        result.success = true;

        String text = rawText.toLowerCase();
        String[] lines = rawText.split("\n");

        // 1. Extrai valor (procura padrões como R$ XX,XX ou XX.XX)
        result.amount = extractAmount(rawText);

        // 2. Extrai estabelecimento (geralmente nas primeiras linhas)
        result.establishment = extractEstablishment(lines);

        // 3. Extrai data
        result.date = extractDate(rawText);

        // 4. Sugere categoria automaticamente
        suggestCategorySync(context, result, text);

        return result;
    }

    private static double extractAmount(String text) {
        // Padrões: R$ 1.234,56 | R$1234.56 | TOTAL: 45,90
        String[] patterns = {
            "R\\$\\s*([\\d.,]+)",
            "TOTAL[:\\s]+R?\\$?\\s*([\\d.,]+)",
            "VALOR[:\\s]+R?\\$?\\s*([\\d.,]+)",
            "(?:^|\\s)([\\d]{1,4}[,.]\\d{2})(?:\\s|$)"
        };

        double maxAmount = 0;
        for (String pattern : patterns) {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            while (m.find()) {
                try {
                    String numStr = m.group(1)
                        .replace(".", "")
                        .replace(",", ".");
                    double val = Double.parseDouble(numStr);
                    if (val > maxAmount) maxAmount = val;
                } catch (Exception ignored) {}
            }
        }
        return maxAmount;
    }

    private static String extractEstablishment(String[] lines) {
        // Geralmente é uma das primeiras linhas não vazias
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            String line = lines[i].trim();
            if (line.length() > 3 && !line.matches(".*\\d{2}/\\d{2}/\\d{4}.*")
                    && !line.matches(".*R\\$.*")
                    && !line.toLowerCase().contains("cnpj")
                    && !line.toLowerCase().contains("cpf")) {
                // Capitaliza nome do estabelecimento
                return capitalize(line);
            }
        }
        return "Estabelecimento";
    }

    private static String extractDate(String text) {
        // Padrões de data: DD/MM/YYYY, DD-MM-YYYY, DD/MM/YY
        Pattern[] datePatterns = {
            Pattern.compile("(\\d{2})[/-](\\d{2})[/-](\\d{4})"),
            Pattern.compile("(\\d{2})[/-](\\d{2})[/-](\\d{2})")
        };

        for (Pattern p : datePatterns) {
            Matcher m = p.matcher(text);
            if (m.find()) {
                String year = m.group(3);
                if (year.length() == 2) year = "20" + year;
                return m.group(1) + "/" + m.group(2) + "/" + year;
            }
        }

        // Se não encontrar, usa data atual
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }

    private static void suggestCategorySync(Context context, OcrResult result, String text) {
        // Busca no banco de dados por palavras-chave
        AppDatabase db = AppDatabase.getInstance(context);
        
        Category match = db.categoryDao().findCategoryByKeyword(
            result.establishment != null ? result.establishment.toLowerCase() : ""
        );

        if (match != null) {
            result.suggestedCategory = match.getName();
            result.suggestedCategoryEmoji = match.getEmoji();
            return;
        }

        // Fallback: regras embutidas
        if (text.contains("mcdonalds") || text.contains("burger") || text.contains("ifood")
                || text.contains("mercado") || text.contains("supermercado")) {
            result.suggestedCategory = "Alimentação";
            result.suggestedCategoryEmoji = "🍔";
        } else if (text.contains("uber") || text.contains("99") || text.contains("passagem")
                || text.contains("combustivel") || text.contains("posto")) {
            result.suggestedCategory = "Transporte";
            result.suggestedCategoryEmoji = "🚌";
        } else if (text.contains("cinema") || text.contains("netflix") || text.contains("shopping")) {
            result.suggestedCategory = "Lazer";
            result.suggestedCategoryEmoji = "🎬";
        } else if (text.contains("energia") || text.contains("agua") || text.contains("internet")
                || text.contains("telefone") || text.contains("aluguel")) {
            result.suggestedCategory = "Contas";
            result.suggestedCategoryEmoji = "⚡";
        } else if (text.contains("farmacia") || text.contains("hospital") || text.contains("medico")) {
            result.suggestedCategory = "Saúde";
            result.suggestedCategoryEmoji = "💊";
        } else {
            result.suggestedCategory = "Outros";
            result.suggestedCategoryEmoji = "📦";
        }
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        String[] words = text.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}