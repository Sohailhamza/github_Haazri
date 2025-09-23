package com.example.onenew;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
                employee -> Toast.makeText(this, "Clicked: " + employee.getName(), Toast.LENGTH_SHORT).show());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchEmployees(); // yeh call
        
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchEmployees() {
        db.collection("employees")
//                .document("employee")
//                .collection("records")
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
                            String id         = doc.getId();
                            String name       = doc.getString("name");
                            String status     = doc.getString("status");
//                            String dutyStart  = doc.getString("inTime");
//                            String dutyOff    = doc.getString("outTime");
//                            String breakStart = doc.getString("breakStart");
//                            String breakEnd   = doc.getString("breakEnd");
                            String phone      = doc.getString("phone");
                            String address    = doc.getString("address");

                            Employee emp = new Employee(
                                    id,
                                    name != null ? name : "",
                                    status != null ? status : "—",
//                                    dutyStart != null ? dutyStart : "",
//                                    dutyOff != null ? dutyOff : "",
//                                    breakStart != null ? breakStart : "",
//                                    breakEnd != null ? breakEnd : "",
                                    phone != null ? phone : "",
                                    address != null ? address : "",
                                    R.drawable.ic_person
                            );
                            employeeList.add(emp);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

}
