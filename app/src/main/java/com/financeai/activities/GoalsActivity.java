package com.financeai.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.financeai.R;
import com.financeai.adapters.MetaAdapter;
import com.financeai.database.AppDatabase;
import com.financeai.models.Meta;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoalsActivity extends AppCompatActivity {
    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private MetaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        db = AppDatabase.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_goals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MetaAdapter();
        recyclerView.setAdapter(adapter);

        // Agora usando MetaDao e a classe Meta traduzida
        db.metaDao().getAllMetas().observe(this, metas -> {
            if (metas != null) {
                adapter.setMetas(metas);
            }
        });

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        findViewById(R.id.btn_add_goal).setOnClickListener(v -> showAddGoalDialog());
    }

    private void showAddGoalDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null);
        EditText etTitle = dialogView.findViewById(R.id.et_goal_title);
        EditText etTarget = dialogView.findViewById(R.id.et_goal_target);
        EditText etDeadline = dialogView.findViewById(R.id.et_goal_deadline);
        TextView tvMonthlySaving = dialogView.findViewById(R.id.tv_monthly_saving);
        TextView tvRecommendationDesc = dialogView.findViewById(R.id.tv_recommendation_desc);
        Button btnSave = dialogView.findViewById(R.id.btn_save_goal);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_goal);

        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateRecommendation(etTarget, etDeadline, tvMonthlySaving, tvRecommendationDesc);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };

        etTarget.addTextChangedListener(watcher);
        etDeadline.addTextChangedListener(watcher);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_Vortex_Dark)
                .setView(dialogView)
                .create();

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String targetStr = etTarget.getText().toString();
            String deadlineStr = etDeadline.getText().toString();

            if (title.isEmpty() || targetStr.isEmpty() || deadlineStr.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            double target = Double.parseDouble(targetStr);
            int months = Integer.parseInt(deadlineStr);
            
            // Calcula data limite baseada nos meses
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.MONTH, months);

            Meta novaMeta = new Meta(title, target, 0, cal.getTimeInMillis(), "Geral");

            executor.execute(() -> {
                db.metaDao().insert(novaMeta);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Meta \"" + title + "\" criada! 🚀", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        
        dialog.show();
    }

    private void calculateRecommendation(EditText etTarget, EditText etDeadline, TextView tvMonthly, TextView tvDesc) {
        String targetStr = etTarget.getText().toString();
        String deadlineStr = etDeadline.getText().toString();

        if (!targetStr.isEmpty() && !deadlineStr.isEmpty()) {
            try {
                double target = Double.parseDouble(targetStr);
                int months = Integer.parseInt(deadlineStr);

                if (months > 0) {
                    double monthly = target / months;
                    tvMonthly.setText(com.financeai.utils.CurrencyHelper.format(monthly) + " / mês");
                    tvDesc.setText("Para realizar este sonho em " + months + " meses, você precisa poupar este valor.");
                } else {
                    tvMonthly.setText("Prazo inválido");
                    tvDesc.setText("O número de meses deve ser maior que zero.");
                }
            } catch (Exception e) {
                tvMonthly.setText("Aguardando dados...");
            }
        } else {
            tvMonthly.setText("Aguardando dados...");
            tvDesc.setText("Preencha o valor e o prazo para calcular o esforço mensal.");
        }
    }
}
