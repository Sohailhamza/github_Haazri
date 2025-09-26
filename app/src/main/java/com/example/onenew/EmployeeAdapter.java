package com.example.onenew;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder>
        implements Filterable {

    private final List<Employee> employeeList;   // filtered list
    private final List<Employee> fullList;       // original list copy
    private final OnEmployeeClickListener listener;

    // ---- Click interface ----
    public interface OnEmployeeClickListener {
        void onEmployeeClick(Employee employee);
    }

    public EmployeeAdapter(List<Employee> employeeList, OnEmployeeClickListener listener) {
        this.employeeList = employeeList;
        this.fullList = new ArrayList<>(employeeList);
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
        Employee e = employeeList.get(position);

        holder.tvName.setText(e.getName());
        holder.tvId.setText("ID: " + e.getId());
        holder.tvPassword.setText("Pass: " + e.getPassword());
        holder.tvPhone.setText("Ph: " + e.getPhone());
        holder.tvAddress.setText("Address: " + e.getAddress());
        holder.ivPhoto.setImageResource(e.getImageResId());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEmployeeClick(e);
        });
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    // ---------------- Filter for SearchView ----------------
    @Override
    public Filter getFilter() {
        return employeeFilter;
    }

    private final Filter employeeFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Employee> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(fullList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Employee emp : fullList) {
                    if (emp.getName().toLowerCase().contains(filterPattern) ||
                            emp.getPhone().toLowerCase().contains(filterPattern) ||
                            emp.getId().toLowerCase().contains(filterPattern)) {
                        filteredList.add(emp);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            employeeList.clear();
            employeeList.addAll((List<Employee>) results.values);
            notifyDataSetChanged();
        }
    };

    // 🔄 Call this after Firestore updates so full list refreshes too
    public void updateFullList(List<Employee> newList) {
        fullList.clear();
        fullList.addAll(newList);
    }

    // ---------------- ViewHolder ----------------
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvId, tvPassword, tvPhone, tvAddress;
        ImageView ivPhoto;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto    = itemView.findViewById(R.id.ivEmployeePhoto);
            tvName     = itemView.findViewById(R.id.tvEmployeeName);
            tvId       = itemView.findViewById(R.id.tvEmployeeId);
            tvPassword = itemView.findViewById(R.id.tvEmployeePassword);
            tvPhone    = itemView.findViewById(R.id.tvEmployeePhone);
            tvAddress  = itemView.findViewById(R.id.tvEmployeeAddress);
        }
    }
}
