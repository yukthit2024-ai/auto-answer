package com.vypeensoft.autoanswer;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

public class AudioPlayerManager {
    private static final String TAG = "AudioPlayerManager";
    private MediaPlayer mediaPlayer;
    private Context context;

    public AudioPlayerManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void playAudio(String fileUriPath) {
        stopAudio();

        if (fileUriPath == null || fileUriPath.isEmpty()) {
            Log.e(TAG, "No audio file selected");
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();
            
            // Set audio attributes to play through the voice call stream
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setLegacyStreamType(AudioManager.STREAM_VOICE_CALL)
                    .build();
            
            mediaPlayer.setAudioAttributes(attributes);
            mediaPlayer.setDataSource(context, Uri.parse(fileUriPath));
            
            mediaPlayer.setOnPreparedListener(mp -> {
                Log.d(TAG, "MediaPlayer prepared, starting playback");
                mp.start();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Playback completed");
                stopAudio();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                stopAudio();
                return true;
            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            Log.e(TAG, "Error playing audio: " + e.getMessage());
            stopAudio();
        }
    }

    public void stopAudio() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaPlayer: " + e.getMessage());
            }
            mediaPlayer = null;
        }
    }
}
