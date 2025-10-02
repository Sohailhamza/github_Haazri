package com.example.onenew;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CaptureDialog extends AppCompatActivity {

    private ImageView imageView;
    private Button btnCapture, btnSave;
    private Bitmap capturedBitmap;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    capturedBitmap = (Bitmap) result.getData().getExtras().get("data");
                    imageView.setImageBitmap(capturedBitmap);
                    btnSave.setEnabled(true);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_capture);

        imageView = findViewById(R.id.imagePreview);
        btnCapture = findViewById(R.id.btnCapture);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setEnabled(false);

        // Request camera permission if not already granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 200);
        }

        btnCapture.setOnClickListener(v -> openCamera());

        btnSave.setOnClickListener(v -> {
            if (capturedBitmap != null) {
                uploadImageToCloudinary(capturedBitmap);
            } else {
                Toast.makeText(this, "Capture a photo first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- Upload Image to Cloudinary ----------
    private void uploadImageToCloudinary(Bitmap bitmap) {
        File file = bitmapToFile(bitmap);
        if (file == null) {
            Toast.makeText(this, "Error converting image", Toast.LENGTH_SHORT).show();
            return;
        }

        MediaManager.get().upload(file.getAbsolutePath())
                .unsigned("checkin_upload")   // 👈 tumhara preset name
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(CaptureDialog.this, "Uploading...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        saveImageUrlToFirestore(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(CaptureDialog.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    // ---------- Convert Bitmap to File ----------
    private File bitmapToFile(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir(), "temp_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ---------- Save URL to Firestore ----------
    private void saveImageUrlToFirestore(String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 👇 yahan tum apni logic se employee ID aur date pass karna
        String empId = "emp001"; // test ke liye static rakha hai
        String date = "2025-10-02"; // test ke liye static rakha hai

        Map<String, Object> data = new HashMap<>();
        data.put("selfieUrl", imageUrl);

        db.collection("attendance")
                .document(date)
                .collection("records")
                .document(empId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "✅ Image saved with attendance!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Firestore save failed", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }
}
