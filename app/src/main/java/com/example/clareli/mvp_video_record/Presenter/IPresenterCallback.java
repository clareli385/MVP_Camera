package com.example.clareli.mvp_video_record.Presenter;

import android.hardware.camera2.CameraDevice;
import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public interface IPresenterCallback {
    void getCameraDevice(CameraDevice mCameraDevice);
    void errorCameraCallback();
    void errorCameraRecordCallback();
    void errorEncoderCallback();
    void errorDecoderCallback();
    void completedCameraCallback();
    void completedCameraRecordCallback();
    void completedEncoderCallback();
    void completedDecoderCallback();
    void onVideoOutputFormatChanged(MediaFormat format);
    void onVideoOutputBufferAvailable(MediaCodec.BufferInfo info, ByteBuffer encodedData);
    void muxerErrorCallback(String msg);

}
