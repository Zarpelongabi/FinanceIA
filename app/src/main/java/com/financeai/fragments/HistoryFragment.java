package com.financeai.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.financeai.R;
import com.financeai.adapters.TransactionAdapter;
import com.financeai.database.AppDatabase;
import com.financeai.utils.SpendingAnalyzer;
import com.google.android.material.tabs.TabLayout;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private TabLayout tabMonths;
    private TransactionAdapter adapter;
    private AppDatabase db;
    private Calendar selectedMonth = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        db = AppDatabase.getInstance(requireContext());
        rvHistory = view.findViewById(R.id.rv_history);
        tabMonths = view.findViewById(R.id.tab_months);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new TransactionAdapter(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(com.financeai.models.Transaction transaction) {}
            @Override
            public void onTransactionLongClick(com.financeai.models.Transaction transaction) {
                showDeleteDialog(transaction);
            }
        });
        rvHistory.setAdapter(adapter);
        
        setupTabs();
        loadHistory();
        return view;
    }

    private void setupTabs() {
        tabMonths.removeAllTabs();
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 12; i++) {
            String monthName = new java.text.SimpleDateFormat("MMM/yy", Locale.getDefault()).format(cal.getTime());
            TabLayout.Tab tab = tabMonths.newTab().setText(monthName).setTag(cal.clone());
            tabMonths.addTab(tab, i == 0);
            cal.add(Calendar.MONTH, -1);
        }

        tabMonths.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedMonth = (Calendar) tab.getTag();
                loadHistory();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showDeleteDialog(com.financeai.models.Transaction transaction) {
        new AlertDialog.Builder(requireContext(), R.style.Theme_Vortex_Dark)
            .setTitle("Excluir Lançamento")
            .setMessage("Deseja realmente apagar este registro?")
            .setPositiveButton("Excluir", (dialog, which) -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    db.transactionDao().delete(transaction);
                });
            })
            .setNegativeButton("Manter", null)
            .show();
    }

    private void loadHistory() {
        db.categoryDao().getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                adapter.setCategories(categories);
            }
        });

        long[] range = SpendingAnalyzer.getMonthRange(selectedMonth);
        db.transactionDao().getTransactionsByPeriod(range[0], range[1]).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                adapter.setTransactions(transactions);
            }
        });
    }
}
