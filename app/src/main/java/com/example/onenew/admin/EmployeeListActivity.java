package com.example.onenew.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;

import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.onenew.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EmployeeListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private final List<Employee> employeeList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);

        recyclerView = findViewById(R.id.recyclerEmployees);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EmployeeAdapter(employeeList,
                employee -> Toast.makeText(this,
                        "Clicked: " + employee.getName(),
                        Toast.LENGTH_SHORT).show());
        recyclerView.setAdapter(adapter);

        // 🔎 SearchView
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        db = FirebaseFirestore.getInstance();
        fetchEmployees();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchEmployees() {
        db.collection("employees")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(EmployeeListActivity.this,
                                "Listen failed: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (querySnapshot != null) {
                        employeeList.clear();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            String id       = doc.getId();
                            String name     = doc.getString("name");
                            String password = doc.getString("password");
                            String phone    = doc.getString("phone");
                            String address  = doc.getString("address");
                            String photoUrl = doc.getString("photo"); // 🔄 Get photo URL

                            Employee emp = new Employee(
                                    id,
                                    name != null ? name : "",
                                    password != null ? password : "",
                                    phone != null ? phone : "",
                                    address != null ? address : "",
                                    photoUrl != null ? photoUrl : ""

                            );
                            employeeList.add(emp);
                        }

                        // 🔄 Update adapter lists for search + UI
                        adapter.updateFullList(new ArrayList<>(employeeList));
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
