package com.example.onenew;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
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

    private EditText etName, etID, etPassword;
    private FirebaseFirestore db;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_employee, null, false);

        etName = v.findViewById(R.id.etName);
        etID = v.findViewById(R.id.etID);
        etPassword = v.findViewById(R.id.etPassword);
        db = FirebaseFirestore.getInstance();

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(v);

        Button btnAdd = v.findViewById(R.id.btnAddEmployeeConfirm);
        btnAdd.setOnClickListener(view -> addEmployee());

        return builder.create();
    }

    private void addEmployee() {
        String name = etName.getText().toString().trim();
        String id   = etID.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (name.isEmpty() || id.isEmpty() || pass.isEmpty()) {
            Toast.makeText(getContext(), "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> emp = new HashMap<>();
        emp.put("name", name);
        emp.put("password", pass);

        db.collection("employees").document(id)
                .set(emp)
                .addOnSuccessListener(a -> {
                    Toast.makeText(getContext(), "Employee Added", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
