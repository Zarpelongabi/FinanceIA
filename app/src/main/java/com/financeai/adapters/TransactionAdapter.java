package com.financeai.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.financeai.R;
import com.financeai.models.Transaction;
import com.financeai.utils.CurrencyHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
        void onTransactionLongClick(Transaction transaction);
    }

    private List<Transaction> transactions = new ArrayList<>();
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
        
        // Ajuste: Cores e Ícones
        String cat = currentTransaction.getCategoryName() != null ? currentTransaction.getCategoryName().toLowerCase() : "";
        boolean isInvestment = cat.contains("invest") || cat.contains("poup");

        if (isInvestment) {
            holder.textViewAmount.setText("-" + CurrencyHelper.format(currentTransaction.getAmount()));
            holder.textViewAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.invest_purple));
            
            holder.imageViewIcon.setImageResource(R.drawable.investimento);
        } else if (currentTransaction.isExpense()) {
            holder.textViewAmount.setText("-" + CurrencyHelper.format(currentTransaction.getAmount()));
            holder.textViewAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red_negative));
            
            // Ícone da categoria para gastos
            holder.imageViewIcon.setImageResource(getCategoryIcon(holder.itemView.getContext(), currentTransaction.getCategoryName()));
        } else {
            holder.textViewAmount.setText("+" + CurrencyHelper.format(currentTransaction.getAmount()));
            holder.textViewAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green_positive));
            
            // Ícone salario.png para ganhos
            holder.imageViewIcon.setImageResource(R.drawable.salario);
        }

        holder.textViewDate.setText(dateFormat.format(new Date(currentTransaction.getDate())) + " · " + currentTransaction.getCategoryName());

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

    private int getCategoryIcon(Context context, String category) {
        String resName = "outros";
        if (category != null) {
            String cat = category.toLowerCase();
            if (cat.contains("alimen") || cat.contains("mercado") || cat.contains("restaurante")) resName = "alimentacao";
            else if (cat.contains("transp") || cat.contains("uber") || cat.contains("combustivel")) resName = "transporte";
            else if (cat.contains("lazer") || cat.contains("cinema") || cat.contains("show") || cat.contains("viagem")) resName = "lazer";
            else if (cat.contains("conta") || cat.contains("boleto") || cat.contains("luz") || cat.contains("agua")) resName = "contas";
            else if (cat.contains("saúde") || cat.contains("saude") || cat.contains("farma") || cat.contains("medico")) resName = "saude";
            else if (cat.contains("invest") || cat.contains("ação") || cat.contains("acao") || cat.contains("tesouro")) resName = "investimentos";
            else if (cat.contains("educa") || cat.contains("curso") || cat.contains("faculdade") || cat.contains("livro")) resName = "educacao";
            else if (cat.contains("pet") || cat.contains("dog") || cat.contains("cat")) resName = "pets";
            else if (cat.contains("casa") || cat.contains("aluguel") || cat.contains("moveis")) resName = "moradia";
            else if (cat.contains("shop") || cat.contains("compra") || cat.contains("roupa")) resName = "compras";
            else if (cat.contains("assin") || cat.contains("netflix") || cat.contains("spotify") || cat.contains("streaming")) resName = "assinaturas";
        }
        
        // Usa getDrawable para garantir que o sistema encontre os arquivos .png
        int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
        return resId != 0 ? resId : android.R.drawable.ic_menu_agenda;
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewAmount;
        private TextView textViewDate;
        private ImageView imageViewIcon;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewAmount = itemView.findViewById(R.id.text_view_amount);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            imageViewIcon = itemView.findViewById(R.id.iv_category_icon);
        }
    }
}
