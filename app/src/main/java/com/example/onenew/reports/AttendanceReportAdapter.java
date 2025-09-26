package com.example.onenew.reports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onenew.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AttendanceReportAdapter extends RecyclerView.Adapter<AttendanceReportAdapter.ViewHolder> {

    private List<AttendanceRecord> records;

    public AttendanceReportAdapter(List<AttendanceRecord> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your custom item layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord record = records.get(position);

        holder.tvDate.setText(record.date);
        holder.tvStatus.setText(record.status);

        long workMillis = record.dutyMillis - record.breakMillis;
        long TWELVE_HOURS_MS = 12 * 60 * 60 * 1000;

        // Duty hours
        long dutyHrs = TimeUnit.MILLISECONDS.toHours(workMillis);
        long dutyMin = TimeUnit.MILLISECONDS.toMinutes(workMillis) % 60;
        holder.tvDutyHours.setText(String.format("%d:%02d", dutyHrs, dutyMin));

        // Overtime / Shortfall
        if (workMillis > TWELVE_HOURS_MS) {
            long overtime = workMillis - TWELVE_HOURS_MS;
            holder.tvOvertime.setText(String.format("%d:%02d",
                    TimeUnit.MILLISECONDS.toHours(overtime),
                    TimeUnit.MILLISECONDS.toMinutes(overtime) % 60));
            holder.tvShortTime.setText("0:00");
        } else if (workMillis < TWELVE_HOURS_MS) {
            long shortfall = TWELVE_HOURS_MS - workMillis;
            holder.tvShortTime.setText(String.format("%d:%02d",
                    TimeUnit.MILLISECONDS.toHours(shortfall),
                    TimeUnit.MILLISECONDS.toMinutes(shortfall) % 60));
            holder.tvOvertime.setText("0:00");
        } else {
            holder.tvOvertime.setText("0:00");
            holder.tvShortTime.setText("0:00");
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStatus, tvDutyHours, tvOvertime, tvShortTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDutyHours = itemView.findViewById(R.id.tvDutyHours);
            tvOvertime = itemView.findViewById(R.id.tvOvertime);
            tvShortTime = itemView.findViewById(R.id.tvShortTime);
        }
    }
}
