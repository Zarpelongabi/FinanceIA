package com.financeai.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.financeai.R;
import com.financeai.utils.SpendingAnalyzer;
import java.util.ArrayList;
import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {
    private List<SpendingAnalyzer.Alert> alerts = new ArrayList<>();

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        SpendingAnalyzer.Alert alert = alerts.get(position);
        holder.tvEmoji.setText(alert.emoji);
        holder.tvTitle.setText(alert.title);
        holder.tvMessage.setText(alert.message);

        int bgColor, textColor;
        switch (alert.type) {
            case DANGER:
                bgColor = holder.itemView.getContext().getColor(R.color.alert_danger_bg);
                textColor = holder.itemView.getContext().getColor(R.color.alert_danger_text);
                break;
            case WARNING:
                bgColor = holder.itemView.getContext().getColor(R.color.alert_warning_bg);
                textColor = holder.itemView.getContext().getColor(R.color.alert_warning_text);
                break;
            default:
                bgColor = holder.itemView.getContext().getColor(R.color.alert_info_bg);
                textColor = holder.itemView.getContext().getColor(R.color.alert_info_text);
                break;
        }

        ((com.google.android.material.card.MaterialCardView) holder.itemView).setCardBackgroundColor(bgColor);
        holder.tvTitle.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    public void setAlerts(List<SpendingAnalyzer.Alert> alerts) {
        this.alerts = alerts;
        notifyDataSetChanged();
    }

    class AlertViewHolder extends RecyclerView.ViewHolder {
        private TextView tvEmoji, tvTitle, tvMessage;

        public AlertViewHolder(View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tv_alert_emoji);
            tvTitle = itemView.findViewById(R.id.tv_alert_title);
            tvMessage = itemView.findViewById(R.id.text_view_alert);
        }
    }
}
