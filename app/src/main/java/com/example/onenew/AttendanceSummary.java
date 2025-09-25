package com.example.onenew;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceSummary extends AppCompatActivity {

    private TextView tvDate, tvPresentCount, tvAbsentCount,
            tvBreak, tvDuty, tvBreakStart, tvBreakEnd, tvIn, tvOut, tvName, tvId, tvStatus;
    private RecyclerView rvPresent, rvAbsent, rvBreak, rvDuty, rvBreakStart, rvBreakEnd, rvIn, rvOut, rvName, rvId, rvStatus;
    private FirebaseFirestore db;
    private final List<EmployeeAttendance> presentList = new ArrayList<>();
    private final List<EmployeeAttendance> absentList = new ArrayList<>();

    private AttendanceAdapter presentAdapter, absentAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_summary);

        tvDate = findViewById(R.id.tvDate);
        tvPresentCount = findViewById(R.id.tvPresentCount);
        tvAbsentCount = findViewById(R.id.tvAbsentCount);
        rvPresent = findViewById(R.id.rvPresent);
        rvAbsent = findViewById(R.id.rvAbsent);
        tvBreak = findViewById(R.id.tvBreak);
        tvDuty = findViewById(R.id.tvDuty);
        tvBreakStart = findViewById(R.id.tvBreakStart);
        tvBreakEnd = findViewById(R.id.tvBreakEnd);
        tvIn = findViewById(R.id.tvIn);
        tvOut = findViewById(R.id.tvOut);
        tvName = findViewById(R.id.tvName);
        tvId = findViewById(R.id.tvId);
        tvStatus = findViewById(R.id.tvStatus);


        rvPresent.setLayoutManager(new LinearLayoutManager(this));
        rvAbsent.setLayoutManager(new LinearLayoutManager(this));

        presentAdapter = new AttendanceAdapter(presentList);
        absentAdapter = new AttendanceAdapter(absentList);
        rvPresent.setAdapter(presentAdapter);
        rvAbsent.setAdapter(absentAdapter);

        db = FirebaseFirestore.getInstance();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvDate.setText("Attendance: " + today);

        loadSummary(today);

        tvDate.setOnClickListener(v -> {
            // current date ko default rakhein
            final Calendar cal = Calendar.getInstance();
            int year  = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day   = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dlg = new DatePickerDialog(
                    AttendanceSummary.this,
                    (view, y, m, d) -> {
                        // user ne jo date choose ki uska yyyy-MM-dd format banao
                        String chosen = String.format(Locale.getDefault(),
                                "%04d-%02d-%02d", y, m + 1, d);

                        tvDate.setText("Attendance: " + chosen);
                        loadSummary(chosen);   // 🔑 Firestore se nayi date ka data
                    },
                    year, month, day
            );
            dlg.show();
        });

    }



    private void loadSummary(String dateKey) {
        // `attendance/dateKey/employees` collection structure
        db.collection("attendance").document(dateKey)
                .collection("records")
                .addSnapshotListener((snap, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Listen failed: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    presentList.clear();
                    absentList.clear();


                    if (snap != null) {
                        for (QueryDocumentSnapshot doc : snap) {
                            EmployeeAttendance ea = doc.toObject(EmployeeAttendance.class);
                            if ("CheckedOut".equalsIgnoreCase(ea.status) ||
                                    "Present".equalsIgnoreCase(ea.status)) {
                                presentList.add(ea);
                            } else {
                                absentList.add(ea);
                            }
                        }
                        tvPresentCount.setText("Present: " + presentList.size());
                        tvAbsentCount.setText("Absent: " + absentList.size());

                        presentAdapter.notifyDataSetChanged();
                        absentAdapter.notifyDataSetChanged();
                    }
                });
    }
}
