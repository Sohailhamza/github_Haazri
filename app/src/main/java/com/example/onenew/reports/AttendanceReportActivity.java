package com.example.onenew.reports;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onenew.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceReportActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AttendanceReportAdapter adapter;
    private List<AttendanceRecord> recordList;
    private FirebaseFirestore firestore;

    private Spinner spinnerEmployee;
    private Button btnStartDate, btnEndDate, btnGenerateReport;
    private TextView tvSummary;

    private List<String> employeeIds;
    private String selectedStartDate = null;
    private String selectedEndDate = null;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);

        recyclerView = findViewById(R.id.recyclerViewReport);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        spinnerEmployee = findViewById(R.id.spinnerEmployee);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        tvSummary = findViewById(R.id.tvSummary);

        recordList = new ArrayList<>();
        adapter = new AttendanceReportAdapter(recordList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        employeeIds = new ArrayList<>();

        loadEmployees();

        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));

        btnGenerateReport.setOnClickListener(v -> {
            int selectedPos = spinnerEmployee.getSelectedItemPosition();
            if (selectedPos >= 0 && selectedPos < employeeIds.size()) {
                String empId = employeeIds.get(selectedPos);
                loadAttendanceData(empId);
            }
        });
    }

    private void loadEmployees() {
        firestore.collection("employees")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        employeeIds.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            employeeIds.add(doc.getId());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, employeeIds);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerEmployee.setAdapter(adapter);
                    } else {
                        Log.e("Firestore", "Error loading employees", task.getException());
                    }
                });
    }

    private void showDatePicker(boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String date = year + "-" + String.format(Locale.getDefault(), "%02d", month + 1)
                            + "-" + String.format(Locale.getDefault(), "%02d", dayOfMonth);
                    if (isStart) {
                        selectedStartDate = date;
                        btnStartDate.setText("Start: " + date);
                    } else {
                        selectedEndDate = date;
                        btnEndDate.setText("End: " + date);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void loadAttendanceData(String empId) {
        firestore.collection("attendance")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        recordList.clear();
                        final int[] presentCount = {0};
                        final int[] absentCount = {0};

                        List<DocumentSnapshot> dateDocs = task.getResult().getDocuments();
                        if (dateDocs.isEmpty()) {
                            adapter.notifyDataSetChanged();
                            tvSummary.setText("Summary: Present = 0, Absent = 0");
                            return;
                        }

                        final int[] processedCount = {0};
                        int totalDates = dateDocs.size();

                        for (DocumentSnapshot dateDoc : dateDocs) {
                            String dateId = dateDoc.getId();

                            dateDoc.getReference().collection("records").document(empId)
                                    .get()
                                    .addOnSuccessListener(empDoc -> {
                                        processedCount[0]++;

                                        String status = "Absent"; // default
                                        String dutyHours = "00:00";
                                        String overtime = "0:00";
                                        String shortTime = "0:00";

                                        if (empDoc.exists()) {
                                            status = empDoc.getString("status");
                                            long dutyMillis = empDoc.getLong("dutyMillis") != null ? empDoc.getLong("dutyMillis") : 0;

                                            int hours = (int) (dutyMillis / (1000 * 60 * 60));
                                            int minutes = (int) ((dutyMillis / (1000 * 60)) % 60);
                                            dutyHours = String.format("%02d:%02d", hours, minutes);

                                            String[] times = calculateOvertimeAndShorttime(hours, minutes);
                                            overtime = times[0];
                                            shortTime = times[1];
                                        }

                                        if (isWithinDateRange(dateId)) {
                                            AttendanceRecord record = new AttendanceRecord(dateId, status, dutyHours, overtime, shortTime);
                                            recordList.add(record);

                                            if ("CheckedIn".equalsIgnoreCase(status) || "CheckedOut".equalsIgnoreCase(status) || "Present".equalsIgnoreCase(status))
                                                presentCount[0]++;
                                            else if ("Absent".equalsIgnoreCase(status))
                                                absentCount[0]++;
                                        }

                                        if (processedCount[0] == totalDates) {
                                            adapter.notifyDataSetChanged();
                                            tvSummary.setText("Summary: Present = " + presentCount[0] + ", Absent = " + absentCount[0]);
                                        }
                                    });
                        }

                    } else {
                        Log.e("Firestore", "Error loading attendance", task.getException());
                    }
                });
    }

    private String[] calculateOvertimeAndShorttime(int hours, int minutes) {
        String[] result = {"0:00", "0:00"};
        int totalMin = hours * 60 + minutes;
        int standardMin = 12 * 60;

        if (totalMin > standardMin) {
            int extra = totalMin - standardMin;
            result[0] = (extra / 60) + ":" + String.format("%02d", extra % 60);
        } else if (totalMin < standardMin) {
            int shortTime = standardMin - totalMin;
            result[1] = (shortTime / 60) + ":" + String.format("%02d", shortTime % 60);
        }
        return result;
    }

    private boolean isWithinDateRange(String dateStr) {
        if (selectedStartDate == null || selectedEndDate == null) return true;
        try {
            Date date = sdf.parse(dateStr);
            Date start = sdf.parse(selectedStartDate);
            Date end = sdf.parse(selectedEndDate);
            return date != null && !date.before(start) && !date.after(end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}
