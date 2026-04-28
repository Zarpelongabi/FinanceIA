package com.financeai.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.financeai.fragments.HomeFragment;
import com.financeai.fragments.HistoryFragment;
import com.financeai.fragments.GoalsFragment;

public class MainViewPagerAdapter extends FragmentStateAdapter {

    public MainViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1: return new HistoryFragment();
            case 2: return new GoalsFragment();
            default: return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
