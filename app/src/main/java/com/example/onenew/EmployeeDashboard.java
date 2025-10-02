package com.example.onenew;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmployeeDashboard extends AppCompatActivity {

    private static final int PERMISSION_CAMERA = 200;
    private static final int PERMISSION_LOCATION = 201;

    // === UI ===
    private TextView tvName, tvEmpId, tvDutyHour, tvBreakDuration,
            tvCheckInBtn, btnCheckOut, btnStartBreak, btnEndBreak,
            tvDate, tvCheckInTime, tvCheckOutTime, tvBreakStartTime, tvBreakEndTime;

    private AlertDialog dialog;
    private ImageView dialogImagePreview;
    private Button btnSave;
    private Bitmap capturedImage;

    private boolean breakTaken = false;
    private long checkInTime = 0L;
    private long breakStartTime = 0L;
    private long totalBreakMillis = 0L;
    private long firstBreakStart = 0L;

    private SharedPreferences prefs;
    private String checkInDate = "";   // <-- holds the locked date

    // Allowed office coordinates
    private static final double OFFICE1_LAT = 30.6484947;
    private static final double OFFICE1_LNG = 73.1070595;
    private static final double OFFICE2_LAT = 30.8049604;
    private static final double OFFICE2_LNG = 73.4380137;
    private static final double OFFICE3_LAT = 30.6703321;
    private static final double OFFICE3_LNG = 73.1276480;
    private static final double OFFICE4_LAT = 30.8768576;
    private static final double OFFICE4_LNG = 73.5926216;

    private static final double OFFICE5_LAT = 31.5191643;
    private static final double OFFICE5_LNG = 74.3209694;
    private static final float ALLOWED_RADIUS_METERS = 1000f;

    private FusedLocationProviderClient fusedClient;

    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
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

        breakTaken = prefs.getBoolean("breakTaken", false);
        checkInDate = prefs.getString("checkInDate", ""); // load saved date

        // Bind Views
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

        // name/ID from Login
        String empId = getIntent().getStringExtra("empId");
        String empName = getIntent().getStringExtra("empName");
        tvName.setText(empName != null ? empName : "Employee");
        tvEmpId.setText(empId != null ? empId : "ID");

        // show locked date if exists, else today
        String displayDate = checkInDate.isEmpty()
                ? new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())
                : checkInDate;
        tvDate.setText(displayDate);

        restoreSavedState();

        tvCheckInBtn.setOnClickListener(v -> attemptAction(this::handleCheckIn));
        btnCheckOut.setOnClickListener(v -> attemptAction(this::handleCheckOut));
        btnStartBreak.setOnClickListener(v -> attemptAction(this::handleStartBreak));
        btnEndBreak.setOnClickListener(v -> attemptAction(this::handleEndBreak));
    }

    @SuppressLint("SetTextI18n")
    private void restoreSavedState() {
        checkInTime = prefs.getLong("checkInTime", 0L);
        breakStartTime = prefs.getLong("breakStartTime", 0L);
        totalBreakMillis = prefs.getLong("totalBreakMillis", 0L);
        firstBreakStart = prefs.getLong("firstBreakStart", 0L);

        if (checkInTime != 0L) {
            tvCheckInTime.setText("Check-In Time: " +
                    new SimpleDateFormat("hh:mm a", Locale.getDefault())
                            .format(new Date(checkInTime)));
        }

        if (breakStartTime != 0L) {
            long extra = System.currentTimeMillis() - breakStartTime;
            totalBreakMillis += extra;
            prefs.edit()
                    .putLong("totalBreakMillis", totalBreakMillis)
                    .putLong("breakStartTime", 0L)
                    .apply();
            breakStartTime = 0L;
            Toast.makeText(this, "Break ended automatically (app restart)", Toast.LENGTH_SHORT).show();
        }
    }

    /* ---------------- Attendance Logic ---------------- */

    @SuppressLint("SetTextI18n")
    private void handleCheckIn() {
        if (checkInTime != 0) {
            Toast.makeText(this, "Already checked in", Toast.LENGTH_SHORT).show();
            return;
        }

        // lock the date at the moment of check-in
        checkInDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        prefs.edit().putString("checkInDate", checkInDate).apply();
        tvDate.setText(checkInDate);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String empId = tvEmpId.getText().toString();

        showCaptureDialog(() -> {
            checkInTime = System.currentTimeMillis();
            prefs.edit().putLong("checkInTime", checkInTime).apply();
            tvCheckInTime.setText("Check-In Time: " +
                    new SimpleDateFormat("hh:mm a", Locale.getDefault())
                            .format(new Date(checkInTime)));

            Map<String, Object> checkInData = new HashMap<>();
            checkInData.put("employeeId", empId);
            checkInData.put("name", tvName.getText().toString());
            checkInData.put("status", "Present");
            checkInData.put("checkInTime",
                    new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            .format(new Date(checkInTime)));
            checkInData.put("timestamp", FieldValue.serverTimestamp());

            db.collection("attendance")
                    .document(checkInDate)
                    .collection("records")
                    .document(empId)
                    .set(checkInData, SetOptions.merge())
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

        if (breakStartTime != 0L) {
            totalBreakMillis += System.currentTimeMillis() - breakStartTime;
            breakStartTime = 0L;
            prefs.edit().putLong("breakStartTime", 0L).apply();
        }

        long now = System.currentTimeMillis();
        long dutyMillis = now - checkInTime - totalBreakMillis;
        tvDutyHour.setText("Duty Hour: " + formatMillis(dutyMillis));
        tvBreakDuration.setText("Break Duration: " + formatMillis(totalBreakMillis));
        tvCheckOutTime.setText("Check-Out Time: " +
                new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(now)));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> update = new HashMap<>();
        update.put("checkOutTime",
                new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(now)));
        update.put("dutyMillis", dutyMillis);
        update.put("breakMillis", totalBreakMillis);
        if (firstBreakStart != 0L) {
            update.put("breakStart",
                    new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(firstBreakStart)));
        }
        update.put("breakEnd",
                new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(now)));
        update.put("status", "CheckedOut");

        // always use locked checkInDate
        db.collection("attendance")
                .document(checkInDate)
                .collection("records")
                .document(tvEmpId.getText().toString())
                .set(update, SetOptions.merge())
                .addOnFailureListener(e -> Toast.makeText(this, "Firestore Update Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // clear for next day
        prefs.edit()
                .remove("checkInDate")
                .putLong("checkInTime", 0L)
                .putLong("totalBreakMillis", 0L)
                .putLong("breakStartTime", 0L)
                .putLong("firstBreakStart", 0L)
                .apply();

        checkInTime = 0L;
        totalBreakMillis = 0L;
        breakStartTime = 0L;
        firstBreakStart = 0L;
        checkInDate = "";
    }

    @SuppressLint("SetTextI18n")
    private void handleStartBreak() {
        if (checkInTime == 0) {
            Toast.makeText(this, "Check in first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (breakTaken) {
            Toast.makeText(this, "Only one break allowed", Toast.LENGTH_SHORT).show();
            return;
        }
        if (breakStartTime != 0) {
            Toast.makeText(this, "Break already in progress", Toast.LENGTH_SHORT).show();
            return;
        }

        breakStartTime = System.currentTimeMillis();
        prefs.edit().putLong("breakStartTime", breakStartTime).apply();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String empId = tvEmpId.getText().toString().trim();
        if (empId.isEmpty()) {
            Toast.makeText(this, "Employee ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put("breakStart", new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(new Date(breakStartTime)));

        db.collection("attendance")
                .document(checkInDate)
                .collection("records")
                .document(empId)
                .set(update, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Break start saved"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving break start", e));

        tvBreakStartTime.setText("Break-Start Time: " +
                new SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(new Date(breakStartTime)));
        Toast.makeText(this, "Break Started", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    private void handleEndBreak() {
        if (breakStartTime == 0) {
            Toast.makeText(this, "Break not in progress", Toast.LENGTH_SHORT).show();
            return;
        }

        totalBreakMillis += System.currentTimeMillis() - breakStartTime;
        long breakEndTime = System.currentTimeMillis();
        breakStartTime = 0L;
        breakTaken = true;

        prefs.edit()
                .putBoolean("breakTaken", true)
                .putLong("totalBreakMillis", totalBreakMillis)
                .putLong("breakStartTime", 0L)
                .putLong("lastBreakEndTime", breakEndTime)
                .apply();

        tvBreakEndTime.setText("Break-End Time: " +
                new SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(new Date(breakEndTime)));
        Toast.makeText(this, "Break Ended", Toast.LENGTH_SHORT).show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String empId = tvEmpId.getText().toString().trim();
        if (empId.isEmpty()) {
            Toast.makeText(this, "Employee ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put("breakEnd", new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(new Date(breakEndTime)));
        update.put("breakMillis", totalBreakMillis);

        db.collection("attendance")
                .document(checkInDate)
                .collection("records")
                .document(empId)
                .set(update, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Break end saved"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving break end", e));
    }

    private String formatMillis(long ms) {
        long hrs = ms / (1000 * 60 * 60);
        long mins = (ms / (1000 * 60)) % 60;
        return hrs + "h " + mins + "m";
    }

    /* --------------- Camera Dialog --------------- */
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
            if (checkCameraPermission()) {
                cameraLauncher.launch(null);
            }
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

    /* --------------- Location & Permission --------------- */
    private void attemptAction(Runnable action) {
        if (!checkLocationPermission()) return;

        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(loc -> {
                    if (loc == null) {
                        Toast.makeText(this, "Location unavailable. Try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    float[] d1 = new float[1];
                    float[] d2 = new float[1];
                    float[] d3 = new float[1];
                    float[] d4 = new float[1];
                    float[] d5 = new float[1];
                    android.location.Location.distanceBetween(
                            loc.getLatitude(), loc.getLongitude(), OFFICE1_LAT, OFFICE1_LNG, d1);
                    android.location.Location.distanceBetween(
                            loc.getLatitude(), loc.getLongitude(), OFFICE2_LAT, OFFICE2_LNG, d2);
                    android.location.Location.distanceBetween(
                            loc.getLatitude(), loc.getLongitude(), OFFICE3_LAT, OFFICE3_LNG, d3);
                    android.location.Location.distanceBetween(
                            loc.getLatitude(), loc.getLongitude(), OFFICE4_LAT, OFFICE4_LNG, d4);
                    android.location.Location.distanceBetween(
                            loc.getLatitude(), loc.getLongitude(), OFFICE5_LAT, OFFICE5_LNG, d5);

                    if (d1[0] <= ALLOWED_RADIUS_METERS || d2[0] <= ALLOWED_RADIUS_METERS ||
                            d3[0] <= ALLOWED_RADIUS_METERS || d4[0] <= ALLOWED_RADIUS_METERS || d5[0] <= ALLOWED_RADIUS_METERS) {
                        action.run();
                    } else {
                        Toast.makeText(this, "You are not at an allowed office location!", Toast.LENGTH_LONG).show();
                    }
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
