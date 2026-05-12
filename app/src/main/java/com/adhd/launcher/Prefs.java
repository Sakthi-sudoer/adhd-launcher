package com.adhd.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

/** Persists the user's selected app package names. */
public class Prefs {
    private static final String PREF_FILE = "adhd_launcher_prefs";
    private static final String KEY_SELECTED = "selected_apps";

    public static Set<String> getSelectedApps(Context ctx) {
        return new HashSet<>(getPrefs(ctx).getStringSet(KEY_SELECTED, new HashSet<>()));
    }

    public static void saveSelectedApps(Context ctx, Set<String> packages) {
        getPrefs(ctx).edit().putStringSet(KEY_SELECTED, packages).apply();
    }

    private static SharedPreferences getPrefs(Context ctx) {
        return ctx.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    }
}
