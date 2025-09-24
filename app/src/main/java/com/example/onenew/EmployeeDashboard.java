package com.example.onenew;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Employee Dashboard with:
 * 1. Location check at End-Break
 * 2. Automatic checkout 11:58 PM
 * 3. Single Check-In/Out per day
 */
public class EmployeeDashboard extends AppCompatActivity {

    private static final int PERMISSION_CAMERA = 200;
    private static final int PERMISSION_LOCATION = 201;

    private TextView tvName, tvEmpId, tvDutyHour, tvBreakDuration,
            tvCheckInBtn, btnCheckOut, btnStartBreak, btnEndBreak,
            tvDate, tvCheckInTime, tvCheckOutTime, tvBreakStartTime, tvBreakEndTime;

    private AlertDialog dialog;
    private ImageView dialogImagePreview;
    private Button btnSave;
    private Bitmap capturedImage;

    private long checkInTime = 0L;
    private long breakStartTime = 0L;
    private long totalBreakMillis = 0L;

    private SharedPreferences prefs;
    private FusedLocationProviderClient fusedClient;

    private static final double[][] OFFICES = {
            {30.6484947, 73.1070595},
            {30.8049604, 73.4380137},
            {30.6703321, 73.1276480},
            {30.8768576, 73.5926216}
    };
    private static final float ALLOWED_RADIUS_METERS = 1000f;

    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(),
                    bitmap -> {
                        if (bitmap != null) {
                            capturedImage = bitmap;
                            if (dialogImagePreview != null) {
                                dialogImagePreview.setImageBitmap(bitmap);
                                if (btnSave != null) btnSave.setEnabled(true);
                            }
                        } else {
                            Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        prefs = getSharedPreferences("attendance_prefs", MODE_PRIVATE);
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        bindViews();
        showToday();
        restoreSavedState();
        checkFirestoreForToday();      // ensure 1 check-in/out per day
        scheduleAutoCheckout();        // 11:58 PM auto-checkout
        setClickListeners();
    }

    private void bindViews() {
        tvName = findViewById(R.id.tvName);
        tvEmpId = findViewById(R.id.tvEmpId);
        tvDutyHour = findViewById(R.id.tvDHour);
        tvBreakDuration = findViewById(R.id.tvBreakDuration);
        tvCheckInBtn = findViewById(R.id.tvCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        btnStartBreak = findViewById(R.id.btnStartBreak);
        btnEndBreak = findViewById(R.id.btnEndBreak);
        tvDate = findViewById(R.id.tvDate);
        tvCheckInTime = findViewById(R.id.tvCheckInTime);
        tvCheckOutTime = findViewById(R.id.tvCheckOutTime);
        tvBreakStartTime = findViewById(R.id.tvBreakStartTime);
        tvBreakEndTime = findViewById(R.id.tvBreakEndTime);

        String empId = getIntent().getStringExtra("empId");
        String empName = getIntent().getStringExtra("empName");
        tvEmpId.setText(empId != null ? empId : "ID");
        tvName.setText(empName != null ? empName : "Employee");
    }

    private void showToday() {
        String today = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date());
        tvDate.setText(today);
    }

    @SuppressLint("SetTextI18n")
    private void restoreSavedState() {
        checkInTime = prefs.getLong("checkInTime", 0L);
        breakStartTime = prefs.getLong("breakStartTime", 0L);
        totalBreakMillis = prefs.getLong("totalBreakMillis", 0L);

        if (checkInTime != 0)
            tvCheckInTime.setText("Check-In Time: " + formatTime(checkInTime));
        if (breakStartTime != 0)
            tvBreakStartTime.setText("Break-Start Time: " + formatTime(breakStartTime));
    }

    private void setClickListeners() {
        tvCheckInBtn.setOnClickListener(v -> attemptAction(this::handleCheckIn));
        btnCheckOut.setOnClickListener(v -> attemptAction(this::handleCheckOut));
        btnStartBreak.setOnClickListener(v -> attemptAction(this::handleStartBreak));
        // End break now requires location check again
        btnEndBreak.setOnClickListener(v -> attemptAction(this::handleEndBreak));
    }

    /**
     * Ensure single check-in/out per day
     */
    private void checkFirestoreForToday() {
        String id = tvEmpId.getText().toString();
        if (id.isEmpty()) return;
        String todayId = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseFirestore.getInstance()
                .collection("attendance")
                .document(todayId)
                .collection("records")
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        if (doc.contains("checkInTime")) tvCheckInBtn.setEnabled(false);
                        if (doc.contains("checkOutTime")) btnCheckOut.setEnabled(false);
                    }
                });
    }

    /**
     * Schedules automatic checkout at 11:58 PM
     */
    private void scheduleAutoCheckout() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 58);
        cal.set(Calendar.SECOND, 0);
        if (cal.getTimeInMillis() < System.currentTimeMillis()) {
            // already past for today; schedule for tomorrow
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        long trigger = cal.getTimeInMillis();
        long delay = trigger - System.currentTimeMillis();

        new Handler(getMainLooper()).postDelayed(this::autoCheckout, delay);
    }

    /**
     * Automatic checkout logic
     */
    private void autoCheckout() {
        if (checkInTime == 0) return; // no active duty

        // End any running break first
        if (breakStartTime != 0) {
            long end = System.currentTimeMillis();
            totalBreakMillis += end - breakStartTime;
            breakStartTime = 0;
        }
        handleCheckOut();
        Toast.makeText(this, "Auto checked out at 11:58 PM", Toast.LENGTH_LONG).show();
    }

    /* ---------- Attendance Methods ---------- */

    private void handleCheckIn() {
        if (checkInTime != 0) {
            toast("Already checked in");
            return;
        }

        showCaptureDialog(() -> {
            checkInTime = System.currentTimeMillis();
            prefs.edit().putLong("checkInTime", checkInTime).apply();

            tvCheckInTime.setText("Check-In Time: " + formatTime(checkInTime));

            String id = tvEmpId.getText().toString();
            if (id.isEmpty()) {
                toast("Employee ID missing!");
                return;
            }

            String todayId = todayDoc();
            Map<String, Object> data = new HashMap<>();
            data.put("employeeId", id);
            data.put("name", tvName.getText().toString());
            data.put("status", "Present");
            data.put("checkInTime", timeForDb(checkInTime));

            FirebaseFirestore.getInstance()
                    .collection("attendance").document(todayId)
                    .collection("records").document(id)
                    .set(data)
                    .addOnSuccessListener(a -> toast("Attendance saved"))
                    .addOnFailureListener(e -> toast("Error: " + e.getMessage()));
        });
    }

    private void handleCheckOut() {
        if (checkInTime == 0) {
            toast("Check in first");
            return;
        }

        long now = System.currentTimeMillis();
        long dutyMillis = now - checkInTime - totalBreakMillis;

        tvDutyHour.setText("Duty Hour: " + formatMillis(dutyMillis));
        tvBreakDuration.setText("Break Duration: " + formatMillis(totalBreakMillis));
        tvCheckOutTime.setText("Check-Out Time: " + formatTime(now));

        String id = tvEmpId.getText().toString();
        if (id.isEmpty()) return;

        Map<String, Object> update = new HashMap<>();
        update.put("checkOutTime", timeForDb(now));
        update.put("dutyMillis", dutyMillis);
        update.put("breakMillis", totalBreakMillis);
        update.put("status", "CheckedOut");

        FirebaseFirestore.getInstance()
                .collection("attendance").document(todayDoc())
                .collection("records").document(id)
                .update(update)
                .addOnFailureListener(e -> toast("Firestore Update Error: " + e.getMessage()));

        // Reset local state
        prefs.edit().clear().apply();
        checkInTime = breakStartTime = totalBreakMillis = 0L;
        tvCheckInBtn.setEnabled(false);
        btnCheckOut.setEnabled(false);
    }

    private void handleStartBreak() {
        if (checkInTime == 0) {
            toast("Check in first");
            return;
        }
        if (breakStartTime != 0) {
            toast("Already on break");
            return;
        }

        breakStartTime = System.currentTimeMillis();
        prefs.edit().putLong("breakStartTime", breakStartTime).apply();

        tvBreakStartTime.setText("Break-Start Time: " + formatTime(breakStartTime));
        toast("Break Started");
    }

    /**
     * End break with extra location check
     */
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void handleEndBreak() {
        if (breakStartTime == 0) {
            toast("No active break");
            return;
        }

        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(loc -> {
                    if (loc == null) {
                        toast("Location unavailable");
                        return;
                    }

                    boolean inside = false;
                    for (double[] o : OFFICES) {
                        float[] dist = new float[1];
                        Location.distanceBetween(
                                loc.getLatitude(), loc.getLongitude(), o[0], o[1], dist);
                        if (dist[0] <= ALLOWED_RADIUS_METERS) {
                            inside = true;
                            break;
                        }
                    }
                    if (!inside) {
                        toast("Not in allowed location");
                        return;
                    }

                    long end = System.currentTimeMillis();
                    totalBreakMillis += end - breakStartTime;
                    prefs.edit()
                            .putLong("totalBreakMillis", totalBreakMillis)
                            .putLong("breakStartTime", 0L)
                            .apply();

                    tvBreakEndTime.setText("Break-End Time: " + formatTime(end));
                    breakStartTime = 0;

                    String id = tvEmpId.getText().toString();
                    if (!id.isEmpty()) {
                        FirebaseFirestore.getInstance()
                                .collection("attendance").document(todayDoc())
                                .collection("records").document(id)
                                .update("breakEndTime", timeForDb(end),
                                        "breakMillis", totalBreakMillis)
                                .addOnFailureListener(e -> toast("Failed to update break end"));
                    }
                    toast("Break Ended");
                })
                .addOnFailureListener(e -> toast("Location error: " + e.getMessage()));
    }

    /* ---------- Helpers ---------- */

    private String todayDoc() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String timeForDb(long ms) {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(ms));
    }

    private String formatTime(long ms) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(ms));
    }

    private String formatMillis(long ms) {
        long h = ms / (1000 * 60 * 60);
        long m = (ms / (1000 * 60)) % 60;
        return h + "h " + m + "m";
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void showCaptureDialog(Runnable onSuccess) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_capture, null);
        b.setView(v);
        dialogImagePreview = v.findViewById(R.id.imagePreview);
        Button btnCapture = v.findViewById(R.id.btnCapture);
        btnSave = v.findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        dialog = b.create();
        dialog.show();

        btnCapture.setOnClickListener(x -> {
            if (checkCameraPermission()) cameraLauncher.launch(null);
        });
        btnSave.setOnClickListener(x -> {
            if (capturedImage != null) {
                dialog.dismiss();
                onSuccess.run();
            } else toast("Capture a selfie first!");
        });
    }

    private void attemptAction(Runnable action) {
        if (!checkLocationPermission()) return;
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(loc -> {
                    if (loc == null) {
                        toast("Location unavailable");
                        return;
                    }
                    boolean inside = false;
                    for (double[] o : OFFICES) {
                        float[] dist = new float[1];
                        android.location.Location.distanceBetween(
                                loc.getLatitude(), loc.getLongitude(), o[0], o[1], dist);
                        if (dist[0] <= ALLOWED_RADIUS_METERS) {
                            inside = true;
                            break;
                        }
                    }
                    if (inside) action.run();
                    else toast("You are not at an allowed location!");
                })
                .addOnFailureListener(e -> toast("Location error: " + e.getMessage()));
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
            return false;
        }
        return true;
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == PERMISSION_CAMERA || requestCode == PERMISSION_LOCATION)
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            toast("Permission granted. Try again.");
        } else {
            toast("Permission denied.");
        }
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}
