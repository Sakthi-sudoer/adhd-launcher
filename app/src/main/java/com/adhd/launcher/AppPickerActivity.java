package com.adhd.launcher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppPickerActivity extends AppCompatActivity {

    private AppPickerAdapter adapter;
    private final List<AppInfo> allApps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_picker);

        RecyclerView rv = findViewById(R.id.rvAllApps);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppPickerAdapter(allApps);
        rv.setAdapter(adapter);

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Done — save and return
        TextView btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> saveAndFinish());

        // Select all / clear all
        findViewById(R.id.btnSelectAll).setOnClickListener(v -> adapter.selectAll());
        findViewById(R.id.btnClearAll).setOnClickListener(v -> adapter.clearAll());

        // Search
        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadApps();
    }

    private void loadApps() {
        new AsyncTask<Void, Void, List<AppInfo>>() {
            @Override
            protected List<AppInfo> doInBackground(Void... v) {
                PackageManager pm = getPackageManager();
                Set<String> saved = Prefs.getSelectedApps(AppPickerActivity.this);

                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> resolved = pm.queryIntentActivities(intent, 0);

                List<AppInfo> list = new ArrayList<>();
                for (ResolveInfo ri : resolved) {
                    String pkg = ri.activityInfo.packageName;
                    // Don't show this launcher itself
                    if (pkg.equals(getPackageName())) continue;
                    list.add(new AppInfo(
                            ri.loadLabel(pm).toString(),
                            pkg,
                            ri.loadIcon(pm),
                            saved.contains(pkg)
                    ));
                }
                Collections.sort(list, (a, b) -> a.label.compareToIgnoreCase(b.label));
                return list;
            }

            @Override
            protected void onPostExecute(List<AppInfo> result) {
                allApps.clear();
                allApps.addAll(result);
                adapter.filter(""); // reset filter with full list
            }
        }.execute();
    }

    private void saveAndFinish() {
        Set<String> selected = new HashSet<>();
        for (AppInfo a : allApps) {
            if (a.selected) selected.add(a.packageName);
        }
        Prefs.saveSelectedApps(this, selected);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        saveAndFinish();
    }
}
