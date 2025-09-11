package com.example.onenew;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;


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
        CardView cardLogout = findViewById(R.id.cardLogout);
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



        cardLogout.setOnClickListener(v -> {
            // sign out + move to login screen
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(AdminDashboard.this, LoginActivity.class));
            finish();
        });


    }
}
