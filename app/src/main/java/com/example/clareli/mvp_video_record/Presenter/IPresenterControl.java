package com.example.clareli.mvp_video_record.Presenter;


import android.graphics.SurfaceTexture;

import com.example.clareli.mvp_video_record.View.AutoFitTextureView;
import com.example.clareli.mvp_video_record.View.IViewErrorCallback;

public interface IPresenterControl {
    void openCamera(SurfaceTexture surface, int width, int height);
    void closeCamera();
    void videoRecordStart(String filePath, SurfaceTexture previewSurTexture, int width, int height);
    void videoRecordStop();
    void stopVideoEncode();
    void audioRecordStart(String filePath);
    void audioRecordStop();
}
