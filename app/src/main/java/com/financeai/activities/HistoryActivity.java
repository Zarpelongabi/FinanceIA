package com.financeai.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.financeai.R;
import com.financeai.adapters.TransactionAdapter;
import com.financeai.database.AppDatabase;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private TransactionAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = AppDatabase.getInstance(this);

        rvHistory = findViewById(R.id.rv_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new TransactionAdapter(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(com.financeai.models.Transaction transaction) {
                // Clique simples desativado
            }

            @Override
            public void onTransactionLongClick(com.financeai.models.Transaction transaction) {
                showDeleteDialog(transaction);
            }
        });
        rvHistory.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadHistory();
    }

    private void showDeleteDialog(com.financeai.models.Transaction transaction) {
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_FinanceAI_Dark)
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
        db.transactionDao().getAllTransactions().observe(this, transactions -> {
            if (transactions != null) {
                adapter.setTransactions(transactions);
            }
        });
    }
}
