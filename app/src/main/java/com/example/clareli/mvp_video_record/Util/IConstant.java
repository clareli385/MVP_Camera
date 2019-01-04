package com.example.clareli.mvp_video_record.Util;

import android.Manifest;

public interface IConstant {
    int REQUEST_PERMISSION_CODE = 0x20FF;
    int COMPLETE_RESULT = 100;
    int FAIL_RESULT = -1;
    static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

}
