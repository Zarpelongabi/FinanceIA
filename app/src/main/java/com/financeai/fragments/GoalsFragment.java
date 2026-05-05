package com.financeai.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.financeai.R;
import com.financeai.adapters.MetaAdapter;
import com.financeai.database.AppDatabase;

public class GoalsFragment extends Fragment {

    private RecyclerView rvGoals;
    private MetaAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goals, container, false);
        db = AppDatabase.getInstance(requireContext());
        rvGoals = view.findViewById(R.id.rv_goals_fragment);
        rvGoals.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new MetaAdapter(meta -> showGoalDetailsDialog(meta));
        rvGoals.setAdapter(adapter);

        view.findViewById(R.id.btn_add_goal).setOnClickListener(v -> showAddGoalDialog());
        
        loadGoals();
        return view;
    }

    private void showGoalDetailsDialog(com.financeai.models.Meta meta) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_goal_details, null);
        
        android.widget.TextView tvTitle = dialogView.findViewById(R.id.tv_detail_title);
        android.widget.TextView tvPercentage = dialogView.findViewById(R.id.tv_detail_percentage);
        com.google.android.material.progressindicator.LinearProgressIndicator progress = dialogView.findViewById(R.id.progress_detail);
        android.widget.TextView tvValues = dialogView.findViewById(R.id.tv_detail_values);
        android.widget.TextView tvDeadline = dialogView.findViewById(R.id.tv_detail_deadline);
        android.widget.TextView tvMonthly = dialogView.findViewById(R.id.tv_detail_monthly);
        android.widget.Button btnClose = dialogView.findViewById(R.id.btn_close_detail);
        android.widget.Button btnDelete = dialogView.findViewById(R.id.btn_delete_goal_detail);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.Theme_Vortex_Dark)
                .setView(dialogView)
                .create();

        tvTitle.setText(meta.getNome());
        int perc = meta.getValorObjetivo() > 0 ? (int) ((meta.getValorAtual() / meta.getValorObjetivo()) * 100) : 0;
        tvPercentage.setText(perc + "%");
        progress.setProgress(perc);
        
        tvValues.setText(com.financeai.utils.CurrencyHelper.format(meta.getValorAtual()) + " de " + com.financeai.utils.CurrencyHelper.format(meta.getValorObjetivo()));
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        tvDeadline.setText(sdf.format(new java.util.Date(meta.getDataLimite())));

        // Cálculo do mensal fixo (Baseado no plano original)
        java.util.Calendar start = java.util.Calendar.getInstance();
        if (meta.getDataCriacao() > 0) {
            start.setTimeInMillis(meta.getDataCriacao());
        } else {
            // Caso a meta seja antiga e não tenha data de criação, usa a data atual
            start.setTimeInMillis(System.currentTimeMillis());
        }
        
        java.util.Calendar end = java.util.Calendar.getInstance();
        end.setTimeInMillis(meta.getDataLimite());
        
        int totalMonths = (end.get(java.util.Calendar.YEAR) - start.get(java.util.Calendar.YEAR)) * 12 
                        + (end.get(java.util.Calendar.MONTH) - start.get(java.util.Calendar.MONTH));
        
        if (totalMonths <= 0) totalMonths = 1;

        // O valor mensal é SEMPRE o total dividido pelos meses totais do plano
        double monthly = meta.getValorObjetivo() / totalMonths;
        
        // Se a meta já foi batida, mostra 0
        if (meta.getValorAtual() >= meta.getValorObjetivo()) monthly = 0;

        tvMonthly.setText(com.financeai.utils.CurrencyHelper.format(monthly) + " / mês");

        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        btnDelete.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.Theme_Vortex_Dark)
                .setTitle("Excluir Meta")
                .setMessage("Deseja realmente apagar esta meta?")
                .setPositiveButton("Sim, excluir", (d, w) -> {
                    java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                        db.metaDao().delete(meta);
                        requireActivity().runOnUiThread(() -> dialog.dismiss());
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
        });

        dialog.show();
    }

    private void showAddGoalDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_goal, null);
        android.widget.EditText etTitle = dialogView.findViewById(R.id.et_goal_title);
        android.widget.EditText etTarget = dialogView.findViewById(R.id.et_goal_target);
        android.widget.EditText etDeadline = dialogView.findViewById(R.id.et_goal_deadline);
        android.widget.TextView tvMonthlySaving = dialogView.findViewById(R.id.tv_monthly_saving);
        android.widget.Button btnSave = dialogView.findViewById(R.id.btn_save_goal);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btn_cancel_goal);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.Theme_Vortex_Dark)
                .setView(dialogView)
                .create();

        // Lógica de cálculo em tempo real
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateMonthlySaving(etTarget, etDeadline, tvMonthlySaving);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        etTarget.addTextChangedListener(watcher);
        etDeadline.addTextChangedListener(watcher);

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String targetStr = etTarget.getText().toString();
            String monthsStr = etDeadline.getText().toString();

            if (title.isEmpty() || targetStr.isEmpty() || monthsStr.isEmpty()) {
                android.widget.Toast.makeText(getContext(), "Preencha todos os campos!", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            double target = Double.parseDouble(targetStr);
            int months = Integer.parseInt(monthsStr);
            
            com.financeai.models.Meta novaMeta = new com.financeai.models.Meta(title, target, 0, System.currentTimeMillis(), "Geral");
            // Calcula a data limite somando os meses
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.MONTH, months);
            novaMeta.setDataLimite(cal.getTimeInMillis());

            java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                db.metaDao().insert(novaMeta);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        android.widget.Toast.makeText(getContext(), "Meta \"" + title + "\" criada! 🚀", android.widget.Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                }
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void calculateMonthlySaving(android.widget.EditText etTarget, android.widget.EditText etDeadline, android.widget.TextView tvMonthly) {
        try {
            String targetStr = etTarget.getText().toString();
            String monthsStr = etDeadline.getText().toString();

            if (!targetStr.isEmpty() && !monthsStr.isEmpty()) {
                double target = Double.parseDouble(targetStr);
                double months = Double.parseDouble(monthsStr);

                if (months > 0) {
                    double monthly = target / months;
                    tvMonthly.setText(com.financeai.utils.CurrencyHelper.format(monthly) + " / mês");
                    return;
                }
            }
            tvMonthly.setText("R$ 0,00 / mês");
        } catch (Exception e) {
            tvMonthly.setText("R$ 0,00 / mês");
        }
    }

    private void loadGoals() {
        db.metaDao().getAllMetas().observe(getViewLifecycleOwner(), metas -> {
            if (metas != null) {
                adapter.setMetas(metas);
            }
        });
    }
}
