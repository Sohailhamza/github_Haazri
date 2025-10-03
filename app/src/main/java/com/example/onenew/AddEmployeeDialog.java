package com.example.onenew;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddEmployeeDialog extends BottomSheetDialogFragment {

    private EditText etName, etID, etPassword, etPhoneNum, etAddress;
    private Button btnAdd, btnPickImage;
    private ImageView imgEmployee;
    private FirebaseFirestore db;

    private Uri selectedImageUri;

    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_employee, container, false);

        etName = view.findViewById(R.id.etName);
        etID = view.findViewById(R.id.etID);
        etPassword = view.findViewById(R.id.etPassword);
        etPhoneNum = view.findViewById(R.id.etPhoneNum);
        etAddress = view.findViewById(R.id.etAddress);
        btnAdd = view.findViewById(R.id.btnAddEmployeeConfirm);
        imgEmployee = view.findViewById(R.id.imgEmployee);
        btnPickImage = view.findViewById(R.id.btnPickImage);

        db = FirebaseFirestore.getInstance();

        // 📷 Pick Image
        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1001);
        });

        btnAdd.setOnClickListener(v -> addEmployee());

        return view;
    }

    // 📷 After picking image
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == getActivity().RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imgEmployee.setImageURI(selectedImageUri);
        }
    }

    // 🚀 Add Employee with Cloudinary Upload
    private void addEmployee() {
        String name = etName.getText().toString().trim();
        String id = etID.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String phone = etPhoneNum.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || id.isEmpty() || pass.isEmpty() ||
                phone.isEmpty() || address.isEmpty() || selectedImageUri == null) {
            Toast.makeText(getContext(), "All fields including photo are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.matches(PASSWORD_PATTERN)) {
            Toast.makeText(getContext(),
                    "Password must be ≥6 chars, include uppercase, number, special char",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // ✅ Upload to Cloudinary
        MediaManager.get().upload(selectedImageUri)
                .option("folder", "employees")  // optional folder name
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(getContext(), "Uploading photo...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String photoUrl = resultData.get("secure_url").toString();

                        // ✅ Save to Firestore
                        Map<String, Object> emp = new HashMap<>();
                        emp.put("name", name);
                        emp.put("password", pass);
                        emp.put("phone", phone);
                        emp.put("address", address);
                        emp.put("photo", photoUrl);

                        db.collection("employees").document(id)
                                .set(emp)
                                .addOnSuccessListener(a -> {
                                    Toast.makeText(getContext(), "Employee Added", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(),
                                        "Firestore Error: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(getContext(), "Upload Error: " + error.getDescription(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) { }
                })
                .dispatch();
    }
}
