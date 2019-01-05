package com.example.clareli.mvp_video_record.Presenter;


import com.example.clareli.mvp_video_record.View.AutoFitTextureView;
import com.example.clareli.mvp_video_record.View.IViewErrorCallback;

public interface IPresenterCameraControl {
    void videoRecordStart(String filePath);
    void videoRecordStop();
    void videoPreviewStart(AutoFitTextureView textureView, IViewErrorCallback iViewErrorCallback);
    void closeCamera();
    void cameraOpenError();
    void startBackground();
}
