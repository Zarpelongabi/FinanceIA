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
import com.financeai.models.Transacao;
import com.financeai.utils.CurrencyHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para a lista de transações, agora totalmente em português.
 */
public class TransacaoAdapter extends RecyclerView.Adapter<TransacaoAdapter.TransacaoViewHolder> {
    public interface OnTransacaoClickListener {
        void onTransacaoLongClick(Transacao transacao);
    }

    private List<Transacao> transacoes = new ArrayList<>();
    private final OnTransacaoClickListener listener;
    private final SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public TransacaoAdapter(OnTransacaoClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransacaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransacaoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransacaoViewHolder holder, int position) {
        Transacao atual = transacoes.get(position);
        
        String titulo = atual.getTitulo();
        if (titulo == null || titulo.isEmpty()) {
            titulo = atual.getEstabelecimento();
        }
        if (titulo == null || titulo.isEmpty()) {
            titulo = "Sem título";
        }
        
        holder.tvTitulo.setText(titulo);
        holder.tvValor.setText("-" + CurrencyHelper.format(atual.getValor()));
        holder.tvData.setText(formatoData.format(new Date(atual.getData())) + " · " + atual.getCategoriaNome());

        int iconeRes = getIconeCategoria(holder.itemView.getContext(), atual.getCategoriaNome());
        holder.ivIcone.setImageResource(iconeRes);

        // Clique simples desativado por padrão UX
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onTransacaoLongClick(atual);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transacoes.size();
    }

    public void setTransacoes(List<Transacao> transacoes) {
        this.transacoes = transacoes;
        notifyDataSetChanged();
    }

    private int getIconeCategoria(Context context, String categoria) {
        String nomeRes = "outros";
        if (categoria != null) {
            String cat = categoria.toLowerCase();
            if (cat.contains("alimen") || cat.contains("mercado")) nomeRes = "alimentacao";
            else if (cat.contains("transp") || cat.contains("uber")) nomeRes = "transporte";
            else if (cat.contains("lazer") || cat.contains("cinema")) nomeRes = "lazer";
            else if (cat.contains("conta") || cat.contains("boleto")) nomeRes = "contas";
            else if (cat.contains("saúde") || cat.contains("saude")) nomeRes = "saude";
            else if (cat.contains("invest")) nomeRes = "investimentos";
            else if (cat.contains("educa")) nomeRes = "educacao";
            else if (cat.contains("pet")) nomeRes = "pets";
            else if (cat.contains("casa") || cat.contains("moradia")) nomeRes = "moradia";
            else if (cat.contains("shop") || cat.contains("compra")) nomeRes = "compras";
            else if (cat.contains("assin")) nomeRes = "assinaturas";
        }
        
        int resId = context.getResources().getIdentifier(nomeRes, "drawable", context.getPackageName());
        return resId != 0 ? resId : android.R.drawable.ic_menu_agenda;
    }

    class TransacaoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvValor, tvData;
        ImageView ivIcone;

        public TransacaoViewHolder(View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.text_view_title);
            tvValor = itemView.findViewById(R.id.text_view_amount);
            tvData = itemView.findViewById(R.id.text_view_date);
            ivIcone = itemView.findViewById(R.id.iv_category_icon);
        }
    }
}
