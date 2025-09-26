package com.example.onenew.reports;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onenew.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class AttendanceReportActivity extends AppCompatActivity {

    private Spinner spinnerEmployee;
    private Button btnStartDate, btnEndDate, btnGenerateReport;
    private TextView tvSummary;
    private RecyclerView recyclerViewReport;

    private final Calendar startDate = Calendar.getInstance();
    private final Calendar endDate   = Calendar.getInstance();

    private final List<AttendanceRecord> recordList = new ArrayList<>();
    private AttendanceReportAdapter adapter;

    private FirebaseFirestore firestore;
    private final List<String> employees = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);

        spinnerEmployee   = findViewById(R.id.spinnerEmployee);
        btnStartDate      = findViewById(R.id.btnStartDate);
        btnEndDate        = findViewById(R.id.btnEndDate);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        tvSummary         = findViewById(R.id.tvSummary);
        recyclerViewReport= findViewById(R.id.recyclerViewReport);

        recyclerViewReport.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceReportAdapter(recordList);
        recyclerViewReport.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        // spinner adapter
        spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, employees);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmployee.setAdapter(spinnerAdapter);

        loadEmployees();        // 🔑 Load employee IDs here

        btnStartDate.setOnClickListener(v -> pickDate(startDate, btnStartDate));
        btnEndDate.setOnClickListener(v -> pickDate(endDate, btnEndDate));
        btnGenerateReport.setOnClickListener(v -> generateReport());
    }

    private void loadEmployees() {
        firestore.collection("employees")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    employees.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        // prefer a field "employeeId", fallback to doc id
                        String empId = doc.getString("employeeId");
                        if (empId == null || empId.trim().isEmpty()) {
                            empId = doc.getId();
                        }
                        employees.add(empId);
                    }
                    if (employees.isEmpty()) {
                        Toast.makeText(this, "No employees found", Toast.LENGTH_SHORT).show();
                    }
                    spinnerAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading employees: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void pickDate(Calendar date, Button btn) {
        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    date.set(year, month, day);
                    btn.setText(day + "/" + (month + 1) + "/" + year);
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void generateReport() {
        if (employees.isEmpty()) {
            Toast.makeText(this, "No employees loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerEmployee.getSelectedItem() == null) {
            Toast.makeText(this, "Select an employee", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedEmployee = spinnerEmployee.getSelectedItem().toString();
        recordList.clear();
        adapter.notifyDataSetChanged();

        final long TWELVE_HOURS_MS = 12 * 60 * 60 * 1000;

        // ✅ Use your actual collection name (lower-case)
        firestore.collection("attendance")
                .whereEqualTo("employeeId", selectedEmployee)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    int presentDays = 0;
                    int leaveDays   = 0;
                    long totalOver  = 0;
                    long totalUnder = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        AttendanceRecord rec = doc.toObject(AttendanceRecord.class);
                        if (rec == null || rec.date == null || rec.date.trim().isEmpty()) continue;

                        // Parse yyyy-MM-dd safely
                        String[] parts = rec.date.split("-");
                        if (parts.length != 3) continue;
                        Calendar recCal = Calendar.getInstance();
                        try {
                            recCal.set(
                                    Integer.parseInt(parts[0]),
                                    Integer.parseInt(parts[1]) - 1,
                                    Integer.parseInt(parts[2])
                            );
                        } catch (NumberFormatException e) {
                            continue;
                        }

                        // Filter by selected range (inclusive)
                        if (recCal.before(startDate) || recCal.after(endDate)) continue;

                        recordList.add(rec);

                        if ("CheckedOut".equalsIgnoreCase(rec.status)) {
                            presentDays++;

                            long worked = rec.dutyMillis - rec.breakMillis;
                            if (worked > TWELVE_HOURS_MS) {
                                totalOver += (worked - TWELVE_HOURS_MS);
                            } else if (worked < TWELVE_HOURS_MS) {
                                totalUnder += (TWELVE_HOURS_MS - worked);
                            }
                        } else {
                            leaveDays++;
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (recordList.isEmpty()) {
                        tvSummary.setText("No attendance records in selected range.");
                        return;
                    }

                    String summary =
                            "Present Days : " + presentDays + "\n" +
                                    "Leave Days   : " + leaveDays + "\n" +
                                    "Total Overtime : " + TimeUnit.MILLISECONDS.toHours(totalOver) + "h "
                                    + (TimeUnit.MILLISECONDS.toMinutes(totalOver) % 60) + "m\n" +
                                    "Total Shortfall: " + TimeUnit.MILLISECONDS.toHours(totalUnder) + "h "
                                    + (TimeUnit.MILLISECONDS.toMinutes(totalUnder) % 60) + "m";

                    tvSummary.setText(summary);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error fetching report: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

}
