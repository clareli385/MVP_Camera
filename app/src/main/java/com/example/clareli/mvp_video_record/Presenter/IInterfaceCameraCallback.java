package com.example.clareli.mvp_video_record.Presenter;

public interface IInterfaceCameraCallback {
    void errorCameraCallback();
    void errorCameraRecordCallback();
    void completedCameraCallback();
    void completedCameraRecordCallback();
    void errorEncoderCallback();
    void completedEncoderCallback();
    void errorDecoderCallback();
    void completedDecoderCallback();
}
