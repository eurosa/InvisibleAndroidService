package com.googleapi.invisible;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class LockScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        EditText pinEditText = findViewById(R.id.pinEditText);
        Button unlockButton = findViewById(R.id.unlockButton);

        unlockButton.setOnClickListener(v -> {
            String enteredPin = pinEditText.getText().toString();
            if ("1234".equals(enteredPin)) { // Replace with secure PIN storage
                finish();
            } else {
                pinEditText.setError("Incorrect PIN");
            }
        });
    }
}