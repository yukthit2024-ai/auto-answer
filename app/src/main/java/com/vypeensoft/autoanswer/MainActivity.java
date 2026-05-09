package com.vypeensoft.autoanswer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private View statusIndicator;
    private Button btnStart, btnStop, btnSettings;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("AutoAnswerPrefs", MODE_PRIVATE);

        statusText = findViewById(R.id.status_text);
        statusIndicator = findViewById(R.id.status_indicator);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        btnSettings = findViewById(R.id.btn_settings);

        btnStart.setOnClickListener(v -> toggleAutoAnswer(true));
        btnStop.setOnClickListener(v -> toggleAutoAnswer(false));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        updateUI();
        
        // Request permissions on startup if not granted
        if (!PermissionUtils.hasAllPermissions(this)) {
            PermissionUtils.requestPermissions(this);
        }
    }

    private void toggleAutoAnswer(boolean enable) {
        if (enable && !PermissionUtils.hasAllPermissions(this)) {
            Toast.makeText(this, "Please grant all permissions first", Toast.LENGTH_LONG).show();
            PermissionUtils.requestPermissions(this);
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_enabled", enable);
        editor.apply();

        Intent serviceIntent = new Intent(this, AutoAnswerService.class);
        if (enable) {
            ContextCompat.startForegroundService(this, serviceIntent);
        } else {
            stopService(serviceIntent);
        }

        updateUI();
    }

    private void updateUI() {
        boolean isEnabled = prefs.getBoolean("is_enabled", false);

        if (isEnabled) {
            statusText.setText("Auto Answer Enabled");
            statusIndicator.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
        } else {
            statusText.setText("Auto Answer Disabled");
            statusIndicator.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.hasAllPermissions(this)) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Some permissions were denied. App may not work correctly.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}
