package com.example.onenew;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddEmployeeDialog extends DialogFragment {

    private EditText etName, etID, etPassword, etPhoneNum, etAddress;

    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-\\[\\]{};':\"\\\\|,.<>/?]).{6,}$";

    private FirebaseFirestore db;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = getLayoutInflater().inflate(R.layout.dialog_add_employee, null, false);

        etName      = v.findViewById(R.id.etName);
        etID        = v.findViewById(R.id.etID);
        etPassword  = v.findViewById(R.id.etPassword);
        etPhoneNum  = v.findViewById(R.id.etPhoneNum);
        etAddress   = v.findViewById(R.id.etAddress);

        db = FirebaseFirestore.getInstance();

        // ⛔ Image pick button/commented — no image selection
        // Button btnPickImage = v.findViewById(R.id.btnPickImage);
        // btnPickImage.setVisibility(View.GONE);

        // Add employee button
        Button btnAdd = v.findViewById(R.id.btnAddEmployeeConfirm);
        btnAdd.setOnClickListener(view -> addEmployee());

        return new AlertDialog.Builder(requireContext()).setView(v).create();
    }

    private void addEmployee() {
        String name    = etName.getText().toString().trim();
        String id      = etID.getText().toString().trim();
        String pass    = etPassword.getText().toString().trim();
        String phone   = etPhoneNum.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || id.isEmpty() || pass.isEmpty() ||
                phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(getContext(), "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Password validation
        if (!pass.matches(PASSWORD_PATTERN)) {
            Toast.makeText(getContext(),
                    "Password must be ≥6 chars, include an uppercase letter, " +
                            "a number, and a special character.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> emp = new HashMap<>();
        emp.put("name", name);
        emp.put("password", pass);
        emp.put("phone", phone);
        emp.put("address", address);

        db.collection("employees").document(id)
                .set(emp)
                .addOnSuccessListener(a -> {
                    Toast.makeText(getContext(), "Employee Added", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Firestore Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

}
