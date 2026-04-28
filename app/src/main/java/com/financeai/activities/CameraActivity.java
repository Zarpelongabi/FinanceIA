package com.financeai.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.financeai.R;
import com.financeai.database.AppDatabase;
import com.financeai.models.Category;
import com.financeai.models.Transaction;
import com.financeai.utils.NotificationHelper;
import com.financeai.utils.OcrHelper;
import com.financeai.utils.PreferencesHelper;
import com.financeai.utils.SpendingAnalyzer;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    // Views
    private PreviewView previewView;
    private ImageView ivPreviewCapture;
    private ProgressBar progressOcr;
    private TextView tvOcrStatus;
    private View layoutOcrResult, layoutCamera;
    private EditText etEstablishment, etAmount, etDate, etNotes;
    private Spinner spinnerCategory;
    private Button btnCapture, btnGallery, btnSave, btnRetake;

    // Camera
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    // Data
    private AppDatabase db;
    private List<Category> categories = new ArrayList<>();
    private OcrHelper.OcrResult currentOcrResult;
    private Bitmap capturedBitmap;
    private String capturedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        db = AppDatabase.getInstance(this);
        cameraExecutor = Executors.newSingleThreadExecutor();

        initViews();
        loadCategories();
        checkPermissions();
    }

    private void initViews() {
        previewView = findViewById(R.id.preview_view);
        ivPreviewCapture = findViewById(R.id.iv_preview_capture);
        progressOcr = findViewById(R.id.progress_ocr);
        tvOcrStatus = findViewById(R.id.tv_ocr_status);
        layoutOcrResult = findViewById(R.id.layout_ocr_result);
        layoutCamera = findViewById(R.id.layout_camera);
        etEstablishment = findViewById(R.id.et_establishment);
        etAmount = findViewById(R.id.et_amount);
        etDate = findViewById(R.id.et_date);
        etNotes = findViewById(R.id.et_notes);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnCapture = findViewById(R.id.btn_capture);
        btnGallery = findViewById(R.id.btn_gallery);
        btnSave = findViewById(R.id.btn_save);
        btnRetake = findViewById(R.id.btn_retake);

        btnCapture.setOnClickListener(v -> capturePhoto());
        btnGallery.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> saveTransaction());
        btnRetake.setOnClickListener(v -> showCameraMode());

        // Botão voltar
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadCategories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            categories = db.categoryDao().getAllCategoriesSync();
            runOnUiThread(() -> {
                List<String> names = new ArrayList<>();
                for (Category c : categories) names.add(c.getDisplayName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);
            });
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
            ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(getCacheDir(),
            "receipt_" + System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions options =
            new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this),
            new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(ImageCapture.OutputFileResults results) {
                    capturedImagePath = photoFile.getAbsolutePath();
                    try {
                        Bitmap bmp = MediaStore.Images.Media.getBitmap(
                            getContentResolver(),
                            Uri.fromFile(photoFile));
                        capturedBitmap = bmp;
                        processOcr(bmp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(ImageCaptureException exception) {
                    Toast.makeText(CameraActivity.this,
                        "Erro ao capturar foto", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private final ActivityResultLauncher<Intent> galleryLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                try {
                    Uri uri = result.getData().getData();
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    capturedBitmap = bmp;
                    capturedImagePath = uri.toString();
                    processOcr(bmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void processOcr(Bitmap bitmap) {
        showOcrLoading();
        ivPreviewCapture.setImageBitmap(bitmap);

        OcrHelper.processImage(this, bitmap, new OcrHelper.OcrCallback() {
            @Override
            public void onSuccess(OcrHelper.OcrResult result) {
                currentOcrResult = result;
                runOnUiThread(() -> showOcrResult(result));
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    tvOcrStatus.setText("Não foi possível ler o comprovante. Preencha manualmente.");
                    progressOcr.setVisibility(View.GONE);
                    showOcrResultEmpty();
                });
            }
        });
    }

    private void showOcrLoading() {
        layoutCamera.setVisibility(View.GONE);
        layoutOcrResult.setVisibility(View.GONE);
        ivPreviewCapture.setVisibility(View.VISIBLE);
        progressOcr.setVisibility(View.VISIBLE);
        tvOcrStatus.setVisibility(View.VISIBLE);
        tvOcrStatus.setText("Analisando comprovante com IA...");
    }

    private void showOcrResult(OcrHelper.OcrResult result) {
        progressOcr.setVisibility(View.GONE);
        tvOcrStatus.setText("✓ Dados extraídos automaticamente. Confirme abaixo:");
        layoutOcrResult.setVisibility(View.VISIBLE);

        // Preenche campos com dados do OCR
        if (result.establishment != null && !result.establishment.isEmpty()) {
            etEstablishment.setText(result.establishment);
        }
        if (result.amount > 0) {
            etAmount.setText(String.format(Locale.getDefault(), "%.2f", result.amount));
        }
        if (result.date != null) {
            etDate.setText(result.date);
        }

        // Seleciona categoria sugerida
        if (result.suggestedCategory != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getName().equals(result.suggestedCategory)) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    private void showOcrResultEmpty() {
        progressOcr.setVisibility(View.GONE);
        layoutOcrResult.setVisibility(View.VISIBLE);
        etDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(new Date()));
    }

    private void showCameraMode() {
        layoutCamera.setVisibility(View.VISIBLE);
        layoutOcrResult.setVisibility(View.GONE);
        ivPreviewCapture.setVisibility(View.GONE);
        progressOcr.setVisibility(View.GONE);
        tvOcrStatus.setVisibility(View.GONE);
        currentOcrResult = null;
    }

    private void saveTransaction() {
        String establishment = etEstablishment.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim().replace(",", ".");
        String dateStr = etDate.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (establishment.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Preencha o estabelecimento e o valor", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        long dateTimestamp = System.currentTimeMillis();
        try {
            Date parsed = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr);
            if (parsed != null) dateTimestamp = parsed.getTime();
        } catch (ParseException ignored) {}

        Category selectedCategory = categories.isEmpty() ? null
            : categories.get(spinnerCategory.getSelectedItemPosition());

        Transaction transaction = new Transaction(
            establishment, amount,
            selectedCategory != null ? String.valueOf(selectedCategory.getId()) : "outros",
            establishment, dateTimestamp, true);

        if (selectedCategory != null) {
            transaction.setCategoryName(selectedCategory.getName());
        }
        transaction.setNotes(notes);
        transaction.setReceiptImagePath(capturedImagePath);
        transaction.setOcrExtracted(currentOcrResult != null);
        transaction.setType(currentOcrResult != null ? "ocr" : "manual");

        final long finalDateTimestamp = dateTimestamp;
        Executors.newSingleThreadExecutor().execute(() -> {
            db.transactionDao().insert(transaction);

            // Checa alertas pós-inserção
            long[] today = SpendingAnalyzer.getDayRange();
            int todayCount = db.transactionDao().getExpenseCountByPeriod(today[0], today[1]);
            if (todayCount >= 4) {
                NotificationHelper.sendImpulsiveBuyingAlert(this, todayCount);
            }

            // Pontuação por registrar gasto
            PreferencesHelper.addScore(this, 10);

            runOnUiThread(() -> {
                Toast.makeText(this, "Gasto salvo! +10 pontos 🎉", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}