package com.adhd.launcher;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public String label;
    public String packageName;
    public Drawable icon;
    public boolean selected;

    public AppInfo(String label, String packageName, Drawable icon, boolean selected) {
        this.label = label;
        this.packageName = packageName;
        this.icon = icon;
        this.selected = selected;
    }
}
