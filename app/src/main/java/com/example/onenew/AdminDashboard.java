package com.example.onenew;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminDashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        CardView cardAddEmployee = findViewById(R.id.cardAddEmployee);
        CardView cardRemoveEmployee = findViewById(R.id.cardRemoveEmployee);
        CardView cardEmployeeList = findViewById(R.id.cardEmployeeList);

        cardAddEmployee.setOnClickListener(v -> {
            AddEmployeeDialog dialog = new AddEmployeeDialog();
            dialog.show(getSupportFragmentManager(), "AddEmployeeDialog");
        });

        cardRemoveEmployee.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, RemoveEmployee.class);
            startActivity(intent);
        });

        cardEmployeeList.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, EmployeeListActivity.class);
            startActivity(intent);
        });

    }
}