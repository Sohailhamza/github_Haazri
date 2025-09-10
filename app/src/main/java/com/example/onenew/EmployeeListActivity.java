package com.example.onenew;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EmployeeListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EmployeeAdapter adapter;
    List<Employee> employeeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);

        recyclerView = findViewById(R.id.recyclerEmployees);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Dummy employees (baad me DB/Firebase se load kar lena)
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("101", "Ali", "Present", "9:00 AM", "6:00 PM", "1:00 PM", "1:30 PM", R.drawable.ic_person));
        employees.add(new Employee("102", "Ahmed", "Absent", "9:00 AM", "6:00 PM", "1:00 PM", "1:30 PM", R.drawable.ic_person));

        EmployeeAdapter adapter = new EmployeeAdapter(employees, employee -> {
            Toast.makeText(this, "Clicked: " + employee.getName(), Toast.LENGTH_SHORT).show();
        });


        recyclerView.setAdapter(adapter); 
    }
}
