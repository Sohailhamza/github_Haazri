package com.example.onenew;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmployeeDashboard extends AppCompatActivity {

    private static final int PERMISSION_CAMERA   = 200;
    private static final int PERMISSION_LOCATION = 201;

    // === UI ===
    private TextView tvName, tvEmpId, tvDutyHour, tvBreakDuration,
            tvCheckInBtn, btnCheckOut, btnStartBreak, btnEndBreak,
            tvDate, tvCheckInTime, tvCheckOutTime, tvBreakStartTime, tvBreakEndTime;
    private AlertDialog dialog;
    private ImageView dialogImagePreview;
    private Button btnSave;
    private Bitmap capturedImage;

    // === Time tracking ===
    private long checkInTime = 0L;
    private long breakStartTime = 0L;
    private long totalBreakMillis = 0L;

    private SharedPreferences prefs;
    private FusedLocationProviderClient fusedClient;

    // Allowed office locations
    private static final double[][] OFFICES = {
            {30.6484947, 73.1070595},  // Office 1
            {30.8049604, 73.4380137},  // Home Mart Okara
            {30.6703321, 73.1276480},  // TBZ
            {30.8768576, 73.5926216}   // Home Mart Renala
    };
    private static final float ALLOWED_RADIUS_METERS = 1000f;

    /**
     * Camera launcher returning a Bitmap thumbnail
     */
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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        prefs = getSharedPreferences("attendance_prefs", MODE_PRIVATE);
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        // ---- Bind Views ----
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

        // ---- Show name/ID from Login ----
        String empId   = getIntent().getStringExtra("empId");   // ✅ one consistent key
        String empName = getIntent().getStringExtra("empName");
        tvName.setText(empName != null ? empName : "Employee");
        tvEmpId.setText(empId != null ? empId : "ID");

        // ---- Show today's date ----
        String today = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date());
        tvDate.setText(today);

        // Restore saved times if app reopens
        restoreSavedState();

        // ---- Click Listeners ----
        tvCheckInBtn.setOnClickListener(v -> attemptAction(this::handleCheckIn));
        btnCheckOut.setOnClickListener(v -> attemptAction(this::handleCheckOut));
        btnStartBreak.setOnClickListener(v -> attemptAction(this::handleStartBreak));
        btnEndBreak.setOnClickListener(v -> attemptAction(this::handleEndBreak));
    }

    /** Restore saved times when app reopens */
    @SuppressLint("SetTextI18n")
    private void restoreSavedState() {
        checkInTime    = prefs.getLong("checkInTime", 0L);
        totalBreakMillis = prefs.getLong("totalBreakMillis", 0L);
        breakStartTime = prefs.getLong("breakStartTime", 0L);

        if (checkInTime != 0L) {
            tvCheckInTime.setText("Check-In Time: " +
                    new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(checkInTime)));
        }
        if (breakStartTime != 0L) {
            tvBreakStartTime.setText("Break-Start Time: " +
                    new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(breakStartTime)));
        }
    }

    /*--------------------------------------------------
     *   Attendance Logic
     *--------------------------------------------------*/
    @SuppressLint("SetTextI18n")
    private void handleCheckIn() {
        if (checkInTime != 0) {
            Toast.makeText(this, "Already checked in", Toast.LENGTH_SHORT).show();
            return;
        }

        showCaptureDialog(() -> {
            checkInTime = System.currentTimeMillis();
            prefs.edit().putLong("checkInTime", checkInTime).apply();

            tvCheckInTime.setText("Check-In Time: " +
                    new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(checkInTime)));

            String id = tvEmpId.getText().toString();
            if (id.isEmpty()) {
                Toast.makeText(this, "Employee ID missing!", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String todayId = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            Map<String, Object> checkInData = new HashMap<>();
            checkInData.put("employeeId", id);
            checkInData.put("name", tvName.getText().toString());
            checkInData.put("status", "Present");
            checkInData.put("checkInTime",
                    new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(checkInTime)));

            db.collection("attendance")
                    .document(todayId)
                    .collection("records")
                    .document(id)
                    .set(checkInData)
                    .addOnSuccessListener(a -> Toast.makeText(this, "Attendance saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    @SuppressLint("SetTextI18n")
    private void handleCheckOut() {
        if (checkInTime == 0) {
            Toast.makeText(this, "Check in first", Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        long dutyMillis = now - checkInTime - totalBreakMillis;

        tvDutyHour.setText("Duty Hour: " + formatMillis(dutyMillis));
        tvBreakDuration.setText("Break Duration: " + formatMillis(totalBreakMillis));
        tvCheckOutTime.setText("Check-Out Time: " +
                new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(now)));

        String id = tvEmpId.getText().toString();
        if (id.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String todayId = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Map<String, Object> update = new HashMap<>();
        update.put("checkOutTime", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(now)));
        update.put("dutyMillis", dutyMillis);
        update.put("breakMillis", totalBreakMillis);
        update.put("status", "CheckedOut");

        db.collection("attendance")
                .document(todayId)
                .collection("records")
                .document(id)
                .update(update)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firestore Update Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // reset stored values
        prefs.edit()
                .putLong("checkInTime", 0L)
                .putLong("totalBreakMillis", 0L)
                .putLong("breakStartTime", 0L)
                .apply();
        checkInTime = totalBreakMillis = breakStartTime = 0L;
    }

    private void handleStartBreak() {
        if (checkInTime == 0) {
            Toast.makeText(this, "Check in first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (breakStartTime != 0) {
            Toast.makeText(this, "Already on break", Toast.LENGTH_SHORT).show();
            return;
        }
        breakStartTime = System.currentTimeMillis();
        prefs.edit().putLong("breakStartTime", breakStartTime).apply();

        String id = tvEmpId.getText().toString();
        if (!id.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection("employees")
                    .document(id)
                    .update("breakStartTime", breakStartTime)
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to update break start", Toast.LENGTH_SHORT).show());
        }

        tvBreakStartTime.setText("Break-Start Time: " +
                new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(breakStartTime)));
        Toast.makeText(this, "Break Started", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    private void handleEndBreak() {
        if (breakStartTime == 0) {
            Toast.makeText(this, "No active break", Toast.LENGTH_SHORT).show();
            return;
        }
        long end = System.currentTimeMillis();
        totalBreakMillis += end - breakStartTime;
        prefs.edit()
                .putLong("totalBreakMillis", totalBreakMillis)
                .putLong("breakStartTime", 0L)
                .apply();

        String id = tvEmpId.getText().toString();
        if (!id.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection("employees")
                    .document(id)
                    .update("breakEndTime", end,
                            "breakMillis", totalBreakMillis)
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to update break end", Toast.LENGTH_SHORT).show());
        }

        tvBreakEndTime.setText("Break-End Time: " +
                new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(end)));
        breakStartTime = 0;
        Toast.makeText(this, "Break Ended", Toast.LENGTH_SHORT).show();
    }

    private String formatMillis(long ms) {
        long hrs = ms / (1000 * 60 * 60);
        long mins = (ms / (1000 * 60)) % 60;
        return hrs + "h " + mins + "m";
    }

    /*--------------------------------------------------
     *   Camera Selfie Dialog
     *--------------------------------------------------*/
    private void showCaptureDialog(Runnable onSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_capture, null);
        builder.setView(dialogView);

        dialogImagePreview = dialogView.findViewById(R.id.imagePreview);
        Button btnCapture = dialogView.findViewById(R.id.btnCapture);
        btnSave = dialogView.findViewById(R.id.btnSave);
        btnSave.setEnabled(false);

        dialog = builder.create();
        dialog.show();

        btnCapture.setOnClickListener(v -> {
            if (checkCameraPermission()) cameraLauncher.launch(null);
        });

        btnSave.setOnClickListener(v -> {
            if (capturedImage != null) {
                dialog.dismiss();
                onSuccess.run();
            } else {
                Toast.makeText(this, "Capture a selfie first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*--------------------------------------------------
     *   Permissions & Location
     *--------------------------------------------------*/
    private void attemptAction(Runnable action) {
        if (!checkLocationPermission()) return;

        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(loc -> {
                    if (loc == null) {
                        Toast.makeText(this, "Location unavailable. Try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean insideAllowed = false;
                    for (double[] o : OFFICES) {
                        float[] dist = new float[1];
                        android.location.Location.distanceBetween(
                                loc.getLatitude(), loc.getLongitude(), o[0], o[1], dist);
                        if (dist[0] <= ALLOWED_RADIUS_METERS) {
                            insideAllowed = true;
                            break;
                        }
                    }

                    if (insideAllowed) action.run();
                    else Toast.makeText(this, "You are not at an allowed office location!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        if (requestCode == PERMISSION_CAMERA || requestCode == PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. Try again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}
