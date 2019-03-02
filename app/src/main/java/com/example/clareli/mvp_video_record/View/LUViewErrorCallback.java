package com.example.clareli.mvp_video_record.View;

public class LUViewErrorCallback {
    IViewErrorCallback _viewErrorCallback;
    public LUViewErrorCallback(IViewErrorCallback callback){
        _viewErrorCallback = callback;
    }
    public void viewShowErrorDialog(String msg){
        _viewErrorCallback.showErrorMsg(msg);

    }
}
