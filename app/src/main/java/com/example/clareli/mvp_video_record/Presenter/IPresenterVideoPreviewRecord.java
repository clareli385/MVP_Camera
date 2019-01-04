package com.example.clareli.mvp_video_record.Presenter;


import com.example.clareli.mvp_video_record.View.AutoFitTextureView;
import com.example.clareli.mvp_video_record.View.IViewVideoRecordCallback;

public interface IPresenterVideoPreviewRecord {
    void videoRecordStart(String filePath);
    void videoRecordStop();
    void videoPreviewStart(AutoFitTextureView textureView, IViewVideoRecordCallback iViewVideoRecordCallback);
    void closeCamera();
    void cameraOpenError();
    void startBackground();
    void viewShowMsg(String msg);
}
