package com.vypeensoft.autoanswer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private View statusIndicator;
    private MaterialButton btnStart, btnStop, btnSettings;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide ActionBar for a cleaner splash-like look on main screen
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        prefs = getSharedPreferences("AutoAnswerPrefs", MODE_PRIVATE);

        statusText = findViewById(R.id.status_text);
        statusIndicator = findViewById(R.id.status_indicator);
        btnStart = (MaterialButton) findViewById(R.id.btn_start);
        btnStop = (MaterialButton) findViewById(R.id.btn_stop);
        btnSettings = (MaterialButton) findViewById(R.id.btn_settings);

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

        // Check if audio file is selected before starting
        if (enable) {
            String audioPath = prefs.getString("selected_audio_path", null);
            if (audioPath == null) {
                Toast.makeText(this, "Please select an audio file in Settings first", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, SettingsActivity.class));
                return;
            }
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_enabled", enable);
        editor.apply();

        Intent serviceIntent = new Intent(this, AutoAnswerService.class);
        if (enable) {
            ContextCompat.startForegroundService(this, serviceIntent);
            Toast.makeText(this, R.string.status_enabled, Toast.LENGTH_SHORT).show();
        } else {
            stopService(serviceIntent);
            Toast.makeText(this, R.string.status_disabled, Toast.LENGTH_SHORT).show();
        }

        updateUI();
    }

    private void updateUI() {
        boolean isEnabled = prefs.getBoolean("is_enabled", false);

        if (isEnabled) {
            statusText.setText(R.string.status_enabled);
            statusText.setTextColor(ContextCompat.getColor(this, R.color.status_enabled));
            statusIndicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_enabled)));
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
            btnStart.setAlpha(0.5f);
            btnStop.setAlpha(1.0f);
        } else {
            statusText.setText(R.string.status_disabled);
            statusText.setTextColor(ContextCompat.getColor(this, R.color.status_disabled));
            statusIndicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_disabled)));
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
            btnStart.setAlpha(1.0f);
            btnStop.setAlpha(0.5f);
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
