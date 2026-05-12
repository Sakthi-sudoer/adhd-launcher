package com.adhd.launcher;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private WebView webView;
    private TextView tvClock;
    private final Handler clockHandler = new Handler(Looper.getMainLooper());

    // Secret tap counter: tap clock 5 times to open app settings
    private int tapCount = 0;
    private long lastTapTime = 0;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        webView = findViewById(R.id.webView);
        tvClock = findViewById(R.id.tvClock);

        setupWebView();
        setupSecretTap();
        startClock();

        // Load the ADHD Life OS main page from assets
        webView.loadUrl("file:///android_asset/ADHD%20Life%20OS/ADHD%20Life%20OS%200de1959334c983c2ad0801d566a3d830.html");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // Keep all file:// links inside the WebView (offline navigation)
                if (url.startsWith("file://")) {
                    return false;
                }
                // Launch app from dock
                if (url.startsWith("adhd-app://")) {
                    String pkg = url.replace("adhd-app://", "");
                    launchPackage(pkg);
                    return true;
                }
                // For any external http links, try to launch installed browser
                if (url.startsWith("http")) {
                    tryLaunchApp(url);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Re-inject dock after every page load
                loadSelectedAppsOverlay();
            }
        });
    }

    private void launchPackage(String pkg) {
        Intent launch = getPackageManager().getLaunchIntentForPackage(pkg);
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
        }
    }

    private void tryLaunchApp(String url) {
        // Try to open the URL in an installed browser if available
        Intent intent = new Intent(Intent.ACTION_VIEW,
                android.net.Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        if (!activities.isEmpty()) {
            startActivity(intent);
        }
    }

    // Tap the clock 5 times quickly to reveal app picker
    private void setupSecretTap() {
        tvClock.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (now - lastTapTime > 2000) {
                tapCount = 0;
            }
            lastTapTime = now;
            tapCount++;
            if (tapCount >= 5) {
                tapCount = 0;
                startActivity(new Intent(this, AppPickerActivity.class));
            }
        });
    }

    // Live clock
    private final Runnable clockTick = new Runnable() {
        @Override
        public void run() {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat fmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvClock.setText(fmt.format(cal.getTime()));
            clockHandler.postDelayed(this, 30000);
        }
    };

    private void startClock() {
        clockHandler.post(clockTick);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload selected apps into the home screen if needed
        loadSelectedAppsOverlay();
    }

    private void loadSelectedAppsOverlay() {
        Set<String> selected = Prefs.getSelectedApps(this);
        if (selected.isEmpty()) return;

        // Build a JS snippet that injects a floating app dock at the bottom of the page
        PackageManager pm = getPackageManager();
        StringBuilder js = new StringBuilder();
        js.append("(function(){");
        js.append("var old=document.getElementById('adhd-dock');if(old)old.remove();");
        js.append("var dock=document.createElement('div');");
        js.append("dock.id='adhd-dock';");
        js.append("dock.style.cssText='position:fixed;bottom:0;left:0;right:0;");
        js.append("background:rgba(30,16,51,0.95);display:flex;flex-wrap:wrap;");
        js.append("justify-content:center;padding:8px 4px;z-index:9999;");
        js.append("border-top:1px solid rgba(124,58,237,0.4);';");

        Intent launchIntent = new Intent(Intent.ACTION_MAIN, null);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> all = pm.queryIntentActivities(launchIntent, 0);

        for (ResolveInfo ri : all) {
            String pkg = ri.activityInfo.packageName;
            if (!selected.contains(pkg)) continue;
            String label = ri.loadLabel(pm).toString()
                    .replace("'", "\\'").replace("\"", "\\\"");
            js.append("var a=document.createElement('a');");
            js.append("a.href='adhd-app://").append(pkg).append("';");
            js.append("a.style.cssText='color:#fff;font-size:10px;text-align:center;");
            js.append("padding:4px 8px;text-decoration:none;display:flex;");
            js.append("flex-direction:column;align-items:center;min-width:56px;';");
            js.append("a.innerHTML='<span style=\"font-size:22px\">📱</span><span>")
              .append(label).append("</span>';");
            js.append("dock.appendChild(a);");
        }

        js.append("document.body.appendChild(dock);");
        js.append("})();");

        webView.evaluateJavascript(js.toString(), null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clockHandler.removeCallbacks(clockTick);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
        // Don't exit — stay on home screen
    }
}
