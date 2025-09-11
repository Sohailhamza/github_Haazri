package com.example.onenew;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class RemoveEmployeeAdapter extends RecyclerView.Adapter<RemoveEmployeeAdapter.EmpVH> {

    private final List<Employee> list;

    public RemoveEmployeeAdapter(List<Employee> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public EmpVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_remove_employee, parent, false);
        return new EmpVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EmpVH holder, int pos) {
        Employee e = list.get(pos);
        holder.txtName.setText(e.getName() + " (" + e.getId() + ")");
        holder.btnDelete.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("employees")
                    .document(e.getId())
                    .delete()
                    .addOnSuccessListener(a -> {
                        list.remove(pos);
                        notifyItemRemoved(pos);
                    });
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class EmpVH extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageButton btnDelete;
        EmpVH(View v){
            super(v);
            txtName = v.findViewById(R.id.txtEmpName);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
