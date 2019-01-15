package com.example.clareli.mvp_video_record.Util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

 public class LUPermissionCheck {
     static public boolean hasPermissionsGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

     static public void requestPermission(Activity activity, String[] permissionArray, int permission_code) {
        ActivityCompat.requestPermissions(activity, permissionArray, permission_code);
    }



}
