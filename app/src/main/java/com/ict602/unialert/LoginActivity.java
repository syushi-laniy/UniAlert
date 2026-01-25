package com.ict602.unialert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.textfield.TextInputLayout;


import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextInputEditText etName, etEmail, etPassword;
    private Button btnLogin, btnRegister;

    private TextInputLayout tilName;

    private TextView tvToggle;
    private boolean isRegisterMode = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, MapActivity.class));
            finish();
            return;
        }

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tilName = findViewById(R.id.tilName);


        btnRegister.setOnClickListener(v -> {
            // Tunjuk Full Name bila nak register
            tilName.setVisibility(View.VISIBLE);
            register();
        });

        btnLogin.setOnClickListener(v -> {
            // Sembunyi Full Name bila login
            tilName.setVisibility(View.GONE);
            login();
        });

        tvToggle = findViewById(R.id.tvToggle);

        tvToggle.setOnClickListener(v -> {
            isRegisterMode = !isRegisterMode;

            if (isRegisterMode) {
                tilName.setVisibility(View.VISIBLE);
                btnRegister.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.GONE);
                tvToggle.setText("Already have an account? Login");
            } else {
                tilName.setVisibility(View.GONE);
                btnRegister.setVisibility(View.GONE);
                btnLogin.setVisibility(View.VISIBLE);
                tvToggle.setText("Don't have an account? Register");
            }
        });

        btnRegister.setVisibility(View.GONE);



    }

    private void register() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String pass = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty() || pass.length() < 6) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Fill name/email & password min 6 chars", Snackbar.LENGTH_LONG).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(res -> {
                    String uid = res.getUser() != null ? res.getUser().getUid() : null;
                    if (uid == null) return;

                    Map<String, Object> userDoc = new HashMap<>();
                    userDoc.put("fullName", name);
                    userDoc.put("email", email);
                    userDoc.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("users").document(uid).set(userDoc)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Registered!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MapActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Register failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void login() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String pass = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (email.isEmpty() || pass.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Enter email & password", Snackbar.LENGTH_LONG).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(res -> {
                    Toast.makeText(this, "Login success", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MapActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
