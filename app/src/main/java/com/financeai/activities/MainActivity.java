package com.financeai.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.financeai.R;
import com.financeai.adapters.MainViewPagerAdapter;
import com.financeai.database.AppDatabase;
import com.financeai.models.Transaction;
import com.financeai.utils.DateHelper;
import com.financeai.utils.PreferencesHelper;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private android.widget.ImageButton btnNavHome, btnNavSummary, btnNavHistory, btnNavGoals, btnNavPredicted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyCustomColor();
        
        super.onCreate(savedInstanceState);
        
        if (PreferencesHelper.isDarkTheme(this)) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_main);

        initViews();
        setupViewPager();
        setupMenu();
        checkAndDepositSalary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndDepositSalary();
        generateFixedExpensesPredictions();
    }

    private void generateFixedExpensesPredictions() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Calendar cal = Calendar.getInstance();
            long[] monthRange = com.financeai.utils.SpendingAnalyzer.getMonthRange();
            
            // Busca gastos recorrentes (isRecurring = 1)
            List<Transaction> recurringBase = db.transactionDao().getRecurringTransactionsBase();
            
            for (Transaction base : recurringBase) {
                // Verifica se já existe uma versão "Predicted" ou "Real" para este mês e título
                boolean exists = db.transactionDao().existsForMonth(base.getTitle(), monthRange[0], monthRange[1]);
                
                if (!exists) {
                    Transaction prediction = new Transaction();
                    prediction.setTitle(base.getTitle());
                    prediction.setAmount(base.getAmount());
                    prediction.setCategoryName(base.getCategoryName());
                    prediction.setCategoryId(base.getCategoryId());
                    prediction.setExpense(true);
                    prediction.setPredicted(true);
                    prediction.setRecurring(true);
                    // Coloca na data atual ou início do mês para aparecer no previsto
                    prediction.setDate(System.currentTimeMillis());
                    
                    db.transactionDao().insert(prediction);
                }
            }
        });
    }

    private void checkAndDepositSalary() {
        if (DateHelper.isTodayFifthWorkingDay()) {
            Calendar cal = Calendar.getInstance();
            String currentMonthYear = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
            String lastDeposited = PreferencesHelper.getLastSalaryMonth(this);

            if (!currentMonthYear.equals(lastDeposited)) {
                double salary = PreferencesHelper.getSalary(this);
                if (salary > 0) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase db = AppDatabase.getInstance(this);
                        Transaction incomeTx = new Transaction();
                        incomeTx.setTitle("Salário Automático");
                        incomeTx.setAmount(salary);
                        incomeTx.setCategoryName("Salário");
                        incomeTx.setDate(System.currentTimeMillis());
                        incomeTx.setExpense(false);
                        db.transactionDao().insert(incomeTx);
                        
                        PreferencesHelper.setLastSalaryMonth(this, currentMonthYear);
                    });
                }
            }
        }
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        btnNavHome = findViewById(R.id.btn_nav_home);
        btnNavSummary = findViewById(R.id.btn_nav_summary);
        btnNavHistory = findViewById(R.id.btn_nav_history);
        btnNavGoals = findViewById(R.id.btn_nav_goals);
        btnNavPredicted = findViewById(R.id.btn_nav_predicted);
    }

    private void setupViewPager() {
        MainViewPagerAdapter adapter = new MainViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateMenuHighlight(position);
            }
        });
    }

    private void setupMenu() {
        btnNavHome.setOnClickListener(v -> viewPager.setCurrentItem(0));
        btnNavSummary.setOnClickListener(v -> viewPager.setCurrentItem(1));
        btnNavHistory.setOnClickListener(v -> viewPager.setCurrentItem(2));
        btnNavGoals.setOnClickListener(v -> viewPager.setCurrentItem(3));
        btnNavPredicted.setOnClickListener(v -> viewPager.setCurrentItem(4));
    }

    private void updateMenuHighlight(int position) {
        int primaryColor = PreferencesHelper.getPrimaryColor(this);
        
        btnNavHome.setAlpha(0.6f);
        btnNavSummary.setAlpha(0.6f);
        btnNavHistory.setAlpha(0.6f);
        btnNavGoals.setAlpha(0.6f);
        btnNavPredicted.setAlpha(0.6f);

        btnNavHome.setColorFilter(Color.WHITE);
        btnNavSummary.setColorFilter(Color.WHITE);
        btnNavHistory.setColorFilter(Color.WHITE);
        btnNavGoals.setColorFilter(Color.WHITE);
        btnNavPredicted.setColorFilter(Color.WHITE);

        switch (position) {
            case 0:
                btnNavHome.setAlpha(1.0f);
                btnNavHome.setColorFilter(primaryColor);
                break;
            case 1:
                btnNavSummary.setAlpha(1.0f);
                btnNavSummary.setColorFilter(primaryColor);
                break;
            case 2:
                btnNavHistory.setAlpha(1.0f);
                btnNavHistory.setColorFilter(primaryColor);
                break;
            case 3:
                btnNavGoals.setAlpha(1.0f);
                btnNavGoals.setColorFilter(primaryColor);
                break;
            case 4:
                btnNavPredicted.setAlpha(1.0f);
                btnNavPredicted.setColorFilter(primaryColor);
                break;
        }
    }

    public void goToPage(int page) {
        viewPager.setCurrentItem(page);
    }

    private void applyCustomColor() {
        int surfaceColor = PreferencesHelper.getSurfaceColor(this);

        // Status bar preta/escura para combinar com o tema
        getWindow().setStatusBarColor(androidx.core.content.ContextCompat.getColor(this, R.color.bg_dark));

        View bottomNav = findViewById(R.id.floating_menu_container);
        if (bottomNav != null) {
            bottomNav.setBackgroundColor(surfaceColor);
        }
    }
}
