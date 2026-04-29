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
import com.financeai.utils.PreferencesHelper;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private android.widget.ImageButton btnNavHome, btnNavSummary, btnNavHistory, btnNavGoals, btnNavPredicted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Aplica a cor personalizada antes de criar a view
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
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        btnNavHome = findViewById(R.id.btn_nav_home);
        btnNavSummary = findViewById(R.id.btn_nav_summary);
        btnNavHistory = findViewById(R.id.btn_nav_history);
        btnNavGoals = findViewById(R.id.btn_nav_goals);
        btnNavPredicted = findViewById(R.id.btn_nav_predicted);

        findViewById(R.id.btn_investments).setOnClickListener(v -> startActivity(new Intent(this, InvestimentosActivity.class)));
        findViewById(R.id.btn_settings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
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
        int primaryColor = PreferencesHelper.getPrimaryColor(this);
        int darkColor = PreferencesHelper.getPrimaryDarkColor(this);
        int surfaceColor = PreferencesHelper.getSurfaceColor(this);

        // Aplica a cor na StatusBar
        getWindow().setStatusBarColor(darkColor);

        // Se houver um container de menu inferior, podemos pintar o fundo dele também
        View bottomNav = findViewById(R.id.floating_menu_container);
        if (bottomNav != null) {
            bottomNav.setBackgroundColor(surfaceColor);
        }
    }
}
