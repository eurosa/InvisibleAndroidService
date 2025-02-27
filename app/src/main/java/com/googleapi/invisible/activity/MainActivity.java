package com.googleapi.invisible.activity;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googleapi.invisible.AppInfo;
import com.googleapi.invisible.AppListAdapter;
import com.googleapi.invisible.R;
import com.googleapi.invisible.service.AppLockService;
import com.googleapi.invisible.service.MyBackgroundService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CONTACTS = 100;
    private RecyclerView recyclerView;
    private AppListAdapter appListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode != AppOpsManager.MODE_ALLOWED) {
            // Permission is not granted; guide the user to settings
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_CONTACTS);
        } else {
            // Permission already granted, start the Service
         //   startMyService();
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<AppInfo> installedApps = getInstalledApps();
        appListAdapter = new AppListAdapter(installedApps, appInfo -> saveLockedApp(appInfo.getPackageName()));

        recyclerView.setAdapter(appListAdapter);

        // Start the background service to monitor app launches
        startService(new Intent(this, AppLockService.class));
    }

    private void startMyService() {
        Intent serviceIntent = new Intent(this, MyBackgroundService.class);
        startService(serviceIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start the Service
                startMyService();
            } else {
                // Permission denied
                // Handle this case, e.g., show a message to the user
                Toast.makeText(this, "Permission denied. Cannot access contacts.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<AppInfo> getInstalledApps() {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> applications = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : applications) {
            appList.add(new AppInfo(app.packageName, app.loadLabel(pm).toString(), app.loadIcon(pm)));
        }

        return appList;
    }

    private void saveLockedApp(String packageName) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppLocker", MODE_PRIVATE);
        Set<String> lockedApps = sharedPreferences.getStringSet("locked_apps", new HashSet<>());
        lockedApps.add(packageName);
        sharedPreferences.edit().putStringSet("locked_apps", lockedApps).apply();
    }
}