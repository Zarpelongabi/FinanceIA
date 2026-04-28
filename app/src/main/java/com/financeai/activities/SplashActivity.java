package com.financeai.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.financeai.database.AppDatabase;
import com.financeai.models.Category;
import com.financeai.utils.PreferencesHelper;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar banco e categorias padrão
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            if (db.categoryDao().getAllCategoriesSync().isEmpty()) {
                for (Category c : Category.getDefaultCategories()) {
                    db.categoryDao().insert(c);
                }
            }
        });

        new Handler().postDelayed(() -> {
            if (PreferencesHelper.getUserName(this).equals("Usuário")) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            finish();
        }, 2000);
    }
}
