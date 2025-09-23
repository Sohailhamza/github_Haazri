package com.example.onenew;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        h.tvName.setText(safe(e.name));
        h.tvId.setText("ID: " + safe(e.employeeId));
        h.tvStatus.setText("Status: " + safe(e.status));
        h.tvIn.setText("Check-In: " + formatTime(e.checkInMillis));
        h.tvOut.setText("Check-Out: " + formatTime(e.checkOutMillis));
        h.tvBreakStart.setText("Break Start: " + formatTime(e.breakStartTime));
        h.tvBreakEnd.setText("Break End: " + formatTime(e.breakEndTime));
        h.tvBreak.setText("Break Duration: " + (e.breakMillis / 1000) + " sec");
        h.tvDuty.setText("Duty Duration: " + (e.dutyMillis / 1000) + " sec");
    }

    private String safe(String s) {
        return s == null ? "—" : s;
    }

    /** 👇 Ye method yahan rakhein */
    private String formatTime(long millis) {
        if (millis <= 0) return "—";
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(new Date(millis));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvName, tvId, tvStatus, tvIn, tvOut,
                tvBreakStart, tvBreakEnd, tvBreak, tvDuty;
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
