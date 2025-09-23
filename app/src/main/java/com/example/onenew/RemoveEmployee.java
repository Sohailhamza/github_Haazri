package com.example.onenew;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class RemoveEmployee extends AppCompatActivity {

    private final List<Employee> employees = new ArrayList<>();

    private RemoveEmployeeAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_employee);

        RecyclerView rv = findViewById(R.id.recyclerRemove);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RemoveEmployeeAdapter(employees);
        rv.setAdapter(adapter);

        loadEmployees();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadEmployees() {
        FirebaseFirestore.getInstance().collection("employees")
                .get()
                .addOnSuccessListener(qs -> {
                    employees.clear();
                    for (DocumentSnapshot d : qs) {
                        String id      = d.getId();
                        String name    = d.getString("name");
                        String status  = d.getString("status");   // optional
                        String address = d.getString("address");
                        String phone   = d.getString("phone");



                        employees.add(new Employee(
                                id,
                                name != null ? name : "",
                                status != null ? status : "—",
                                phone != null ? phone : "",
                                address != null ? address : "",

                                R.drawable.ic_person      // ya jo default photo aap use kar rahe hain
                        ));
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
