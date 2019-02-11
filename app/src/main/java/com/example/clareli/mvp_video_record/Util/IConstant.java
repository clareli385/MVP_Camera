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
    public static final String USER_PREFERENCES     = "User Preferences";

    /*2019-01-30, Clare
1.Type:video/avc is video software codec includes OMX.qcom.video.encoder.avc and OMX.google.h264.encoder
2.The MAX encoded count of OMX.qcom.video.encoder.avc is 16
3.The MAX encoded count of OMX.google.h264.encoder is 100
*/
    public final String VIDEO_AVC = MIMETYPE_VIDEO_AVC;  // H.264 Advanced Video Coding
    public final String AUDIO_AAC = MIMETYPE_AUDIO_AAC;  // H.264 Advanced Audio Coding
    public final String VIDEO_SUPPORT_CODEC_NAME    = "h264";
    public final String AUDIO_SUPPORT_CODEC_NAME    = "aac";
    public final String VIDEO_QCOM_ENCODER_AVC      = "OMX.qcom.video.encoder.avc";
    public final String VIDEO_GOOGLE_H264_ENCODER   = "OMX.google.h264.encoder";
    public final String AUDIO_GOOGLE_AAC_ENCODER    = "OMX.google.aac.encoder";
    public final String ENCODEC_NAME                = "encodecName";
    public final String ENCODEC_AUDIO_TYPE          = "encodedAudioType";
    public final String ENCODEC_AUDIO_SAMPLE_RATE   = "sampleRate";
    public final String ENCODEC_AUDIO_CHANNEL_COUNT = "channelCount";
    public final String ENCODEC_AUDIO_BIT_RATE      = "bitRate";
    public final String ENCODEC_AUDIO_MAX_INPUT_SIZE= "maxInputSize";
    public final String ENCODEC_PROFILE_LEVEL       = "profileLevel";
    public final String ENCODEC_VIDEO_TYPE          = "encodedVideoType";
    public final String VIDEO_WIDTH                 = "width";
    public final String VIDEO_HEIGHT                = "height";
    public final String ENCODEC_VIDEO_FRAME_RATES   = "videoFrameRates";
    public final String ENCODEC_VIDEO_BIT_RATE      = "videoBitrate";
    public final String ENCODEC_VIDEO_IFRAME_INTERVAL= "iFrameInterval";
    public final String ENCODEC_VIDEO_COLOR_FORMAT  = "colorFormat";
    public final String ENCODEC_VIDEO_SELECTED_RECORD = "video_selected_record";
    public final String ENCODEC_AUDIO_SELECTED_RECORD = "audio_selected_record";

}
