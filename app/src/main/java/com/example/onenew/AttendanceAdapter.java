package com.example.onenew;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
        h.tvIn.setText("Check-In: " + safe(e.checkInTime));
        h.tvOut.setText("Check-Out: " + safe(e.checkOutTime));
        h.tvBreakStart.setText("Break Start: " + safe(e.breakStart));
        h.tvBreakEnd.setText("Break End: " + safe(e.breakEnd));
        h.tvBreak.setText("Break Duration: " + formatDuration(e.breakMillis));
        h.tvDuty.setText("Duty Duration: " + formatDuration(e.dutyMillis));

        // 👇 Selfie load karo agar URL mila hai
        if (e.selfieUrl != null && !e.selfieUrl.isEmpty()) {
            Glide.with(h.ivSelfie.getContext())
                    .load(e.selfieUrl)
                    .placeholder(R.drawable.ic_person) // jab tak load ho raha hai
                    .error(R.drawable.ic_person)       // agar URL galat ho
                    .into(h.ivSelfie);
        } else {
            h.ivSelfie.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // --------- Helper Methods ---------
    private String formatDuration(long millis) {
        if (millis <= 0) return "—";

        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d hr %d min %d sec", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format(Locale.getDefault(), "%d min %d sec", minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%d sec", seconds);
        }
    }

    private String safe(String s) {
        return s == null || s.isEmpty() ? "—" : s;
    }

    // --------- Holder ---------
    static class Holder extends RecyclerView.ViewHolder {
        TextView tvName, tvId, tvStatus, tvIn, tvOut, tvBreakStart, tvBreakEnd, tvBreak, tvDuty;
        ImageView ivSelfie;

        Holder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvId = v.findViewById(R.id.tvId);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvIn = v.findViewById(R.id.tvIn);
            tvOut = v.findViewById(R.id.tvOut);
            tvBreakStart = v.findViewById(R.id.tvBreakStart);
            tvBreakEnd = v.findViewById(R.id.tvBreakEnd);
            tvBreak = v.findViewById(R.id.tvBreak);
            tvDuty = v.findViewById(R.id.tvDuty);
            ivSelfie = v.findViewById(R.id.ivSelfie);
        }
    }
}
