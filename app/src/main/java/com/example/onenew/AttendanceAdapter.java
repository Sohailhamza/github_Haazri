package com.example.onenew;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.Holder> {

    private final List<EmployeeAttendance> items;

    public AttendanceAdapter(List<EmployeeAttendance> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_employee, parent, false);
        return new Holder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        EmployeeAttendance e = items.get(pos);
        h.tvName.setText("Name: " + safe(e.name));
        h.tvId.setText("ID: " + safe(e.employeeId));
        h.tvStatus.setText("Status: " + safe(e.status));
        h.tvIn.setText("Check-In: " + safe(e.checkInTime));
        h.tvOut.setText("Check-Out: " + safe(e.checkOutTime));
        h.tvBreakStart.setText("Break Start: " + safe(e.breakStart));
        h.tvBreakEnd.setText("Break End: " + safe(e.breakEnd));
        h.tvBreak.setText("Break Duration: " + (e.breakMillis / 1000) + " sec");
        h.tvDuty.setText("Duty Duration: " + (e.dutyMillis / 1000) + " sec");
    }

    private String safe(String s) {
        return s == null ? "—" : s;
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvName, tvId, tvStatus, tvIn, tvOut, tvBreakStart, tvBreakEnd, tvBreak, tvDuty;
        Holder(View v) {
            super(v);
            tvName  = v.findViewById(R.id.tvName);
            tvId    = v.findViewById(R.id.tvId);
            tvStatus= v.findViewById(R.id.tvStatus);
            tvIn    = v.findViewById(R.id.tvIn);
            tvOut   = v.findViewById(R.id.tvOut);
            tvBreakStart = v.findViewById(R.id.tvBreakStart);
            tvBreakEnd   = v.findViewById(R.id.tvBreakEnd);
            tvBreak = v.findViewById(R.id.tvBreak);
            tvDuty  = v.findViewById(R.id.tvDuty);
        }
    }
}
