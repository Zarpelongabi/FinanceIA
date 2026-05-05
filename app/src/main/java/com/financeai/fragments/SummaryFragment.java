package com.financeai.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.financeai.R;
import com.financeai.adapters.TransactionAdapter;
import com.financeai.database.AppDatabase;
import com.financeai.database.dao.TransactionDao;
import com.financeai.models.Category;
import com.financeai.utils.CurrencyHelper;
import com.financeai.utils.PreferencesHelper;
import com.financeai.utils.SpendingAnalyzer;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SummaryFragment extends Fragment {

    private TextView tvComparisonText, tvSummaryTip;
    private ProgressBar pbComparison;
    private RecyclerView rvTopCategories;
    private TabLayout tabMonths;
    private AppDatabase db;
    private List<Category> allCategories = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Calendar selectedMonth = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);
        db = AppDatabase.getInstance(requireContext());
        
        tvComparisonText = view.findViewById(R.id.tv_comparison_text);
        tvSummaryTip = view.findViewById(R.id.tv_summary_tip);
        pbComparison = view.findViewById(R.id.pb_comparison);
        rvTopCategories = view.findViewById(R.id.rv_top_categories);
        tabMonths = view.findViewById(R.id.tab_months);

        rvTopCategories.setLayoutManager(new LinearLayoutManager(getContext()));

        setupTabs();

        db.categoryDao().getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                this.allCategories = categories;
                loadSummaryData(); 
            }
        });

        loadSummaryData();
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
                loadSummaryData();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadSummaryData() {
        executor.execute(() -> {
            long[] currentRange = SpendingAnalyzer.getMonthRange(selectedMonth);
            
            Calendar lastMonthCal = (Calendar) selectedMonth.clone();
            lastMonthCal.add(Calendar.MONTH, -1);
            long[] lastRange = SpendingAnalyzer.getMonthRange(lastMonthCal);

            double spentCurrent = db.transactionDao().getTotalRealExpenseByPeriod(currentRange[0], currentRange[1]);
            double spentLast = db.transactionDao().getTotalRealExpenseByPeriod(lastRange[0], lastRange[1]);
            
            List<TransactionDao.CategorySummary> categories = db.transactionDao().getCategoryExpensesSummary(currentRange[0], currentRange[1]);

            // NOVO: Filtrar categorias de investimento/reserva do resumo de gastos
            if (categories != null) {
                categories.removeIf(cs -> {
                    String name = cs.categoryName.toLowerCase();
                    return name.contains("invest") || name.contains("poup") || name.contains("reserva");
                });
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    int primaryColor = PreferencesHelper.getPrimaryColor(requireContext());
                    if (spentLast > 0) {
                        double diff = ((spentCurrent - spentLast) / spentLast) * 100;
                        if (diff < 0) {
                            tvComparisonText.setText(String.format(Locale.getDefault(), "Você gastou %.0f%% menos que no mês passado! 🎉", Math.abs(diff)));
                            tvComparisonText.setTextColor(primaryColor);
                        } else {
                            tvComparisonText.setText(String.format(Locale.getDefault(), "Seus gastos subiram %.0f%% em relação ao mês passado.", diff));
                            tvComparisonText.setTextColor(getResources().getColor(R.color.red_negative));
                        }
                        pbComparison.setProgress((int) Math.min(100, (spentCurrent / spentLast) * 100));
                        pbComparison.setProgressTintList(android.content.res.ColorStateList.valueOf(primaryColor));
                    } else {
                        tvComparisonText.setText("Primeiro mês de uso? Continue registrando!");
                        pbComparison.setProgress(0);
                    }

                    if (categories != null && !categories.isEmpty()) {
                        setupCategoryList(categories, spentCurrent);
                    }
                });
            }
        });
    }

    private void setupCategoryList(List<TransactionDao.CategorySummary> categories, double total) {
        rvTopCategories.setAdapter(new RecyclerView.Adapter<CategoryViewHolder>() {
            @NonNull
            @Override
            public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
                return new CategoryViewHolder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
                TransactionDao.CategorySummary catSummary = categories.get(position);
                
                holder.tvTitle.setText(catSummary.categoryName);
                holder.tvAmount.setText(CurrencyHelper.format(catSummary.total));
                holder.tvAmount.setTextColor(getResources().getColor(R.color.red_negative));
                
                double percent = (catSummary.total / total) * 100;
                holder.tvDate.setText(String.format(Locale.getDefault(), "%.0f%% do total do mês", percent));

                // Busca ícone personalizado
                Category category = null;
                for (Category c : allCategories) {
                    if (c.getName().equals(catSummary.categoryName)) {
                        category = c;
                        break;
                    }
                }

                if (category != null && category.getIconPath() != null) {
                    com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(category.getIconPath())
                        .circleCrop()
                        .into(holder.ivIcon);
                } else {
                    holder.ivIcon.setImageResource(getCategoryIcon(holder.itemView.getContext(), catSummary.categoryName));
                }
            }

            @Override
            public int getItemCount() {
                return Math.min(categories.size(), 5);
            }
        });
    }

    private int getCategoryIcon(android.content.Context context, String category) {
        String resName = "outros";
        if (category != null) {
            String cat = category.toLowerCase();
            if (cat.contains("alimen") || cat.contains("mercado") || cat.contains("restaurante")) resName = "alimentacao";
            else if (cat.contains("transp") || cat.contains("uber") || cat.contains("combustivel")) resName = "transporte";
            else if (cat.contains("lazer") || cat.contains("cinema") || cat.contains("show") || cat.contains("viagem")) resName = "lazer";
            else if (cat.contains("conta") || cat.contains("boleto") || cat.contains("luz") || cat.contains("agua")) resName = "contas";
            else if (cat.contains("saúde") || cat.contains("saude") || cat.contains("farma") || cat.contains("medico")) resName = "saude";
            else if (cat.contains("invest") || cat.contains("ação") || cat.contains("acao") || cat.contains("tesouro") || cat.contains("reserva")) resName = "investimentos";
            else if (cat.contains("educa") || cat.contains("curso") || cat.contains("faculdade") || cat.contains("livro")) resName = "educacao";
            else if (cat.contains("pet") || cat.contains("dog") || cat.contains("cat")) resName = "pets";
            else if (cat.contains("casa") || cat.contains("aluguel") || cat.contains("moveis")) resName = "moradia";
            else if (cat.contains("shop") || cat.contains("compra") || cat.contains("roupa")) resName = "compras";
            else if (cat.contains("assin") || cat.contains("netflix") || cat.contains("spotify") || cat.contains("streaming")) resName = "assinaturas";
        }
        int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
        return resId != 0 ? resId : android.R.drawable.ic_menu_agenda;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAmount, tvDate;
        android.widget.ImageView ivIcon;
        CategoryViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.text_view_title);
            tvAmount = v.findViewById(R.id.text_view_amount);
            tvDate = v.findViewById(R.id.text_view_date);
            ivIcon = v.findViewById(R.id.iv_category_icon);
        }
    }
}
