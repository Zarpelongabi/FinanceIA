package com.financeai.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.financeai.R;
import com.financeai.utils.PreferencesHelper;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etName = findViewById(R.id.et_name);
        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                PreferencesHelper.setUserName(this, name);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Por favor, digite seu nome", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
