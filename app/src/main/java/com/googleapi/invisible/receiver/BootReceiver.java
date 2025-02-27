package com.googleapi.invisible.receiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.googleapi.invisible.service.MyBackgroundService;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed. Starting service...");
            Intent serviceIntent = new Intent(context, MyBackgroundService.class);
            context.startService(serviceIntent);
        }
    }
}