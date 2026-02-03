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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextInputEditText etName, etEmail, etPassword;
    private TextInputLayout tilName;

    private Button btnLogin, btnRegister;
    private TextView tvToggle;

    private boolean isRegisterMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // If already logged in -> go map
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, MapActivity.class));
            finish();
            return;
        }

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tilName = findViewById(R.id.tilName);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvToggle = findViewById(R.id.tvToggle);

        // default mode = Login
        setMode(false);

        tvToggle.setOnClickListener(v -> {
            isRegisterMode = !isRegisterMode;
            setMode(isRegisterMode);
        });

        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> register());
    }

    private void setMode(boolean registerMode) {
        if (registerMode) {
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
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnRegister.setEnabled(!loading);
        tvToggle.setEnabled(!loading);
    }

    private void register() {
        String name = (etName.getText() != null) ? etName.getText().toString().trim() : "";
        String email = (etEmail.getText() != null) ? etEmail.getText().toString().trim() : "";
        String pass = (etPassword.getText() != null) ? etPassword.getText().toString().trim() : "";

        if (name.isEmpty() || email.isEmpty() || pass.length() < 6) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Fill name/email & password min 6 chars", Snackbar.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(res -> {
                    if (res.getUser() == null) {
                        setLoading(false);
                        Toast.makeText(this, "Register failed (no user)", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String uid = res.getUser().getUid();

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
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Register failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void login() {
        String email = (etEmail.getText() != null) ? etEmail.getText().toString().trim() : "";
        String pass = (etPassword.getText() != null) ? etPassword.getText().toString().trim() : "";

        if (email.isEmpty() || pass.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Enter email & password", Snackbar.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(res -> {
                    Toast.makeText(this, "Login success", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MapActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}