package com.financeai.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.financeai.R;
import com.financeai.adapters.TransactionAdapter;
import com.financeai.database.AppDatabase;
import com.financeai.models.Transaction;
import java.util.concurrent.Executors;

public class PredictedExpensesFragment extends Fragment {

    private RecyclerView rvPredicted;
    private TransactionAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_predicted_expenses, container, false);
        db = AppDatabase.getInstance(requireContext());
        rvPredicted = view.findViewById(R.id.rv_predicted);
        rvPredicted.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TransactionAdapter(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {}

            @Override
            public void onTransactionLongClick(Transaction transaction) {}

            @Override
            public void onConfirmClick(Transaction transaction) {
                confirmExpense(transaction);
            }
        });
        rvPredicted.setAdapter(adapter);
        loadPredictedExpenses();
        return view;
    }

    private void confirmExpense(Transaction transaction) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.transactionDao().confirmPredictedExpense(transaction.getId());
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Gasto confirmado!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadPredictedExpenses() {
        db.transactionDao().getPredictedTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                adapter.setTransactions(transactions);
            }
        });
    }
}
