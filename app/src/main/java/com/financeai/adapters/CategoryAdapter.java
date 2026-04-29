package com.financeai.adapters;

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
import com.financeai.utils.CurrencyHelper;
import com.financeai.utils.PreferencesHelper;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
    }

    private List<Category> categories = new ArrayList<>();
    private final OnCategoryActionListener listener;

    public CategoryAdapter(OnCategoryActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_settings, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        
        if (category.getIconPath() != null) {
            holder.tvEmoji.setVisibility(View.GONE);
            holder.ivPhoto.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                .load(category.getIconPath())
                .circleCrop()
                .into(holder.ivPhoto);
        } else {
            holder.tvEmoji.setVisibility(View.VISIBLE);
            holder.ivPhoto.setVisibility(View.GONE);
            holder.tvEmoji.setText(category.getEmoji());
        }

        holder.tvName.setText(category.getName());
        holder.tvLimit.setText("Limite: " + CurrencyHelper.format(category.getMonthlyLimit()));

        int primaryColor = PreferencesHelper.getPrimaryColor(holder.itemView.getContext());
        holder.btnEdit.setColorFilter(primaryColor);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(category));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(category));
        
        // Don't allow deleting default categories if needed, or handle it
        holder.btnDelete.setVisibility(category.isCustom() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvLimit;
        ImageView ivPhoto;
        ImageButton btnEdit, btnDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tv_category_emoji);
            ivPhoto = itemView.findViewById(R.id.iv_category_photo);
            tvName = itemView.findViewById(R.id.tv_category_name);
            tvLimit = itemView.findViewById(R.id.tv_category_limit);
            btnEdit = itemView.findViewById(R.id.btn_edit_category);
            btnDelete = itemView.findViewById(R.id.btn_delete_category);
        }
    }
}
