package com.example.onenew;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        CardView cardAddEmployee    = findViewById(R.id.cardAddEmployee);
        CardView cardRemoveEmployee = findViewById(R.id.cardRemoveEmployee);
        CardView cardEmployeeList   = findViewById(R.id.cardEmployeeList);
        CardView cardViewAttendance = findViewById(R.id.cardViewAttendance);
        CardView cardLogout         = findViewById(R.id.cardLogout);

        // ➤ Add-Employee dialog
        cardAddEmployee.setOnClickListener(v -> {
            AddEmployeeDialog dialog = new AddEmployeeDialog();
            dialog.show(getSupportFragmentManager(), "AddEmployeeDialog");
        });

        // ➤ Remove employee activity
        cardRemoveEmployee.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, RemoveEmployee.class);
            startActivity(intent);
        });

        // ➤ View employee list
        cardEmployeeList.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboard.this, EmployeeListActivity.class));
        });

        // ➤ View attendance
        cardViewAttendance.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboard.this, AttendanceSummary.class));
        });

        // ➤ Logout
        cardLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(AdminDashboard.this, LoginActivity.class));
            finish();
        });
    }

    // ⬇️ Back button press pe app ko background me bhejne ke liye
    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        finishAffinity();
    }
}
