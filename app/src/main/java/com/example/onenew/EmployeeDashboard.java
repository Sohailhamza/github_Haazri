package com.example.onenew;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EmployeeDashboard extends AppCompatActivity {

    private static final int PERMISSION_CAMERA = 200;
    private static final int PERMISSION_LOCATION = 201;

    // === UI ===
    private TextView tvName, tvEmpId, tvDutyHour, tvBreakDuration,
            tvCheckInBtn, btnCheckOut, btnStartBreak, btnEndBreak,
            tvDate, tvCheckInTime, tvCheckOutTime;
    private AlertDialog dialog;
    private ImageView dialogImagePreview;
    private Button btnSave;
    private Bitmap capturedImage;

    // === Time tracking ===
    private long checkInTime = 0L;
    private long breakStartTime = 0L;
    private long totalBreakMillis = 0L;

    // === Office coordinates (example) ===
    private static final double OFFICE_LAT =  30.8049544;  // replace with your lat
    private static final double OFFICE_LNG = 73.4381871;  // replace with your lng
    private static final float ALLOWED_RADIUS_METERS = 100f;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bundle extras = result.getData().getExtras();
                            if (extras != null) {
                                capturedImage = (Bitmap) extras.get("data");
                                if (dialogImagePreview != null && capturedImage != null) {
                                    dialogImagePreview.setImageBitmap(capturedImage);
                                    if (btnSave != null) btnSave.setEnabled(true);
                                }
                            }
                        }
                    });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        // ---- Bind Views ----
        tvName          = findViewById(R.id.tvName);
        tvEmpId         = findViewById(R.id.tvEmpId);
        tvDutyHour      = findViewById(R.id.tvDHour);
        tvBreakDuration = findViewById(R.id.tvBreakDuration);
        tvCheckInBtn    = findViewById(R.id.tvCheckIn);
        btnCheckOut     = findViewById(R.id.btnCheckOut);
        btnStartBreak   = findViewById(R.id.btnStartBreak);
        btnEndBreak     = findViewById(R.id.btnEndBreak);
        tvDate          = findViewById(R.id.tvDate);
        tvCheckInTime   = findViewById(R.id.tvCheckInTime);
        tvCheckOutTime  = findViewById(R.id.tvCheckOutTime);

        // ---- Show name/ID from Login ----
        var empId   = getIntent().getStringExtra("empId");
        var empName = getIntent().getStringExtra("empName");

        tvName.setText(empName != null ? empName : "Employee");
        tvEmpId.setText(empId   != null ? empId   : "ID");

        // ---- Show today's date ----
        var today = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                .format(new Date());
        tvDate.setText(today);

        // ---- Clicks ----
        tvCheckInBtn.setOnClickListener(v -> attemptAction(this::handleCheckIn));
        btnCheckOut.setOnClickListener(v -> attemptAction(this::handleCheckOut));
        btnStartBreak.setOnClickListener(v -> attemptAction(this::handleStartBreak));
        btnEndBreak.setOnClickListener(v -> attemptAction(this::handleEndBreak));
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
            tvCheckInTime.setText("Check-In Time: " +
                    new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(checkInTime)));
            Toast.makeText(this, "Checked In!", Toast.LENGTH_SHORT).show();
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
        tvCheckOutTime.setText("Check-Out Time: " + new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(now)));

        Toast.makeText(this, "Checked Out!", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, "Break Started", Toast.LENGTH_SHORT).show();
    }

    private void handleEndBreak() {
        if (breakStartTime == 0) {
            Toast.makeText(this, "No active break", Toast.LENGTH_SHORT).show();
            return;
        }
        totalBreakMillis += System.currentTimeMillis() - breakStartTime;
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
    @SuppressLint("QueryPermissionsNeeded")
    private void showCaptureDialog(Runnable onSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_capture, null);
        builder.setView(dialogView);

        dialogImagePreview = dialogView.findViewById(R.id.imagePreview);
        Button btnCapture  = dialogView.findViewById(R.id.btnCapture);
        btnSave            = dialogView.findViewById(R.id.btnSave);
        btnSave.setEnabled(false);

        dialog = builder.create();
        dialog.show();

        btnCapture.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    cameraLauncher.launch(intent);
                }
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

    /*--------------------------------------------------
     *   Permissions & Location
     *--------------------------------------------------*/
    private void attemptAction(Runnable action) {
        if (!checkLocationPermission()) return;
        if (isInsideOffice()) {
            action.run();
        } else {
            Toast.makeText(this, "You are not at the office location!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CAMERA);
            return false;
        }
        return true;
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION);
            return false;
        }
        return true;
    }

    private boolean isInsideOffice() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return false;

        @SuppressLint("MissingPermission")
        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc == null) {
            Toast.makeText(this, "Location unavailable", Toast.LENGTH_SHORT).show();
            return false;
        }
        float[] distance = new float[1];
        Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                OFFICE_LAT, OFFICE_LNG, distance);
        return distance[0] <= ALLOWED_RADIUS_METERS;
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
}
