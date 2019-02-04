package com.example.clareli.mvp_video_record.Util;

import android.Manifest;

import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;

public interface IConstant {
    int REQUEST_PERMISSION_CODE = 0x20FF;
    static final String[] RECORD_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public final static String USER_PREFERENCES     = "User Preferences";

    /*2019-01-30, Clare
1.Type:video/avc is video software codec includes OMX.qcom.video.encoder.avc and OMX.google.h264.encoder
2.The MAX encoded count of OMX.qcom.video.encoder.avc is 16
3.The MAX encoded count of OMX.google.h264.encoder is 100
*/
    public final String VIDEO_AVC = MIMETYPE_VIDEO_AVC;  // H.264 Advanced Video Coding
    public final String AUDIO_AAC = MIMETYPE_AUDIO_AAC;  // H.264 Advanced Audio Coding
    public final String VIDEO_SUPPORT_CODEC_NAME = "h264";
    public final String AUDIO_SUPPORT_CODEC_NAME = "aac";
    public final String VIDEO_QCOM_ENCODER_AVC = "OMX.qcom.video.encoder.avc";
    public final String VIDEO_GOOGLE_H264_ENCODER = "OMX.google.h264.encoder";
    public final String AUDIO_GOOGLE_AAC_ENCODER = "OMX.google.aac.encoder";

}
