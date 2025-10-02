package com.example.onenew.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.onenew.AddEmployeeDialog;
import com.example.onenew.AttendanceSummary;
import com.example.onenew.LoginActivity;
import com.example.onenew.R;
import com.example.onenew.admin.remove.RemoveEmployee;
import com.example.onenew.reports.AttendanceReportActivity;
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
        CardView cardViewAttendanceReport = findViewById(R.id.cardViewAttendanceReport);

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
        // ➤ View attendance report
        cardViewAttendanceReport.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboard.this, AttendanceReportActivity.class));
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
