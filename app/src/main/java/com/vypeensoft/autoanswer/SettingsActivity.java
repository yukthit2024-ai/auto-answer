package com.vypeensoft.autoanswer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private TextView selectedFileText;
    private Button btnPickFile;
    private SharedPreferences prefs;

    private final ActivityResultLauncher<String> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    saveFileUri(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Setup ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        prefs = getSharedPreferences("AutoAnswerPrefs", MODE_PRIVATE);

        selectedFileText = findViewById(R.id.selected_file_text);
        btnPickFile = findViewById(R.id.btn_pick_file);

        btnPickFile.setOnClickListener(v -> {
            // Support MP3, WAV, M4A
            filePickerLauncher.launch("audio/*");
        });

        displayCurrentFile();
    }

    private void saveFileUri(Uri uri) {
        // Take persistable URI permission for later use in service
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
            // This might happen if the URI doesn't support persistable permissions
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("selected_audio_path", uri.toString());
        editor.apply();

        displayCurrentFile();
        Toast.makeText(this, "Audio file selected", Toast.LENGTH_SHORT).show();
    }

    private void displayCurrentFile() {
        String path = prefs.getString("selected_audio_path", getString(R.string.no_file_selected));
        selectedFileText.setText(path);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
