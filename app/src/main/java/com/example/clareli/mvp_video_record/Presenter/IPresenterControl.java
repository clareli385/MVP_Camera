package com.example.clareli.mvp_video_record.Presenter;


import android.graphics.SurfaceTexture;

import com.example.clareli.mvp_video_record.View.AutoFitTextureView;
import com.example.clareli.mvp_video_record.View.IViewErrorCallback;

public interface IPresenterControl {
    void openCamera(SurfaceTexture surface, int width, int height);
    void closeCamera();
//    void startVideoRecord(String filePath, SurfaceTexture previewSurTexture, int width, int height);
//    void stopVideoRecord();
    void stopVideoEncode();
    void stopRecord();
//    void startAudioRecord(String filePath);
    void startRecord(String filePath, SurfaceTexture previewSurTexture, int width, int height);
//    void stopAudioRecord();
    void stopMuxer();
    void createMuxer(String dstPath);
}
