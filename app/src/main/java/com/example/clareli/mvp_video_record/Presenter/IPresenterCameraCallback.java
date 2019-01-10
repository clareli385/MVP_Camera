package com.example.clareli.mvp_video_record.Presenter;

import android.hardware.camera2.CameraDevice;

import java.nio.ByteBuffer;

public interface IPresenterCameraCallback {
    void errorCameraCallback();
    void errorCameraRecordCallback();
    void completedCameraCallback();
    void completedCameraRecordCallback();
    void errorEncoderCallback();
    void completedEncoderCallback();
    void errorDecoderCallback();
    void completedDecoderCallback();
    void getCameraDevice(CameraDevice mCameraDevice);
}
