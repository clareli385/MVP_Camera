package com.example.clareli.mvp_video_record.Presenter;

import android.hardware.camera2.CameraDevice;
import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public class PresenterCameraCallback {
    private IPresenterCallback _cameraCallback;
    public PresenterCameraCallback(IPresenterCallback callback){
        _cameraCallback = callback;
    }
    public void getCameraDevice(CameraDevice cameraDevice){
        _cameraCallback.getCameraDevice(cameraDevice);
    }

    public void errorPreview(){
        _cameraCallback.errorCameraCallback();
    }

    public void errorRecord(){
        _cameraCallback.errorCameraRecordCallback();
    }

    public void errorFromEncoder(){
        _cameraCallback.errorEncoderCallback();
    }

    public void errorFromDecoder(){
        _cameraCallback.errorDecoderCallback();
    }

    public void completedPreview(){
        _cameraCallback.completedCameraCallback();
    }

    public void completedRecord(){
        _cameraCallback.completedCameraRecordCallback();
    }

    public void completedFromEncoder(){
        _cameraCallback.completedEncoderCallback();
    }
    public void completedFromDecoder(){
        _cameraCallback.completedDecoderCallback();
    }


    public void getOutputFormatChanged(MediaFormat format){
        _cameraCallback.onOutputFormatChanged(format);
    }

    public void getOutputBufferAvailable(MediaCodec.BufferInfo info, ByteBuffer encodedData){
        _cameraCallback.onOutputBufferAvailable(info, encodedData);
    }

}
