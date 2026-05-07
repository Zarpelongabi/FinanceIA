package com.financeai.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.financeai.R;
import com.financeai.database.AppDatabase;
import com.financeai.models.Transaction;
import java.util.concurrent.Executors;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText etTitle, etAmount;
    private RadioButton rbFixed, rbPredicted;
    private Spinner spinnerCategory;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        db = AppDatabase.getInstance(this);

        etTitle = findViewById(R.id.et_title);
        etAmount = findViewById(R.id.et_amount);
        rbFixed = findViewById(R.id.rb_fixed);
        rbPredicted = findViewById(R.id.rb_predicted);
        spinnerCategory = findViewById(R.id.spinner_category);
        Button btnSave = findViewById(R.id.btn_save);

        setupCategorySpinner();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void setupCategorySpinner() {
        db.categoryDao().getAllCategories().observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                java.util.List<String> categoryNames = new java.util.ArrayList<>();
                for (com.financeai.models.Category c : categories) {
                    categoryNames.add(c.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_eco, categoryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);
            }
        });
    }

    private void saveTransaction() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim().replace(",", ".");

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isFixed = rbFixed.isChecked();
        boolean isPredicted = rbPredicted.isChecked();
        Object selectedItem = spinnerCategory.getSelectedItem();
        String category = selectedItem != null ? selectedItem.toString() : "Outros";

        Executors.newSingleThreadExecutor().execute(() -> {
            double totalIncome = db.transactionDao().getTotalIncome();
            double totalExpense = db.transactionDao().getTotalExpense();
            double currentBalance = totalIncome - totalExpense;

            if (!isPredicted && amount > currentBalance) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Aviso: Este gasto excede seu saldo disponível (R$ " + 
                        String.format(java.util.Locale.getDefault(), "%.2f", currentBalance) + ")", Toast.LENGTH_LONG).show();
                });
            }

            Transaction transaction = new Transaction();
            transaction.setTitle(title);
            transaction.setAmount(amount);
            transaction.setCategoryName(category);
            transaction.setRecurring(isFixed);
            // Se for gasto fixo, entra como previsto primeiro, conforme solicitado
            transaction.setPredicted(isFixed || isPredicted);
            transaction.setDate(System.currentTimeMillis());
            transaction.setExpense(true);

            db.transactionDao().insert(transaction);
            runOnUiThread(() -> {
                String msg = isFixed ? "Gasto fixo agendado em 'Previstos'!" : "Gasto salvo com sucesso!";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
