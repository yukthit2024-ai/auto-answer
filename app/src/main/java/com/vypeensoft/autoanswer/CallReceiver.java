package com.vypeensoft.autoanswer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null || !intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences("AutoAnswerPrefs", Context.MODE_PRIVATE);
        boolean isEnabled = prefs.getBoolean("is_enabled", false);

        if (!isEnabled) {
            return;
        }

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        Log.d(TAG, "Phone State Changed: " + state);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            // Incoming call ringing
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            Log.d(TAG, "Incoming call from: " + incomingNumber);
            
            Intent serviceIntent = new Intent(context, AutoAnswerService.class);
            serviceIntent.setAction(AutoAnswerService.ACTION_CALL_RECEIVED);
            context.startService(serviceIntent);

        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            // Call ended or rejected
            Intent serviceIntent = new Intent(context, AutoAnswerService.class);
            serviceIntent.setAction(AutoAnswerService.ACTION_CALL_ENDED);
            context.startService(serviceIntent);
        }
    }
}
