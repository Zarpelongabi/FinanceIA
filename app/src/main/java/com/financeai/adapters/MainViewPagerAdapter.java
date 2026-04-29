package com.financeai.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.financeai.fragments.HomeFragment;
import com.financeai.fragments.SummaryFragment;
import com.financeai.fragments.HistoryFragment;
import com.financeai.fragments.GoalsFragment;
import com.financeai.fragments.PredictedExpensesFragment;

public class MainViewPagerAdapter extends FragmentStateAdapter {

    public MainViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1: return new SummaryFragment();
            case 2: return new HistoryFragment();
            case 3: return new GoalsFragment();
            case 4: return new PredictedExpensesFragment();
            default: return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
