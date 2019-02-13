package com.example.clareli.mvp_video_record.Presenter;


import android.graphics.SurfaceTexture;

import com.example.clareli.mvp_video_record.Util.LUAudioCodecInfo;
import com.example.clareli.mvp_video_record.Util.LUAudioCodecProfile;
import com.example.clareli.mvp_video_record.Util.LUVideoCodecInfo;
import com.example.clareli.mvp_video_record.Util.LUVideoCodecProfile;

public interface IPresenterControl {
    void openCamera(SurfaceTexture surface, int width, int height);
    void closeCamera();
    void stopVideoEncode();
    void stopRecord();
    void startRecord(String filePath, SurfaceTexture previewSurTexture, int width, int height);
    void stopMuxer();
    void createMuxer(String dstPath);
    void findAllSupportedCodecs();
    void separateCodecs(String formatType);
    boolean getPreferenceAudioCodecSettings();
    boolean getPreferenceVideoCodecSettings();
    LUVideoCodecInfo getVideoCodecInfoRangeByName(String codecName);
    LUAudioCodecInfo getAudioCodecInfoRangeByName(String codecName);
    void createDefaultVideoCodecSelection(LUVideoCodecInfo codecInfo);
    void createDefaultAudioCodecSelection(LUAudioCodecInfo codecInfo);
    boolean isAudioCodecSettingAvailable(LUAudioCodecProfile audioCodecProfile);
    boolean isVideoCodecSettingAvailable(LUVideoCodecProfile videoCodecSettings);

}
