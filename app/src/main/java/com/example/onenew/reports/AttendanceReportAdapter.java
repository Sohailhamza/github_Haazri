package com.example.onenew.reports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.onenew.R;

import java.util.List;

public class AttendanceReportAdapter extends RecyclerView.Adapter<AttendanceReportAdapter.ViewHolder> {

    private List<AttendanceRecord> recordList;

    public AttendanceReportAdapter(List<AttendanceRecord> recordList) {
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_day, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord record = recordList.get(position);

        holder.tvDate.setText(record.getDate());
        holder.tvStatus.setText(record.getStatus());
        holder.tvDutyHours.setText(record.getDutyHours());
        holder.tvOvertime.setText(record.getOvertime());
        holder.tvShortTime.setText(record.getShortTime());
    }

    @Override
    public int getItemCount() {
        return recordList.size();
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
