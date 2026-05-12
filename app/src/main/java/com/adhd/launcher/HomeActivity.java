package com.adhd.launcher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private static final int REQ_PICKER = 1001;

    private RecyclerView rvApps;
    private TextView tvEmpty, tvTime, tvDate, tvGreeting;
    private HomeAdapter adapter;
    private final List<AppInfo> selectedApps = new ArrayList<>();
    private final Handler clockHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvApps = findViewById(R.id.rvApps);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTime = findViewById(R.id.tvTime);
        tvDate = findViewById(R.id.tvDate);
        tvGreeting = findViewById(R.id.tvGreeting);

        // 4-column grid
        rvApps.setLayoutManager(new GridLayoutManager(this, 4));
        adapter = new HomeAdapter(selectedApps, this::launchApp);
        rvApps.setAdapter(adapter);

        findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivityForResult(new Intent(this, AppPickerActivity.class), REQ_PICKER));

        findViewById(R.id.btnAllApps).setOnClickListener(v ->
                startActivityForResult(new Intent(this, AppPickerActivity.class), REQ_PICKER));

        startClock();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSelectedApps();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICKER) {
            loadSelectedApps();
        }
    }

    private void loadSelectedApps() {
        Set<String> saved = Prefs.getSelectedApps(this);
        selectedApps.clear();

        if (saved.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvApps.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            return;
        }

        PackageManager pm = getPackageManager();
        // Get all launchable apps
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> all = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo ri : all) {
            String pkg = ri.activityInfo.packageName;
            if (saved.contains(pkg)) {
                selectedApps.add(new AppInfo(
                        ri.loadLabel(pm).toString(),
                        pkg,
                        ri.loadIcon(pm),
                        true
                ));
            }
        }

        // Sort alphabetically
        Collections.sort(selectedApps, (a, b) -> a.label.compareToIgnoreCase(b.label));

        tvEmpty.setVisibility(selectedApps.isEmpty() ? View.VISIBLE : View.GONE);
        rvApps.setVisibility(selectedApps.isEmpty() ? View.GONE : View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    private void launchApp(AppInfo app) {
        Intent launch = getPackageManager().getLaunchIntentForPackage(app.packageName);
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
        }
    }

    // Live clock
    private final Runnable clockTick = new Runnable() {
        @Override
        public void run() {
            updateClock();
            clockHandler.postDelayed(this, 1000);
        }
    };

    private void startClock() {
        clockHandler.post(clockTick);
    }

    private void updateClock() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        // Time
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvTime.setText(timeFmt.format(cal.getTime()));

        // Date
        SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        tvDate.setText(dateFmt.format(cal.getTime()));

        // Greeting
        String greeting;
        if (hour < 12) greeting = "Good morning ✨";
        else if (hour < 17) greeting = "Good afternoon ⚡";
        else greeting = "Good evening 🌙";
        tvGreeting.setText(greeting);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clockHandler.removeCallbacks(clockTick);
    }

    // Prevent back button from leaving launcher
    @Override
    public void onBackPressed() {
        // intentionally empty — stay on home screen
    }
}
