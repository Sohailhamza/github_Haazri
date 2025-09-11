package com.example.onenew;

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

    private void loadEmployees() {
        FirebaseFirestore.getInstance().collection("employees")
                .get()
                .addOnSuccessListener(qs -> {
                    employees.clear();
                    for (DocumentSnapshot d : qs) {
                        String id   = d.getId();
                        String name = d.getString("name");
                        employees.add(new Employee(id, name, "", "", "", "", "", 0));
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
