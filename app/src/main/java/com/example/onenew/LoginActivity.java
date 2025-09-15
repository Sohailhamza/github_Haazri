package com.example.onenew;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);   // your XML file name

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);

        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> doLogin(

        ));
    }

    private void doLogin() {
        String id   = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Enter ID and Password", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1️⃣ Check in Admins collection
        db.collection("admins").document(id).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        checkPassword(doc, pass, true);
                    } else {
                        // 2️⃣ Else check in Employees collection
                        db.collection("employees").document(id).get()
                                .addOnSuccessListener(empDoc -> {
                                    if (empDoc.exists()) {
                                        checkPassword(empDoc, pass, false);
                                    } else {
                                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(this::showError);
                    }
                })
                .addOnFailureListener(this::showError);
    }

    private void checkPassword(DocumentSnapshot doc, String entered, boolean isAdmin) {
        String savedPass = doc.getString("password");
        if (savedPass != null && savedPass.equals(entered)) {
            Toast.makeText(this,
                    isAdmin ? "Admin Login Successful" : "Employee Login Successful",
                    Toast.LENGTH_SHORT).show();

            Intent intent;
            if (isAdmin) {
                intent = new Intent(this, AdminDashboard.class);
            } else {
                intent = new Intent(this, EmployeeDashboard.class);
                intent.putExtra("empId", doc.getId());
                intent.putExtra("empName", doc.getString("name"));
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(Exception e) {
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
