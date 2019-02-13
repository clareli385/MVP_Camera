package com.example.clareli.mvp_video_record.View;

public class LUViewCallback {
    IViewCallback _viewCallback;
    public LUViewCallback(IViewCallback callback){
        _viewCallback = callback;
    }
    public void viewShowErrorDialog(String msg){
        _viewCallback.showErrorMsg(msg);

    }

    public void isVideoSettingAllowed(boolean result){
        _viewCallback.isVideoCodecSettingAllowed(result);
    }

    public void isAudioSettingAllowed(boolean result){
        _viewCallback.isAudioCodecSettingAllowed(result);
    }
}
