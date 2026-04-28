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
    private TextView menuHome, menuHistory, menuGoals;
    private android.widget.ImageButton btnInvestments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        if (PreferencesHelper.isDarkTheme(this)) {
            setTheme(R.style.Theme_FinanceAI_Dark);
        }
        setContentView(R.layout.activity_main);

        initViews();
        setupViewPager();
        setupMenu();
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        menuHome = findViewById(R.id.menu_home);
        menuHistory = findViewById(R.id.menu_history);
        menuGoals = findViewById(R.id.menu_goals);
        btnInvestments = findViewById(R.id.btn_investments);

        btnInvestments.setImageResource(R.drawable.investimento);
        btnInvestments.setOnClickListener(v -> startActivity(new Intent(this, InvestimentosActivity.class)));
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
        menuHome.setOnClickListener(v -> viewPager.setCurrentItem(0));
        menuHistory.setOnClickListener(v -> viewPager.setCurrentItem(1));
        menuGoals.setOnClickListener(v -> viewPager.setCurrentItem(2));
    }

    private void updateMenuHighlight(int position) {
        menuHome.setAlpha(0.6f);
        menuHistory.setAlpha(0.6f);
        menuGoals.setAlpha(0.6f);
        
        menuHome.setTextColor(Color.WHITE);
        menuHistory.setTextColor(Color.WHITE);
        menuGoals.setTextColor(Color.WHITE);

        switch (position) {
            case 0:
                menuHome.setAlpha(1.0f);
                menuHome.setTextColor(getResources().getColor(R.color.primary_eco));
                break;
            case 1:
                menuHistory.setAlpha(1.0f);
                menuHistory.setTextColor(getResources().getColor(R.color.primary_eco));
                break;
            case 2:
                menuGoals.setAlpha(1.0f);
                menuGoals.setTextColor(getResources().getColor(R.color.primary_eco));
                break;
        }
    }

    public void goToPage(int page) {
        viewPager.setCurrentItem(page);
    }
}
