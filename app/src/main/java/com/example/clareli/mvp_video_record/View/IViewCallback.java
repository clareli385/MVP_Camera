package com.example.clareli.mvp_video_record.View;

public interface IViewCallback {
    void showErrorMsg(String msg);
    void isVideoCodecSettingAllowed(boolean result);
    void isAudioCodecSettingAllowed(boolean result);

}
