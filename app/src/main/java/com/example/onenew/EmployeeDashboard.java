package com.example.onenew;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

public class EmployeeDashboard extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 200;

    private ImageView dialogImagePreview;
    private AlertDialog dialog;
    private Bitmap capturedImage;
    private Button btnSave;   // 👈 Save button reference

    // New API for camera
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bundle extras = result.getData().getExtras();
                            if (extras != null) {
                                capturedImage = (Bitmap) extras.get("data");
                                if (dialogImagePreview != null && capturedImage != null) {
                                    dialogImagePreview.setImageBitmap(capturedImage);

                                    // ✅ Enable Save button after selfie capture
                                    if (btnSave != null) {
                                        btnSave.setEnabled(true);
                                    }
                                }
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        TextView tvCheckIn = findViewById(R.id.tvCheckIn);

        tvCheckIn.setOnClickListener(v -> showCaptureDialog());
    }

    private void showCaptureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_capture, null);
        builder.setView(dialogView);

        dialogImagePreview = dialogView.findViewById(R.id.imagePreview);
        Button btnCapture = dialogView.findViewById(R.id.btnCapture);
        btnSave = dialogView.findViewById(R.id.btnSave);

        // ❌ Disable save button initially
        btnSave.setEnabled(false);

        dialog = builder.create();
        dialog.show();

        // 📸 Capture button
        btnCapture.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        cameraLauncher.launch(intent);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // ✅ Save button
        btnSave.setOnClickListener(v -> {
            if (capturedImage != null) {
                Toast.makeText(this, "✅ Attendance Marked with Selfie!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "⚠ Please capture a selfie first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔑 Runtime Permission Check
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    // 🎯 Permission Result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted! Tap Capture again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
