package com.financeai.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.financeai.R;
import com.financeai.adapters.CategoryAdapter;
import com.financeai.database.AppDatabase;
import com.financeai.models.Category;
import com.financeai.utils.PreferencesHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private AppDatabase db;
    private CategoryAdapter adapter;
    private MaterialSwitch switchTheme;
    private static final int PICK_FILE_REQUEST = 1;
    private static final int CREATE_FILE_REQUEST = 2;
    private static final int PICK_IMAGE_REQUEST = 3;

    private String currentEditingImagePath = null;
    private ImageView ivDialogPhoto;
    private TextView tvDialogEmoji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferencesHelper.isDarkTheme(this)) {
            setTheme(R.style.Theme_FinanceAI_Dark);
        } else {
            setTheme(R.style.Theme_FinanceAI);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db = AppDatabase.getInstance(this);

        initViews();
        loadCategories();
    }

    private void initViews() {
        setupColorPicker();

        int primaryColor = PreferencesHelper.getPrimaryColor(this);
        
        MaterialButton btnAddCategory = findViewById(R.id.btn_add_category);
        btnAddCategory.setStrokeColor(ColorStateList.valueOf(primaryColor));
        btnAddCategory.setTextColor(primaryColor);
        btnAddCategory.setIconTint(ColorStateList.valueOf(primaryColor));

        RecyclerView rvCategories = findViewById(R.id.rv_categories);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(Category category) {
                showEditCategoryDialog(category);
            }

            @Override
            public void onDelete(Category category) {
                deleteCategory(category);
            }
        });
        rvCategories.setAdapter(adapter);

        MaterialButton btnExport = findViewById(R.id.btn_export_data);
        MaterialButton btnImport = findViewById(R.id.btn_import_data);
        btnExport.setIconTint(ColorStateList.valueOf(primaryColor));
        btnImport.setIconTint(ColorStateList.valueOf(primaryColor));

        findViewById(R.id.btn_add_category).setOnClickListener(v -> showEditCategoryDialog(null));
        
        findViewById(R.id.btn_export_data).setOnClickListener(v -> exportDatabase());
        findViewById(R.id.btn_import_data).setOnClickListener(v -> importDatabase());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(primaryColor);
        }
        
        ((TextView)findViewById(R.id.tv_title_personalization)).setTextColor(primaryColor);
        ((TextView)findViewById(R.id.tv_title_backup)).setTextColor(primaryColor);
        ((TextView)findViewById(R.id.tv_title_categories)).setTextColor(primaryColor);

        // Aplicar cor de superfície dinâmica nos cards
        int surfaceColor = PreferencesHelper.getSurfaceColor(this);
        findViewById(R.id.card_personalization).setBackgroundTintList(ColorStateList.valueOf(surfaceColor));
        findViewById(R.id.card_backup).setBackgroundTintList(ColorStateList.valueOf(surfaceColor));
        
        // Aplicar cor na barra de status
        getWindow().setStatusBarColor(PreferencesHelper.getPrimaryDarkColor(this));
    }

    private void setupColorPicker() {
        View colorEmerald = findViewById(R.id.color_emerald);
        View colorBlue = findViewById(R.id.color_blue);
        View colorPurple = findViewById(R.id.color_purple);
        View colorOrange = findViewById(R.id.color_orange);

        colorEmerald.setOnClickListener(v -> updatePrimaryColor(getResources().getColor(R.color.accent_emerald)));
        colorBlue.setOnClickListener(v -> updatePrimaryColor(getResources().getColor(R.color.accent_blue)));
        colorPurple.setOnClickListener(v -> updatePrimaryColor(getResources().getColor(R.color.accent_purple)));
        colorOrange.setOnClickListener(v -> updatePrimaryColor(getResources().getColor(R.color.accent_orange)));
    }

    private void updatePrimaryColor(int color) {
        PreferencesHelper.setPrimaryColor(this, color);
        Toast.makeText(this, "Cor atualizada! Reiniciando para aplicar...", Toast.LENGTH_SHORT).show();
        
        // Reinicia o app a partir da MainActivity para aplicar a cor em todo lugar
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadCategories() {
        db.categoryDao().getAllCategories().observe(this, categories -> {
            if (categories != null) {
                adapter.setCategories(categories);
            }
        });
    }

    private void showEditCategoryDialog(Category category) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_category, null);
        ivDialogPhoto = dialogView.findViewById(R.id.iv_dialog_photo);
        tvDialogEmoji = dialogView.findViewById(R.id.tv_dialog_emoji);
        EditText etName = dialogView.findViewById(R.id.et_category_name);
        EditText etLimit = dialogView.findViewById(R.id.et_category_limit);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save_category);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_category);
        View frameIcon = dialogView.findViewById(R.id.frame_category_icon);

        if (category != null) {
            etName.setText(category.getName());
            etLimit.setText(String.valueOf(category.getMonthlyLimit()));
            currentEditingImagePath = category.getIconPath();
            updateDialogIcon(category.getEmoji(), currentEditingImagePath);
        } else {
            currentEditingImagePath = null;
            updateDialogIcon("📦", null);
        }

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_FinanceAI_Dark)
                .setView(dialogView)
                .create();

        frameIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String limitStr = etLimit.getText().toString();
            double limit = limitStr.isEmpty() ? 0 : Double.parseDouble(limitStr);

            if (!name.isEmpty()) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    if (category == null) {
                        Category newCat = new Category(name, "📦", "#888780", limit);
                        newCat.setCustom(true);
                        newCat.setIconPath(currentEditingImagePath);
                        db.categoryDao().insert(newCat);
                    } else {
                        category.setName(name);
                        category.setMonthlyLimit(limit);
                        category.setIconPath(currentEditingImagePath);
                        db.categoryDao().update(category);
                    }
                });
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Informe o nome", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateDialogIcon(String emoji, String path) {
        if (path != null) {
            tvDialogEmoji.setVisibility(View.GONE);
            ivDialogPhoto.setVisibility(View.VISIBLE);
            Glide.with(this).load(path).circleCrop().into(ivDialogPhoto);
        } else {
            tvDialogEmoji.setVisibility(View.VISIBLE);
            ivDialogPhoto.setVisibility(View.GONE);
            tvDialogEmoji.setText(emoji);
        }
    }

    private void deleteCategory(Category category) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.categoryDao().delete(category);
        });
    }

    private void exportDatabase() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, "financeai_backup.db");
        startActivityForResult(intent, CREATE_FILE_REQUEST);
    }

    private void importDatabase() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == CREATE_FILE_REQUEST) {
                copyDatabaseToUri(uri);
            } else if (requestCode == PICK_FILE_REQUEST) {
                copyUriToDatabase(uri);
            } else if (requestCode == PICK_IMAGE_REQUEST) {
                handleImagePick(uri);
            }
        }
    }

    private void handleImagePick(Uri uri) {
        try {
            // Salva a imagem internamente para garantir que o app sempre tenha acesso
            File folder = new File(getFilesDir(), "category_icons");
            if (!folder.exists()) folder.mkdirs();
            
            String fileName = "cat_" + UUID.randomUUID().toString() + ".jpg";
            File destFile = new File(folder, fileName);
            
            InputStream in = getContentResolver().openInputStream(uri);
            OutputStream out = new FileOutputStream(destFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            
            currentEditingImagePath = destFile.getAbsolutePath();
            updateDialogIcon(null, currentEditingImagePath);
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyDatabaseToUri(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                File dbFile = getDatabasePath("finance_db");
                // Note: If your database name is different, change it here.
                // Usually it's defined in AppDatabase.getInstance()
                
                InputStream in = new FileInputStream(dbFile);
                OutputStream out = getContentResolver().openOutputStream(uri);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                runOnUiThread(() -> Toast.makeText(this, "Backup concluído!", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Erro ao exportar", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void copyUriToDatabase(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                db.close();
                File dbFile = getDatabasePath("finance_db");
                InputStream in = getContentResolver().openInputStream(uri);
                OutputStream out = new FileOutputStream(dbFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Importação concluída! Reiniciando...", Toast.LENGTH_LONG).show();
                    recreate();
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Erro ao importar", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
