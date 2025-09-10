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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class CaptureDialog extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 100;
    private ImageView imageView;
    private Button btnCapture, btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_capture); // ✅ Ye wala layout use hoga

        imageView = findViewById(R.id.imagePreview);
        btnCapture = findViewById(R.id.btnCapture);
        btnSave = findViewById(R.id.btnSave);

        // Default disable Save button until picture is captured
        btnSave.setEnabled(false);

        // Request camera permission if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 200);
        }

        // Capture button
        btnCapture.setOnClickListener(v -> openCamera());

        // Save button
        btnSave.setOnClickListener(v -> {
            Toast.makeText(this, "✅ Image saved with attendance!", Toast.LENGTH_SHORT).show();
            finish(); // close activity
        });
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            btnSave.setEnabled(true); // enable save after picture
        }
    }
}
