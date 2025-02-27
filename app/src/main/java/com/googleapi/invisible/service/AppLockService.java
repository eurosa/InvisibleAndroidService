package com.googleapi.invisible.service;

import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;


import com.googleapi.invisible.LockScreenActivity;

import java.util.Set;

public class AppLockService extends Service {
    private Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        monitorApps();
    }

    private void monitorApps() {
        handler.postDelayed(() -> {
            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            long currentTime = System.currentTimeMillis();
            String currentApp = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                currentApp = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        currentTime - 1000 * 10,
                        currentTime
                ).stream().max((o1, o2) -> Long.compare(o1.getLastTimeUsed(), o2.getLastTimeUsed())).get().getPackageName();
            }

            SharedPreferences sharedPreferences = getSharedPreferences("AppLocker", MODE_PRIVATE);
            Set<String> lockedApps = sharedPreferences.getStringSet("locked_apps", null);

            if (lockedApps != null && lockedApps.contains(currentApp)) {
                Intent intent = new Intent(this, LockScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            monitorApps();
        }, 1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}