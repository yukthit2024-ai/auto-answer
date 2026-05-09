package com.vypeensoft.autoanswer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AutoAnswerService extends Service {
    private static final String TAG = "AutoAnswerService";
    private static final String CHANNEL_ID = "AutoAnswerChannel";
    private static final int NOTIFICATION_ID = 1;

    public static final String ACTION_CALL_RECEIVED = "com.vypeensoft.autoanswer.CALL_RECEIVED";
    public static final String ACTION_CALL_ENDED = "com.vypeensoft.autoanswer.CALL_ENDED";

    private AudioPlayerManager audioPlayerManager;

    @Override
    public void onCreate() {
        super.onCreate();
        audioPlayerManager = new AudioPlayerManager(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, getNotification("Auto Answer Service is active"));

        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (ACTION_CALL_RECEIVED.equals(action)) {
                handleIncomingCall();
            } else if (ACTION_CALL_ENDED.equals(action)) {
                handleCallEnded();
            }
        }

        return START_STICKY;
    }

    private void handleIncomingCall() {
        Log.d(TAG, "Handling incoming call...");
        
        // Attempt to answer the call
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                try {
                    // This requires ANSWER_PHONE_CALLS permission
                    telecomManager.acceptRingingCall();
                    Log.d(TAG, "Call accepted via TelecomManager");
                    
                    // Small delay to ensure call is fully established before playing audio
                    new android.os.Handler(getMainLooper()).postDelayed(this::startPlayback, 1500);
                    
                } catch (SecurityException e) {
                    Log.e(TAG, "Permission denied to answer call: " + e.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "Error answering call: " + e.getMessage());
                }
            }
        } else {
            Log.w(TAG, "Auto-answering not supported via official API on this Android version");
            // Older versions might require internal API hacks which are unreliable
        }
    }

    private void startPlayback() {
        SharedPreferences prefs = getSharedPreferences("AutoAnswerPrefs", MODE_PRIVATE);
        String audioPath = prefs.getString("selected_audio_path", null);
        
        if (audioPath != null) {
            Log.d(TAG, "Starting audio playback: " + audioPath);
            audioPlayerManager.playAudio(audioPath);
        } else {
            Log.w(TAG, "No audio file selected in settings");
        }
    }

    private void handleCallEnded() {
        Log.d(TAG, "Call ended, stopping playback");
        audioPlayerManager.stopAudio();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Auto Answer Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification getNotification(String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Auto Answer Enabled")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_menu_call)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Override
    public void onDestroy() {
        audioPlayerManager.stopAudio();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
