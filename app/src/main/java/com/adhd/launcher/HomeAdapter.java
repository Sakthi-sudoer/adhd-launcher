package com.adhd.launcher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.VH> {

    public interface OnAppClick {
        void onClick(AppInfo app);
    }

    private final List<AppInfo> apps;
    private final OnAppClick listener;

    public HomeAdapter(List<AppInfo> apps, OnAppClick listener) {
        this.apps = apps;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_home, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AppInfo app = apps.get(pos);
        h.icon.setImageDrawable(app.icon);
        h.name.setText(app.label);
        h.itemView.setOnClickListener(v -> listener.onClick(app));
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;

        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.ivAppIcon);
            name = v.findViewById(R.id.tvAppName);
        }
    }
}
