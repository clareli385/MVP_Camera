package com.example.clareli.mvp_video_record.Presenter;

import android.hardware.camera2.CameraDevice;
import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public class PresenterCameraCallback {
    private IPresenterCameraCallback _cameraCallback;
    public PresenterCameraCallback(IPresenterCameraCallback callback){
        _cameraCallback = callback;
    }
    public void completedPreview(){
        _cameraCallback.completedCameraCallback();
    }
    public void errorPreview(){
        _cameraCallback.errorCameraCallback();
    }
    public void completedRecord(){
        _cameraCallback.completedCameraRecordCallback();
    }
    public void errorRecord(){
        _cameraCallback.errorCameraRecordCallback();
    }
    public void completedFromEncoder(){
        _cameraCallback.completedEncoderCallback();
    }
    public void errorFromEncoder(){
        _cameraCallback.errorEncoderCallback();
    }
    public void completedFromDecoder(){
        _cameraCallback.completedDecoderCallback();
    }
    public void errorFromDecoder(){
        _cameraCallback.errorDecoderCallback();
    }
    public void getCameraDevice(CameraDevice cameraDevice){
        _cameraCallback.getCameraDevice(cameraDevice);
    }
    public void getOutputFormatChanged(MediaFormat format){
        _cameraCallback.onOutputFormatChanged(format);
    }

    public void getOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info, ByteBuffer encodedData){
        _cameraCallback.onOutputBufferAvailable(codec, index, info, encodedData);
    }

}
