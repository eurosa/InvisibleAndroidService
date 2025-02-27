package com.googleapi.invisible;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class UsageStatsHelper {
    private final Context context;

    public UsageStatsHelper(Context context) {
        this.context = context;
    }

    /**
     * Checks if the app has permission to access usage stats.
     */
    public boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps != null) {
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), context.getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        }
        return false;
    }

    /**
     * Requests the user to grant usage stats permission.
     */
    public void requestUsageStatsPermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Gets the package name of the currently foreground app.
     *
     * @return The package name of the currently foreground app, or null if unavailable.
     */
    public String getForegroundAppPackageName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usageStatsManager == null) return null;

            long currentTime = System.currentTimeMillis();
            List<UsageStats> stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    currentTime - 1000 * 10, // Past 10 seconds
                    currentTime
            );

            if (stats != null) {
                SortedMap<Long, UsageStats> sortedStats = new TreeMap<>();
                for (UsageStats usageStat : stats) {
                    sortedStats.put(usageStat.getLastTimeUsed(), usageStat);
                }

                if (!sortedStats.isEmpty()) {
                    return sortedStats.get(sortedStats.lastKey()).getPackageName();
                }
            }
        }
        return null;
    }
}
