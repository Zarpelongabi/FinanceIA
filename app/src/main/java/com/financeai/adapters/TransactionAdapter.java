package com.financeai.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.financeai.R;
import com.financeai.models.Category;
import com.financeai.models.Transaction;
import com.financeai.utils.CurrencyHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
        void onTransactionLongClick(Transaction transaction);
        default void onConfirmClick(Transaction transaction) {}
    }

    private List<Transaction> transactions = new ArrayList<>();
    private Map<String, Category> categoryMap = new HashMap<>();
    private final OnTransactionClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public TransactionAdapter(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction currentTransaction = transactions.get(position);
        
        // Se title for vazio, usa establishment
        String title = currentTransaction.getTitle();
        if (title == null || title.isEmpty()) {
            title = currentTransaction.getEstablishment();
        }
        if (title == null || title.isEmpty()) {
            title = "Sem título";
        }
        
        holder.textViewTitle.setText(title);
        
        // Ajuste: Cores e Ícones com Tint para manter o tema Vortex
        String cat = currentTransaction.getCategoryName() != null ? currentTransaction.getCategoryName().toLowerCase() : "";
        boolean isInvestment = cat.contains("invest") || cat.contains("poup") || cat.contains("reserva");

        // Limpa qualquer filtro de cor anterior para mostrar o PNG original
        holder.imageViewIcon.setColorFilter(null);
        holder.imageViewIcon.setImageTintList(null);

        if (isInvestment) {
            holder.textViewAmount.setText("-" + CurrencyHelper.format(currentTransaction.getAmount()));
            holder.textViewAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.invest_purple));
            holder.imageViewIcon.setImageResource(R.drawable.investimento);
        } else if (currentTransaction.isExpense()) {
            holder.textViewAmount.setText("-" + CurrencyHelper.format(currentTransaction.getAmount()));
            holder.textViewAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red_negative));
            
            Category category = categoryMap.get(currentTransaction.getCategoryName());
            if (category != null && category.getIconPath() != null) {
                Glide.with(holder.itemView.getContext())
                    .load(category.getIconPath())
                    .circleCrop()
                    .into(holder.imageViewIcon);
            } else {
                int iconRes = getCategoryIcon(holder.itemView.getContext(), currentTransaction.getCategoryName());
                holder.imageViewIcon.setImageResource(iconRes);
                
                // Apenas se for o ícone genérico "outros" e não um PNG colorido, aplicamos um leve brilho
                if (iconRes == R.drawable.outros) {
                    holder.imageViewIcon.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.text_gray));
                }
            }
        } else {
            holder.textViewAmount.setText("+" + CurrencyHelper.format(currentTransaction.getAmount()));
            holder.textViewAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green_positive));
            holder.imageViewIcon.setImageResource(R.drawable.salario);
        }

        holder.textViewDate.setText(dateFormat.format(new Date(currentTransaction.getDate())) + " · " + currentTransaction.getCategoryName());

        if (currentTransaction.isPredicted()) {
            holder.btnConfirmPredicted.setVisibility(View.VISIBLE);
            holder.btnConfirmPredicted.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConfirmClick(currentTransaction);
                }
            });
        } else {
            holder.btnConfirmPredicted.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(null); // Desativa o clique simples
        
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onTransactionLongClick(currentTransaction);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    public void setCategories(List<Category> categories) {
        this.categoryMap.clear();
        if (categories != null) {
            for (Category cat : categories) {
                this.categoryMap.put(cat.getName(), cat);
            }
        }
        notifyDataSetChanged();
    }

    private int getCategoryIcon(Context context, String category) {
        String resName = "outros";
        if (category != null) {
            String cat = category.toLowerCase();
            if (cat.contains("alimen") || cat.contains("mercado") || cat.contains("restaurante") || cat.contains("comida")) resName = "alimentacao";
            else if (cat.contains("transp") || cat.contains("uber") || cat.contains("combustivel") || cat.contains("carro")) resName = "transporte";
            else if (cat.contains("lazer") || cat.contains("cinema") || cat.contains("show") || cat.contains("viagem") || cat.contains("game")) resName = "lazer";
            else if (cat.contains("conta") || cat.contains("boleto") || cat.contains("luz") || cat.contains("agua") || cat.contains("internet") || cat.contains("mensalidade") || cat.contains("assinatura")) resName = "contas";
            else if (cat.contains("saúde") || cat.contains("saude") || cat.contains("farma") || cat.contains("medico") || cat.contains("hospital")) resName = "saude";
            else if (cat.contains("invest") || cat.contains("ação") || cat.contains("acao") || cat.contains("tesouro") || cat.contains("reserva") || cat.contains("meta")) resName = "investimento";
            else if (cat.contains("salário") || cat.contains("salario") || cat.contains("renda") || cat.contains("receita")) resName = "salario";
        }
        
        // Usa getDrawable para garantir que o sistema encontre os arquivos .png
        int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
        return resId != 0 ? resId : R.drawable.outros;
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewAmount;
        private TextView textViewDate;
        private ImageView imageViewIcon;
        private android.widget.ImageButton btnConfirmPredicted;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewAmount = itemView.findViewById(R.id.text_view_amount);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            imageViewIcon = itemView.findViewById(R.id.iv_category_icon);
            btnConfirmPredicted = itemView.findViewById(R.id.btn_confirm_predicted);
        }
    }
}
