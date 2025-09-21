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

        adapter = new EmployeeAdapter(employeeList, employee ->
                Toast.makeText(this, "Clicked: " + employee.getName(), Toast.LENGTH_SHORT).show());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchEmployees();
    }

    private void fetchEmployees() {
        // employees collection in Firestore
        db.collection("employees")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        employeeList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            String id = doc.getId();
                            String name = doc.getString("name");
                            // Optional fields – add null checks if needed
                            String status = doc.getString("status"); // if you store attendance status
                            String inTime = doc.getString("inTime");
                            String outTime = doc.getString("outTime");
                            String breakStart = doc.getString("breakStart");
                            String breakEnd = doc.getString("breakEnd");

                            // Optional fields – add null checks if needed
                            String phone = doc.getString("phone");
                            String address = doc.getString("address");

                            // Provide defaults if a field might be missing
                            Employee emp = new Employee(
                                    id,
                                    name != null ? name : "",
                                    status != null ? status : "—",
                                    inTime != null ? inTime : "",
                                    outTime != null ? outTime : "",
                                    breakStart != null ? breakStart : "",
                                    breakEnd != null ? breakEnd : "",
                                    phone != null ? phone : "",
                                    address != null ? address : "",
                                    R.drawable.ic_person // Default photo

                            );
                            employeeList.add(emp);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EmployeeListActivity.this,
                                "Failed to load employees: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
