package com.financeai.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.financeai.R;
import com.financeai.models.Meta;
import com.financeai.utils.CurrencyHelper;
import java.util.ArrayList;
import java.util.List;

public class MetaAdapter extends RecyclerView.Adapter<MetaAdapter.MetaViewHolder> {
    public interface OnMetaClickListener {
        void onMetaClick(Meta meta);
    }

    private List<Meta> metas = new ArrayList<>();
    private OnMetaClickListener listener;

    public MetaAdapter() {}

    public MetaAdapter(OnMetaClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false);
        return new MetaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MetaViewHolder holder, int position) {
        Meta meta = metas.get(position);
        holder.tvNome.setText(meta.getNome());
        holder.tvTarget.setText(CurrencyHelper.format(meta.getValorObjetivo()));
        holder.tvCurrent.setText(CurrencyHelper.format(meta.getValorAtual()));
        holder.tvDeadline.setText("Meta: " + meta.getDataLimite());
        
        int progresso = meta.getValorObjetivo() > 0 ? (int) ((meta.getValorAtual() / meta.getValorObjetivo()) * 100) : 0;
        holder.progressBar.setProgress(progresso);
        holder.tvPorcentagem.setText(progresso + "%");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMetaClick(meta);
            }
        });
    }

    @Override
    public int getItemCount() {
        return metas.size();
    }

    public void setMetas(List<Meta> metas) {
        this.metas = metas;
        notifyDataSetChanged();
    }

    static class MetaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvTarget, tvCurrent, tvDeadline, tvPorcentagem;
        com.google.android.material.progressindicator.LinearProgressIndicator progressBar;

        public MetaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tv_goal_title);
            tvTarget = itemView.findViewById(R.id.tv_goal_target);
            tvCurrent = itemView.findViewById(R.id.tv_goal_current);
            tvDeadline = itemView.findViewById(R.id.tv_goal_deadline);
            tvPorcentagem = itemView.findViewById(R.id.tv_goal_percentage);
            progressBar = itemView.findViewById(R.id.progress_goal);
        }
    }
}
