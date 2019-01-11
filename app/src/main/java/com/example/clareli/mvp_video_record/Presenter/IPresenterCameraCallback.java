package com.example.clareli.mvp_video_record.Presenter;

import android.hardware.camera2.CameraDevice;
import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public interface IPresenterCameraCallback {
    void getCameraDevice(CameraDevice mCameraDevice);
    void errorCameraCallback();
    void errorCameraRecordCallback();
    void errorEncoderCallback();
    void errorDecoderCallback();
    void completedCameraCallback();
    void completedCameraRecordCallback();
    void completedEncoderCallback();
    void completedDecoderCallback();
    void onOutputFormatChanged(MediaFormat format);
    void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info, ByteBuffer encodedData);

}
