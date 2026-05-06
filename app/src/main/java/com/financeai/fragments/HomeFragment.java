package com.financeai.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.financeai.R;
import com.financeai.activities.AddTransactionActivity;
import com.financeai.adapters.TransactionAdapter;
import com.financeai.database.AppDatabase;
import com.financeai.utils.CurrencyHelper;
import com.financeai.utils.DateHelper;
import com.financeai.utils.PreferencesHelper;
import com.financeai.utils.SpendingAnalyzer;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private TextView tvBalance, tvMonthTotal, tvSavingsStatus, tvSalaryValue, tvExtraIncomeValue, tvAiInsightText, tvFifthDay, tvLabelStatus, tvSeeAll, tvWelcome;
    private PieChart pieChart;
    private RecyclerView rvTransactions;
    private Button btnAddExpense, btnManageExpenses;
    private ImageButton btnMenu;
    private View cardSalary, cardExtraIncome, cardAiInsight;

    private AppDatabase db;
    private TransactionAdapter transactionAdapter;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean showingAnalysis = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        db = AppDatabase.getInstance(requireContext());
        initViews(view);
        loadDashboardData();
        observeTransactions();
        checkMonthlyReport(); 
        
        return view;
    }

    private void toggleCardFeedback() {
        View cardContent = (getView() != null) ? getView().findViewById(R.id.card_main_content) : null;
        if (cardContent == null) return;

        cardContent.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction(() -> {
                cardContent.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
                
                showingAnalysis = !showingAnalysis;
                loadDashboardData();
            })
            .start();
    }

    private void checkMonthlyReport() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH);
        
        if (dayOfMonth == 1) {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("vortex_prefs", Context.MODE_PRIVATE);
            String lastReportMonth = prefs.getString("last_report_month", "");
            String currentMonth = (cal.get(java.util.Calendar.MONTH) + 1) + "/" + cal.get(java.util.Calendar.YEAR);
            
            if (!lastReportMonth.equals(currentMonth)) {
                showMonthlyReport();
                prefs.edit().putString("last_report_month", currentMonth).apply();
            }
        }
    }

    private void showMonthlyReport() {
        executor.execute(() -> {
            Context context = getContext();
            if (context == null) return;
            
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.MONTH, -1);
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
            long startLastMonth = cal.getTimeInMillis();
            cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
            long endLastMonth = cal.getTimeInMillis();

            double totalSpent = db.transactionDao().getTotalExpenseByPeriod(startLastMonth, endLastMonth);
            List<com.financeai.database.dao.TransactionDao.CategorySummary> categories = db.transactionDao().getCategoryExpensesSummary(startLastMonth, endLastMonth);
            
            String topCategory = (categories != null && !categories.isEmpty()) ? categories.get(0).categoryName : "N/A";
            
            android.app.Activity activity = getActivity();
            if (activity != null && isAdded()) {
                activity.runOnUiThread(() -> {
                    Context ctx = getContext();
                    if (ctx == null) return;
                    new AlertDialog.Builder(ctx, R.style.Theme_Vortex_Dark)
                        .setTitle(R.string.monthly_report_title)
                        .setMessage(getString(R.string.monthly_report_message, 
                                CurrencyHelper.format(totalSpent), topCategory))
                        .setPositiveButton(R.string.view_history, (d, w) -> {
                             if (getActivity() instanceof com.financeai.activities.MainActivity) {
                                 ((com.financeai.activities.MainActivity) getActivity()).goToPage(1);
                             }
                        })
                        .setNegativeButton(R.string.close, null)
                        .show();
                });
            }
        });
    }

    private void initViews(View view) {
        tvBalance = view.findViewById(R.id.tv_balance);
        tvMonthTotal = view.findViewById(R.id.tv_month_total);
        tvSavingsStatus = view.findViewById(R.id.tv_savings_status);
        pieChart = view.findViewById(R.id.pie_chart);
        rvTransactions = view.findViewById(R.id.rv_transactions);
        tvSalaryValue = view.findViewById(R.id.tv_salary_value);
        tvExtraIncomeValue = view.findViewById(R.id.tv_extra_income_value);
        tvAiInsightText = view.findViewById(R.id.tv_ai_insight_text);
        tvFifthDay = view.findViewById(R.id.tv_fifth_day_label);
        tvLabelStatus = view.findViewById(R.id.tv_label_status);
        tvSeeAll = view.findViewById(R.id.tv_see_all);
        tvWelcome = view.findViewById(R.id.tv_welcome);
        cardSalary = view.findViewById(R.id.card_salary);
        cardExtraIncome = view.findViewById(R.id.card_extra_income);
        cardAiInsight = view.findViewById(R.id.card_ai_insight);
        btnAddExpense = view.findViewById(R.id.btn_add_expense);
        btnManageExpenses = view.findViewById(R.id.btn_manage_expenses);
        btnMenu = view.findViewById(R.id.btn_menu);

        View cardSummary = view.findViewById(R.id.card_summary);
        cardSummary.setOnClickListener(v -> toggleCardFeedback());

        tvSeeAll.setOnClickListener(v -> {
            if (getActivity() instanceof com.financeai.activities.MainActivity) {
                ((com.financeai.activities.MainActivity) getActivity()).goToPage(1);
            }
        });

        cardSalary.setOnClickListener(v -> showSalaryDialog(true));
        cardExtraIncome.setOnClickListener(v -> showSalaryDialog(false));

        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionAdapter = new TransactionAdapter(new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(com.financeai.models.Transaction transaction) {}
            @Override
            public void onTransactionLongClick(com.financeai.models.Transaction transaction) {
                showDeleteDialog(transaction);
            }
        });
        rvTransactions.setAdapter(transactionAdapter);

        btnAddExpense.setOnClickListener(v -> startActivity(new Intent(getContext(), AddTransactionActivity.class)));
        btnManageExpenses.setOnClickListener(v -> {
            if (getActivity() instanceof com.financeai.activities.MainActivity) {
                ((com.financeai.activities.MainActivity) getActivity()).goToPage(1);
            }
        });

        btnMenu.setOnClickListener(this::showPopupMenu);
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenuInflater().inflate(R.menu.home_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_settings) {
                return true;
            } else if (id == R.id.menu_export) {
                return true;
            } else if (id == R.id.menu_about) {
                return true;
            }
            return false;
        });
        popup.show();
    }



    private void loadDashboardData() {
        executor.execute(() -> {
            Context context = getContext();
            if (context == null || !isAdded()) return;

            long[] month = SpendingAnalyzer.getMonthRange();
            
            double totalExpensesIncludingInvestments = db.transactionDao().getTotalExpenseByPeriod(month[0], month[1]);
            double realExpensesOnly = db.transactionDao().getTotalRealExpenseByPeriod(month[0], month[1]);
            double investmentsOnly = totalExpensesIncludingInvestments - realExpensesOnly;

            double salary = PreferencesHelper.getSalary(context);
            double extra = context.getSharedPreferences("vortex_prefs", Context.MODE_PRIVATE).getFloat("extra", 0f);
            double totalIncome = salary + extra;
            
            double balance = totalIncome - totalExpensesIncludingInvestments;

            List<com.financeai.models.Meta> metas = db.metaDao().getAllMetasSync();
            double totalMetaEffort = 0;
            for (com.financeai.models.Meta m : metas) {
                if (m.getValorAtual() < m.getValorObjetivo()) {
                    java.util.Calendar start = java.util.Calendar.getInstance();
                    if (m.getDataCriacao() > 0) start.setTimeInMillis(m.getDataCriacao());
                    
                    java.util.Calendar end = java.util.Calendar.getInstance();
                    end.setTimeInMillis(m.getDataLimite());
                    
                    int months = (end.get(java.util.Calendar.YEAR) - start.get(java.util.Calendar.YEAR)) * 12 
                                + (end.get(java.util.Calendar.MONTH) - start.get(java.util.Calendar.MONTH));
                    if (months <= 0) months = 1;
                    
                    totalMetaEffort += (m.getValorObjetivo() / months);
                }
            }

            String statusTitle = "Meta de Aporte Mensal";
            String statusValue = CurrencyHelper.format(totalMetaEffort);
            int primaryColor = PreferencesHelper.getPrimaryColor(context);
            int primaryLight = PreferencesHelper.getPrimaryLightColor(context);
            int statusColor = primaryLight;

            if (totalIncome <= 0) {
                statusValue = "Defina sua renda";
                statusColor = Color.GRAY;
            } else if (investmentsOnly >= totalMetaEffort && totalMetaEffort > 0) {
                statusTitle = "Meta de Aporte";
                statusValue = "Concluída! ✅";
                statusColor = primaryColor;
            }

            List<com.financeai.database.dao.TransactionDao.CategorySummary> catSummary = db.transactionDao().getCategoryExpensesSummary(month[0], month[1]);
            
            String aiInsight;
            if (showingAnalysis) {
                if (investmentsOnly > 0) {
                    aiInsight = String.format("Vortex AI: Você já protegeu %s este mês. Isso não é gasto, é patrimônio! Seu custo de vida real está em %.0f%% da sua renda.", 
                        CurrencyHelper.format(investmentsOnly), (realExpensesOnly/totalIncome)*100);
                } else {
                    aiInsight = "Vortex AI: Seus gastos estão focados em consumo. Tente usar o botão 'Investir Sobra' para começar seu patrimônio.";
                }
            } else {
                if (totalIncome <= 0) aiInsight = "Defina sua renda nos cards abaixo!";
                else if (balance < 0) aiInsight = "Cuidado! Você gastou mais do que recebeu.";
                else if (catSummary != null && !catSummary.isEmpty()) {
                    String topCat = "outros";
                    for(com.financeai.database.dao.TransactionDao.CategorySummary cs : catSummary) {
                        String name = cs.categoryName.toLowerCase();
                        if (!name.contains("invest") && !name.contains("poup") && !name.contains("reserva")) {
                            topCat = cs.categoryName;
                            break;
                        }
                    }
                    aiInsight = String.format("Vortex AI: Seu maior gasto real é com %s.", topCat);
                } else aiInsight = "Adicione seus primeiros gastos!";
            }

            final String finalStatusTitle = statusTitle;
            final String finalStatusValue = statusValue;
            final int finalStatusColor = statusColor;
            final String finalAiInsight = aiInsight;
            final String fifthDay = DateHelper.getFifthWorkingDay();
            
            android.app.Activity activity = getActivity();
            if (activity != null && isAdded()) {
                activity.runOnUiThread(() -> {
                    if (getView() == null) return;
                    
                    String userName = PreferencesHelper.getUserName(context);
                    tvWelcome.setText(getString(R.string.welcome_user, userName));

                    boolean hideBalance = PreferencesHelper.isHideBalance(context);
                    if (hideBalance) {
                        tvBalance.setText("••••");
                        tvMonthTotal.setText("••••");
                        tvSalaryValue.setText("••••");
                        tvExtraIncomeValue.setText("••••");
                        tvSavingsStatus.setText("••••");
                    } else {
                        tvBalance.setText(balance < 0 ? "R$ 0,00" : CurrencyHelper.format(balance));
                        tvMonthTotal.setText("-" + CurrencyHelper.format(realExpensesOnly));
                        tvSalaryValue.setText(CurrencyHelper.format(salary));
                        tvExtraIncomeValue.setText(CurrencyHelper.format(extra));
                        tvSavingsStatus.setText(finalStatusValue);
                    }

                    tvAiInsightText.setText(finalAiInsight);
                    tvFifthDay.setText("Recebe em: " + fifthDay);

                    if (investmentsOnly > 0 && !hideBalance) {
                        tvMonthTotal.setAlpha(0.7f);
                    } else {
                        tvMonthTotal.setAlpha(1.0f);
                    }
                    
                    if (tvLabelStatus != null) tvLabelStatus.setText(finalStatusTitle);
                    
                    if (!hideBalance) {
                        tvSavingsStatus.setTextColor(finalStatusColor);
                    }
                    
                    setupPieChart(catSummary, totalIncome, totalExpensesIncludingInvestments);
                });
            }
        });
    }

    private void observeTransactions() {
        long[] range = SpendingAnalyzer.getMonthRange();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        long endOfDay = cal.getTimeInMillis();

        db.categoryDao().getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                transactionAdapter.setCategories(categories);
            }
        });

        db.transactionDao().getTransactionsByPeriod(range[0], endOfDay).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                transactions.sort((t1, t2) -> Double.compare(t2.getAmount(), t1.getAmount()));
                
                List<com.financeai.models.Transaction> list = transactions.size() > 5 ? transactions.subList(0, 5) : transactions;
                transactionAdapter.setTransactions(list);
            }
        });
    }

    private void setupPieChart(List<com.financeai.database.dao.TransactionDao.CategorySummary> catSummary, double totalIncome, double monthTotal) {
        if (catSummary == null || catSummary.isEmpty() || !isAdded()) return;
        
        List<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        
        // Ordenar por valor (decrescente) para o gráfico
        catSummary.sort((o1, o2) -> Double.compare(o2.total, o1.total));

        for (com.financeai.database.dao.TransactionDao.CategorySummary cs : catSummary) {
            float percentage = (float) (cs.total / monthTotal * 100);
            if (percentage > 0) {
                entries.add(new PieEntry((float) cs.total, cs.categoryName));
                colors.add(getCategoryColor(cs.categoryName));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(3f);
        
        // Configurações solicitadas: Apenas porcentagem, sem nomes no gráfico
        dataSet.setDrawValues(true);
        pieChart.setUsePercentValues(true);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.PercentFormatter(pieChart));

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        
        // Estilização "Donut" mais fino
        pieChart.setHoleRadius(85f); 
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterText("Gastos\n" + (int)((monthTotal/totalIncome)*100) + "%");
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setCenterTextSize(14f);
        
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false); 
        pieChart.setNoDataText("Aguardando dados...");
        pieChart.setNoDataTextColor(Color.WHITE);
        
        // Remove labels das fatias (mostra apenas na legenda)
        pieChart.setDrawEntryLabels(false);
        
        pieChart.animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    private int getCategoryColor(String category) {
        String cat = category.toLowerCase();
        if (cat.contains("saúde")) return ContextCompat.getColor(requireContext(), R.color.chart_health);
        if (cat.contains("conta")) return ContextCompat.getColor(requireContext(), R.color.red_negative);
        if (cat.contains("alimen")) return ContextCompat.getColor(requireContext(), R.color.chart_food);
        if (cat.contains("lazer")) return ContextCompat.getColor(requireContext(), R.color.chart_leisure);
        if (cat.contains("transp") || cat.contains("uber")) return ContextCompat.getColor(requireContext(), R.color.chart_transport);
        if (cat.contains("invest") || cat.contains("poup") || cat.contains("reserva")) return ContextCompat.getColor(requireContext(), R.color.invest_purple);
        return ContextCompat.getColor(requireContext(), R.color.chart_others);
    }

    private void showSalaryDialog(boolean isSalary) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_income, null);
        ImageView ivIcon = dialogView.findViewById(R.id.iv_dialog_icon);
        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextView tvSubtitle = dialogView.findViewById(R.id.tv_dialog_subtitle);
        EditText input = dialogView.findViewById(R.id.et_income_value);
        Button btnSave = dialogView.findViewById(R.id.btn_save_income);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_income);
        TextView btnClear = dialogView.findViewById(R.id.btn_clear_income);

        if (isSalary) {
            tvTitle.setText(R.string.my_salary);
            tvSubtitle.setText(R.string.salary_subtitle);
            ivIcon.setImageResource(R.drawable.salario);
        } else {
            tvTitle.setText(R.string.extra_income);
            tvSubtitle.setText(R.string.extra_income_subtitle);
            ivIcon.setImageResource(R.drawable.outros);
        }

        double currentVal = isSalary ? PreferencesHelper.getSalary(requireContext()) : 
                requireContext().getSharedPreferences("vortex_prefs", Context.MODE_PRIVATE).getFloat("extra", 0f);

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.Theme_Vortex_Dark)
                .setView(dialogView)
                .create();
        
        if (currentVal > 0) {
            input.setText(String.valueOf(currentVal));
            btnClear.setVisibility(View.VISIBLE);
            btnClear.setOnClickListener(v -> {
                saveIncome(isSalary, 0);
                dialog.dismiss();
            });
        }

        btnSave.setOnClickListener(v -> {
            String valStr = input.getText().toString();
            double value = valStr.isEmpty() ? 0 : Double.parseDouble(valStr);
            saveIncome(isSalary, value);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void saveIncome(boolean isSalary, double value) {
        if (isSalary) {
            PreferencesHelper.setSalary(requireContext(), value);
        } else {
            requireContext().getSharedPreferences("vortex_prefs", Context.MODE_PRIVATE)
                .edit().putFloat("extra", (float) value).apply();
        }

        executor.execute(() -> {
            long[] range = com.financeai.utils.SpendingAnalyzer.getMonthRange();
            String catName = isSalary ? "Salário" : "Renda Extra";
            com.financeai.models.Transaction existing = db.transactionDao().getIncomeTransaction(catName, range[0], range[1]);

            if (value <= 0) {
                if (existing != null) db.transactionDao().delete(existing);
            } else {
                if (existing != null) {
                    existing.setAmount(value);
                    db.transactionDao().update(existing);
                } else {
                    com.financeai.models.Transaction incomeTx = new com.financeai.models.Transaction();
                    incomeTx.setTitle(isSalary ? "Salário Mensal" : "Renda Extra");
                    incomeTx.setAmount(value);
                    incomeTx.setCategoryName(catName);
                    incomeTx.setDate(System.currentTimeMillis());
                    incomeTx.setExpense(false);
                    db.transactionDao().insert(incomeTx);
                }
            }
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> loadDashboardData());
        });
    }

    private void showDeleteDialog(com.financeai.models.Transaction transaction) {
        new AlertDialog.Builder(requireContext(), R.style.Theme_Vortex_Dark)
            .setTitle(R.string.delete_action)
            .setPositiveButton(R.string.yes, (d, w) -> executor.execute(() -> {
                db.transactionDao().delete(transaction);
                loadDashboardData();
            })).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }
}
