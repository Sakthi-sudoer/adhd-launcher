package com.adhd.launcher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AppPickerAdapter extends RecyclerView.Adapter<AppPickerAdapter.VH> {

    private final List<AppInfo> allApps;
    private List<AppInfo> filtered;

    public AppPickerAdapter(List<AppInfo> apps) {
        this.allApps = apps;
        this.filtered = new ArrayList<>(apps);
    }

    public void filter(String query) {
        filtered = new ArrayList<>();
        String q = query.toLowerCase().trim();
        for (AppInfo a : allApps) {
            if (q.isEmpty() || a.label.toLowerCase().contains(q)) {
                filtered.add(a);
            }
        }
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (AppInfo a : allApps) a.selected = true;
        notifyDataSetChanged();
    }

    public void clearAll() {
        for (AppInfo a : allApps) a.selected = false;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_picker, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AppInfo app = filtered.get(pos);
        h.icon.setImageDrawable(app.icon);
        h.name.setText(app.label);
        h.check.setChecked(app.selected);
        h.itemView.setOnClickListener(v -> {
            app.selected = !app.selected;
            h.check.setChecked(app.selected);
        });
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox check;

        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.ivIcon);
            name = v.findViewById(R.id.tvName);
            check = v.findViewById(R.id.cbSelected);
        }
    }
}
