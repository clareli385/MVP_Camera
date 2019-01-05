package com.example.clareli.mvp_video_record.View;

public class ViewErrorCallback {
    IViewErrorCallback _viewErrorCallback;
    public ViewErrorCallback(IViewErrorCallback callback){
        _viewErrorCallback = callback;
    }
    public void viewShowErrorDialog(String msg){
        _viewErrorCallback.showErrorMsg(msg);

    }
}
