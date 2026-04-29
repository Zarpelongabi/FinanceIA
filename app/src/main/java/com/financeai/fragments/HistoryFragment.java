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
import java.util.concurrent.Executors;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private TransactionAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        db = AppDatabase.getInstance(requireContext());
        rvHistory = view.findViewById(R.id.rv_history);
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
        loadHistory();
        return view;
    }

    private void showDeleteDialog(com.financeai.models.Transaction transaction) {
        new AlertDialog.Builder(requireContext(), R.style.Theme_FinanceAI_Dark)
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

        db.transactionDao().getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                adapter.setTransactions(transactions);
            }
        });
    }
}
