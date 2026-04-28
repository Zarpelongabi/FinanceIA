package com.financeai.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.financeai.R;
import com.financeai.adapters.MetaAdapter;
import com.financeai.database.AppDatabase;
import com.financeai.models.Meta;
import com.financeai.models.Transaction;
import com.financeai.utils.CurrencyHelper;
import com.financeai.utils.SpendingAnalyzer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InvestimentosActivity extends AppCompatActivity {

    private TextView tvTotal, tvProfit, tvPoupanca, tvInvest, tvPoupancaGoals, tvDisponivelMes;
    private SharedPreferences prefs;
    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private double saldoDisponivelAtual = 0;

    private static final float TAXA_POUPANCA = 0.005f; 
    private static final float TAXA_INVEST = 0.01f;     

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        
        setContentView(R.layout.activity_investimentos);

        prefs = getSharedPreferences("FinanceAI", MODE_PRIVATE);
        db = AppDatabase.getInstance(this);
        
        initViews();
        loadData();

        findViewById(R.id.btn_back_invest).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_poupanca).setOnClickListener(v -> showSelectMetaDialog());
        findViewById(R.id.btn_add_invest).setOnClickListener(v -> showAddDialog("invest", "Novo Investimento", null));
        findViewById(R.id.btn_investir_sobra).setOnClickListener(v -> investirSobra());
    }

    private void initViews() {
        tvTotal = findViewById(R.id.tv_total_patrimonio);
        tvProfit = findViewById(R.id.tv_total_profit);
        tvPoupanca = findViewById(R.id.tv_poupanca_value);
        tvInvest = findViewById(R.id.tv_invest_value);
        tvPoupancaGoals = findViewById(R.id.tv_poupanca_goals);
        tvDisponivelMes = findViewById(R.id.tv_disponivel_mes);
    }

    private void loadData() {
        executor.execute(() -> {
            long[] month = SpendingAnalyzer.getMonthRange();
            
            float salary = prefs.getFloat("salary", 0f);
            float extra = prefs.getFloat("extra", 0f);
            double otherIncome = db.transactionDao().getTotalIncomeByPeriod(month[0], month[1]);
            double totalBalance = salary + extra + otherIncome;
            double totalExpenses = db.transactionDao().getTotalExpenseByPeriod(month[0], month[1]);
            
            saldoDisponivelAtual = totalBalance - totalExpenses;

            // O patrimônio de "Metas" agora é a soma do que já foi guardado em todas as metas
            double totalMetasGuardado = db.metaDao().getTotalMetaAtual(); 
            float vInvest = prefs.getFloat("val_investimento", 0f); 

            // Rendimentos simulados (CDB 1% e Ações/Risco 1.5%)
            double lucroMetas = totalMetasGuardado * 0.005; // 0.5% (Segurança)
            double lucroInvest = vInvest * 0.012; // 1.2% (Rendimento Variável)
            double lucroTotal = lucroMetas + lucroInvest;

            runOnUiThread(() -> {
                tvPoupanca.setText(com.financeai.utils.CurrencyHelper.format(totalMetasGuardado));
                tvInvest.setText(com.financeai.utils.CurrencyHelper.format(vInvest));
                tvPoupancaGoals.setText("Patrimônio reservado para seus sonhos");
                tvTotal.setText(com.financeai.utils.CurrencyHelper.format(totalMetasGuardado + vInvest));
                tvProfit.setText(String.format("+ %s rendimento est./mês", com.financeai.utils.CurrencyHelper.format(lucroTotal)));
                tvDisponivelMes.setText(com.financeai.utils.CurrencyHelper.format(Math.max(0, saldoDisponivelAtual)));
                
                View btnSobra = findViewById(R.id.btn_investir_sobra);
                if (btnSobra != null) {
                    btnSobra.setEnabled(saldoDisponivelAtual > 0);
                    btnSobra.setAlpha(saldoDisponivelAtual > 0 ? 1.0f : 0.5f);
                }
            });
        });
    }

    private void showSelectMetaDialog() {
        executor.execute(() -> {
            List<Meta> metas = db.metaDao().getAllMetasSync();
            runOnUiThread(() -> {
                if (metas.isEmpty()) {
                    Toast.makeText(this, "Você ainda não tem metas criadas!", Toast.LENGTH_SHORT).show();
                    return;
                }

                View view = LayoutInflater.from(this).inflate(R.layout.dialog_select_goal, null);
                RecyclerView rv = view.findViewById(R.id.rv_select_meta);
                rv.setLayoutManager(new LinearLayoutManager(this));
                
                AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_FinanceAI_Dark)
                        .setView(view)
                        .create();

                MetaAdapter adapter = new MetaAdapter(meta -> {
                    dialog.dismiss();
                    showAddDialog("poupanca", "Guardar para: " + meta.getNome(), meta);
                });
                adapter.setMetas(metas);
                rv.setAdapter(adapter);

                view.findViewById(R.id.btn_cancel_selection).setOnClickListener(v -> dialog.dismiss());
                dialog.show();
            });
        });
    }

    private void showAddDialog(String key, String title, Meta targetMeta) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null);
        EditText etValue = view.findViewById(R.id.et_goal_target);
        TextView tvMonthly = view.findViewById(R.id.tv_monthly_saving);
        TextView tvDesc = view.findViewById(R.id.tv_recommendation_desc);
        TextView tvRecTitle = view.findViewById(R.id.tv_rec_title);
        
        ((TextView)view.findViewById(R.id.tv_dialog_title)).setText(title);
        
        // Esconde campos de criação para o modo de Aporte
        view.findViewById(R.id.til_goal_title).setVisibility(View.GONE);
        view.findViewById(R.id.til_goal_deadline).setVisibility(View.GONE);
        
        // Configura Recomendação FinanceAI para Meta Selecionada
        if (targetMeta != null) {
            long diffMillis = targetMeta.getDataLimite() - System.currentTimeMillis();
            double days = diffMillis / (1000.0 * 60 * 60 * 24);
            long months = (long) Math.ceil(days / 30.44); // Média de dias por mês para estabilidade
            if (months < 1) months = 1;

            double remainingInitial = targetMeta.getValorObjetivo() - targetMeta.getValorAtual();
            double monthlyInitial = remainingInitial / months;
            
            tvRecTitle.setText("💡 Recomendação FinanceAI");
            tvMonthly.setText(com.financeai.utils.CurrencyHelper.format(Math.max(0, monthlyInitial)) + " / mês");
            tvDesc.setText("Este é o esforço mensal necessário para atingir sua meta no prazo.");

            final long finalMonths = months;
            etValue.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        double input = s.toString().isEmpty() ? 0 : Double.parseDouble(s.toString());
                        double totalFaltante = targetMeta.getValorObjetivo() - (targetMeta.getValorAtual() + input);
                        if (totalFaltante < 0) totalFaltante = 0;
                        
                        double monthly = totalFaltante / finalMonths;
                        tvMonthly.setText(com.financeai.utils.CurrencyHelper.format(monthly) + " / mês");
                        tvDesc.setText(input > 0 ? "Com este aporte, o esforço mensal cai para este valor." : "Este é o esforço mensal necessário para atingir sua meta no prazo.");
                    } catch (Exception e) {
                        tvMonthly.setText("R$ 0,00 / mês");
                    }
                }
            });
        } else {
            view.findViewById(R.id.card_recommendation).setVisibility(View.GONE);
        }

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_FinanceAI_Dark)
                .setView(view)
                .create();

        view.findViewById(R.id.btn_save_goal).setOnClickListener(v -> {
            String valStr = etValue.getText().toString();
            if (!valStr.isEmpty()) {
                double val = Double.parseDouble(valStr);
                
                // Validação de Saldo Real
                if (val > saldoDisponivelAtual) {
                    Toast.makeText(this, "Saldo insuficiente na conta!", Toast.LENGTH_SHORT).show();
                    return;
                }

                float current = prefs.getFloat("val_" + key, 0f);
                prefs.edit().putFloat("val_" + key, (float)(current + val)).apply();

                executor.execute(() -> {
                    if (targetMeta != null) {
                        targetMeta.setValorAtual(targetMeta.getValorAtual() + val);
                        db.metaDao().update(targetMeta);
                    }

                    Transaction tx = new Transaction();
                    tx.setTitle(targetMeta != null ? "Aporte: " + targetMeta.getNome() : (key.equals("poupanca") ? "Aplicação Poupança" : "Novo Investimento"));
                    tx.setAmount((float)val);
                    tx.setCategoryName(key.equals("poupanca") ? "Poupança" : "Investimento");
                    tx.setDate(System.currentTimeMillis());
                    tx.setExpense(true);
                    db.transactionDao().insert(tx);

                    runOnUiThread(() -> {
                        loadData();
                        dialog.dismiss();
                        Toast.makeText(this, "Valor guardado com sucesso!", Toast.LENGTH_SHORT).show();
                    });
                });
            }
        });

        view.findViewById(R.id.btn_cancel_goal).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void investirSobra() {
        if (saldoDisponivelAtual > 0) {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null);
            EditText etValue = view.findViewById(R.id.et_goal_target);
            TextView tvMonthlySaving = view.findViewById(R.id.tv_monthly_saving);
            TextView tvDesc = view.findViewById(R.id.tv_recommendation_desc);
            TextView tvRecTitle = view.findViewById(R.id.tv_rec_title);
            
            // Sugere 80% do saldo real disponível puxado do banco
            double recomendacao80 = saldoDisponivelAtual * 0.8;
            etValue.setText(String.format(java.util.Locale.US, "%.2f", recomendacao80));
            
            // UI Cleanup para Investir Sobra
            view.findViewById(R.id.til_goal_title).setVisibility(View.GONE);
            view.findViewById(R.id.til_goal_deadline).setVisibility(View.GONE);
            ((TextView)view.findViewById(R.id.tv_dialog_title)).setText("Investir Sobra");
            ((TextView)view.findViewById(R.id.tv_dialog_subtitle)).setText("Saldo Disponível: " + com.financeai.utils.CurrencyHelper.format(saldoDisponivelAtual));
            ((Button)view.findViewById(R.id.btn_save_goal)).setText("Investir Agora");
            
            // Customizando a Recomendação FinanceAI para 80%
            tvRecTitle.setText("💡 Recomendação de Alocação (80%)");
            tvMonthlySaving.setText(com.financeai.utils.CurrencyHelper.format(recomendacao80));
            tvDesc.setText("Investir 80% da sua sobra mantém uma reserva de segurança de 20%.");

            AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_FinanceAI_Dark)
                    .setView(view)
                    .create();

            view.findViewById(R.id.btn_save_goal).setOnClickListener(v -> {
                String valStr = etValue.getText().toString();
                if (!valStr.isEmpty()) {
                    float val = Float.parseFloat(valStr);
                    if (val > saldoDisponivelAtual) {
                        Toast.makeText(this, "Você não tem tudo isso sobrando!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    float currentInvest = prefs.getFloat("val_investimento", 0f);
                    prefs.edit().putFloat("val_investimento", currentInvest + val).apply();

                    // Ajustar saldo disponível imediatamente para refletir no "Disponível para investir"
                    // O saldo será recalculado no loadData() baseado nas transações, 
                    // mas adicionamos a transação primeiro para garantir a consistência.
                    
                    executor.execute(() -> {
                        Transaction tx = new Transaction();
                        tx.setTitle("Investimento de Sobra");
                        tx.setAmount(val);
                        tx.setCategoryName("Investimento");
                        tx.setDate(System.currentTimeMillis());
                        tx.setExpense(true); // Conta como gasto para reduzir o saldo disponível no mês
                        db.transactionDao().insert(tx);

                        runOnUiThread(() -> {
                            Toast.makeText(this, com.financeai.utils.CurrencyHelper.format(val) + " investidos com sucesso! 🚀", Toast.LENGTH_SHORT).show();
                            loadData(); // Recarrega tudo
                            dialog.dismiss();
                        });
                    });
                }
            });

            view.findViewById(R.id.btn_cancel_goal).setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
    }
}
