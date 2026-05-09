package com.vypeensoft.autoanswer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {

    public static final int PERMISSION_REQUEST_CODE = 101;

    public static String[] getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissions.add(Manifest.permission.ANSWER_PHONE_CALLS);
        permissions.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL);
        }

        return permissions.toArray(new String[0]);
    }

    public static boolean hasAllPermissions(Context context) {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, getRequiredPermissions(), PERMISSION_REQUEST_CODE);
    }
}
