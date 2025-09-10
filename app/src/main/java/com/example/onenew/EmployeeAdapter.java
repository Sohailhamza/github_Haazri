package com.example.onenew;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder> {

    private final List<Employee> employeeList;
    private final OnEmployeeClickListener listener;

    // Constructor with click listener
    public EmployeeAdapter(List<Employee> employeeList, OnEmployeeClickListener listener) {
        this.employeeList = employeeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Employee employee = employeeList.get(position);

        holder.tvName.setText(employee.getName());
        holder.tvId.setText("ID: " + employee.getId());
        holder.tvStatus.setText("Status: " + employee.getStatus());
        holder.tvDutyStart.setText("Duty Start: " + employee.getDutyStartTime());
        holder.tvDutyOff.setText("Duty Off: " + employee.getDutyOffTime());
        holder.tvBreakStart.setText("Break Start: " + employee.getBreakStartTime());
        holder.tvBreakEnd.setText("Break End: " + employee.getBreakEndTime());

        holder.ivPhoto.setImageResource(employee.getImageResId());

        // Status ke liye color coding
        if (employee.getStatus().equalsIgnoreCase("Present")) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // Red
        }

        // 👇 Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEmployeeClick(employee);
            }
        });
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvId, tvStatus, tvDutyStart, tvDutyOff, tvBreakStart, tvBreakEnd;
        ImageView ivPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivEmployeePhoto);
            tvName = itemView.findViewById(R.id.tvEmployeeName);
            tvId = itemView.findViewById(R.id.tvEmployeeId);
            tvStatus = itemView.findViewById(R.id.tvEmployeeStatus);
            tvDutyStart = itemView.findViewById(R.id.tvDutyStart);
            tvDutyOff = itemView.findViewById(R.id.tvDutyOff);
            tvBreakStart = itemView.findViewById(R.id.tvBreakStart);
            tvBreakEnd = itemView.findViewById(R.id.tvBreakEnd);
        }
    }

    // 👇 Interface for clicks
    public interface OnEmployeeClickListener {
        void onEmployeeClick(Employee employee);
    }
}
