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

        String[] categories = {"Alimentação", "Transporte", "Lazer", "Contas", "Saúde", "Outros"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_eco, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        boolean isFixed = rbFixed.isChecked();
        boolean isPredicted = rbPredicted.isChecked();
        String category = spinnerCategory.getSelectedItem().toString();

        Transaction transaction = new Transaction();
        transaction.setTitle(title);
        transaction.setAmount(amount);
        transaction.setCategoryName(category);
        transaction.setRecurring(isFixed);
        transaction.setPredicted(isPredicted);
        transaction.setDate(System.currentTimeMillis());
        transaction.setExpense(true); // Definido como gasto

        Executors.newSingleThreadExecutor().execute(() -> {
            db.transactionDao().insert(transaction);
            runOnUiThread(() -> {
                Toast.makeText(this, "Gasto salvo com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
