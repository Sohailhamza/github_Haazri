package com.example.onenew;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceSummary extends AppCompatActivity {

    private TextView tvDate, tvPresentCount, tvAbsentCount;
    private RecyclerView rvPresent, rvAbsent;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_summary);

        tvDate        = findViewById(R.id.tvDate);
        tvPresentCount= findViewById(R.id.tvPresentCount);
        tvAbsentCount = findViewById(R.id.tvAbsentCount);
        rvPresent     = findViewById(R.id.rvPresent);
        rvAbsent      = findViewById(R.id.rvAbsent);

        rvPresent.setLayoutManager(new LinearLayoutManager(this));
        rvAbsent.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        // Today’s date as key
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvDate.setText("Attendance: " + today);

        loadSummary(today);
    }

    private void loadSummary(String dateKey) {
        db.collection("attendance").document(dateKey).get()
                .addOnSuccessListener(this::populateUI)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void populateUI(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "No attendance record for today", Toast.LENGTH_SHORT).show();
            return;
        }

        long present = doc.getLong("presentCount") != null ? doc.getLong("presentCount") : 0;
        long absent  = doc.getLong("absentCount")  != null ? doc.getLong("absentCount")  : 0;
        List<String> presentList = (List<String>) doc.get("presentList");
        List<String> absentList  = (List<String>) doc.get("absentList");

        tvPresentCount.setText("Present: " + present);
        tvAbsentCount.setText("Absent: " + absent);

        if (presentList == null) presentList = new ArrayList<>();
        if (absentList == null)  absentList  = new ArrayList<>();

        rvPresent.setAdapter(new SimpleStringAdapter(presentList));
        rvAbsent.setAdapter(new SimpleStringAdapter(absentList));
    }
}
